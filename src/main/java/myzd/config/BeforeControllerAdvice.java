package myzd.config;

import lombok.extern.slf4j.Slf4j;
import myzd.annotations.FilterParam;
import myzd.annotations.FlowControl;
import myzd.annotations.PipeConfig;
import myzd.domain.request.ResultWrapper;
import myzd.domain.visitlog.TemplateEnum;
import myzd.services.impl.*;
import myzd.utils.RequestHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created by Mike He on 4/28/2017.
 */

@Component
@Aspect
@Slf4j
public class BeforeControllerAdvice {

  private final static String ENV_LOG_KAFKA_TOPIC = "topic.boot";
  private final static String ENV_LOG_KAFKA_MESSAGE_SOURCE = "message.source";
  private final static String ENV_LOG_KAFKA_MESSAGE_FORMAT = "log.access.format";
  private final static String ENV_APPLICATION_NAME = "application.name";

  @Autowired
  private FlowControlService flowControlService;
  @Autowired
  private PipeService penetrationService;
  @Autowired
  private PipeKafkaService pipeKafkaService;
  @Autowired
  private FilterParamService filterParamService;
  @Autowired
  private Environment environment;
  @Autowired
  private TaskExecutor kafkaMsgExecutor;
  @Autowired
  private CheckContentType checkContentType;

  private ThreadLocal<Long> startTime = new ThreadLocal<>();
  private ThreadLocal<Map<String, String>> requestInfo = new ThreadLocal<>();

  @Pointcut("execution(public * myzd.api.controllers.*.*(..))")
  public void init() {
  }

  @Before("init()")
  public void filterBeforeHandling(JoinPoint joinPoint) throws Exception {
    log.debug("before handing");

    //得到request
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = attributes.getRequest();

    Map<String, String> requestInfoMap = new LinkedHashMap<>();
    String clientIpAddr = RequestHelper.getRealIp(request);
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();

    int size = 0;
    requestInfoMap.put(TemplateEnum.MESSAGE_SOURCE, environment.getProperty(ENV_LOG_KAFKA_MESSAGE_SOURCE));
    requestInfoMap.put(TemplateEnum.REMOTE_HOST, clientIpAddr);
    requestInfoMap.put(TemplateEnum.REQUEST_METHOD, requestMethod);
    requestInfoMap.put(TemplateEnum.RESPONSE_BODY_SIZE, String.valueOf(size));
    requestInfoMap.put(TemplateEnum.REQUEST_URI, requestUri);
    requestInfoMap.put(TemplateEnum.SERVICE_NAME, environment.getProperty(ENV_APPLICATION_NAME));
    requestInfo.set(requestInfoMap);

    startTime.set(System.currentTimeMillis());
  }

  @Around("init()")
  public Object filterAroundHandling(ProceedingJoinPoint joinPoint) throws Throwable {
    log.debug("around handing");

    //接收到请求,记录请求内容
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = attributes.getRequest();
    //检查contentType类型
    if(!checkContentType.checkRequestContentType(request)){
      ResultWrapper resultWrapper = new ResultWrapper();
      resultWrapper.setCode(200);
      resultWrapper.setMessage("invalid request format");
      return resultWrapper;
    }

    //得到action方法
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Method action = methodSignature.getMethod();

    //流控
    String clientIp = request.getRemoteAddr();
    String sessionId = request.getSession().getId();
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();
    long requestTimestamp = System.currentTimeMillis();
    flowControlService.flowController(clientIp, sessionId, requestUri, requestMethod, requestTimestamp, action.getDeclaredAnnotation(FlowControl.class));


    //得到参数中标注了@RequestBody的参数内容，和HttpServletResponse
    HttpServletResponse response = null;
    List<Object> requestBodyList = new ArrayList<>();
    BindingResult result = null;

    Parameter[] parameters = action.getParameters();
    Object[] parameterContent = joinPoint.getArgs();

    for(int i = 0; i<parameters.length; i++){
      if(parameters[i].getAnnotation(RequestBody.class)!=null){
        requestBodyList.add(parameterContent[i]);
      }else if(parameterContent[i] instanceof HttpServletResponse){
        response = (HttpServletResponse)parameterContent[i];
      }else if(parameterContent[i] instanceof BindingResult){
        result = (BindingResult)parameterContent[i];
      }
    }

    //过滤参数
    Map<String, String[]> filterParam = filterParamService.filterParam(request.getParameterMap(), action.getAnnotation(FilterParam.class));

    //触发action, 完成参数校验部分
    Object object = joinPoint.proceed();
    if(result!=null && result.hasErrors()){
      ResultWrapper<String> resultWrapper = new ResultWrapper<>();
      resultWrapper.setCode(200);
      resultWrapper.setMessage(result.getFieldError().getDefaultMessage());
      return resultWrapper;
    }
    log.debug("local response: {}", object);

    //透传
    PipeConfig pipeConfig = action.getAnnotation(PipeConfig.class);
    if (pipeConfig != null) {
      object = penetrationService.penetrate(request, response, action, requestBodyList, methodSignature.getDeclaringType(), filterParam);
    }
    log.debug("penetration response: {}", object);

    return object;
  }

  @AfterReturning(returning = "response", pointcut = "init()")
  public void doAfterReturning(Object response) throws Throwable {
    // 处理完请求, 发送kafka消息
    String responseTime = String.valueOf((double) (System.currentTimeMillis() - startTime.get()) / 1000);
    Map<String, String> requestInfoMap = requestInfo.get();
    requestInfoMap.put(TemplateEnum.RESPONSE_TIME, responseTime);
    requestInfoMap.put(TemplateEnum.RESPONSE_STATUS, HttpStatus.OK.toString());
    List<String> keyList = Arrays.asList(environment.getProperty(ENV_LOG_KAFKA_MESSAGE_FORMAT).split("\\|"));
    StringBuilder message = new StringBuilder();
    for (String key : keyList) {
      log.debug(key);
      message.append("|");
      message.append(requestInfoMap.get(key));
    }
    kafkaMsgExecutor.execute(() -> {
      log.debug("send message: {}", message.substring(1));
      try {
        pipeKafkaService.sendMessage(environment.getProperty(ENV_LOG_KAFKA_TOPIC), String.valueOf(message).substring(1));
      } catch (IOException e) {
        log.warn("send request kafka message error.", e);
      }
    });
    log.info("RESPONSE : " + response);
  }

  @AfterThrowing(value = "init()", throwing = "ex")
  private void filterAfterThrowing(Throwable ex) {
    log.warn("has some exception.", ex);
  }

}