package myzd.config;

import lombok.extern.slf4j.Slf4j;
import myzd.annotations.FilterParam;
import myzd.annotations.FlowControl;
import myzd.annotations.PenetrationConfig;
import myzd.domain.visitlog.TemplateEnum;
import myzd.services.impl.FilterParamService;
import myzd.services.impl.FlowControlService;
import myzd.services.impl.PenetrationKafkaService;
import myzd.services.impl.PenetrationService;
import myzd.utils.RequestHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
  private PenetrationService penetrationService;
  @Autowired
  private PenetrationKafkaService penetrationKafkaService;
  @Autowired
  private FilterParamService filterParamService;
  @Autowired
  private Environment environment;

  private ThreadLocal<Long> startTime = new ThreadLocal<>();
  private ThreadLocal<Map<String, String>> requestInfo = new ThreadLocal<>();

  @Pointcut("execution(public * myzd.api.controllers.*.*(..))")
  public void init() {
  }

  @Before("init()")
  public void filterBeforeHandling(JoinPoint joinPoint) throws Exception {
    log.debug("before handing");
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
    //action
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Method action = methodSignature.getMethod();
    //接收到请求,记录请求内容
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = attributes.getRequest();
    //流控
    String clientIp = request.getRemoteAddr();
    String sessionId = request.getSession().getId();
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();
    long requestTimestamp = System.currentTimeMillis();
    flowControlService.flowController(clientIp, sessionId, requestUri, requestMethod, requestTimestamp, action.getDeclaredAnnotation(FlowControl.class));
    //过滤参数
    Map<String, String[]> filterParam = filterParamService.filterParam(request.getParameterMap(), action.getDeclaredAnnotation(FilterParam.class));
    //触发action, 完成参数校验部分
    Object object = joinPoint.proceed();
    log.debug("local response: {}", object);
    //透传
    PenetrationConfig penetrationConfig = action.getAnnotation(PenetrationConfig.class);
    if (penetrationConfig != null) {
      object = penetrationService.penetrate(request, action, methodSignature.getDeclaringType(), filterParam);
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
    log.debug("send message message: {}", message.substring(1));
    penetrationKafkaService.sendMessage(environment.getProperty(ENV_LOG_KAFKA_TOPIC), String.valueOf(message).substring(1));
    log.info("RESPONSE : " + response);
  }

  @AfterThrowing(value = "init()", throwing = "ex")
  private void filterAfterThrowing(Throwable ex) {
    log.error("has some exception.", ex);
  }

}
