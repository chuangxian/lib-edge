package myzd.config;

import lombok.extern.slf4j.Slf4j;
import myzd.annotations.Authentication;
import myzd.annotations.FlowControl;
import myzd.annotations.PipeConfig;
import myzd.domain.exceptions.GenericException;
import myzd.domain.request.ResultWrapper;
import myzd.domain.visitlog.TemplateEnum;
import myzd.services.impl.FlowControlService;
import myzd.services.impl.PipeService;
import myzd.utils.RequestHelper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by Mike He on 4/28/2017.
 */

@Component
@Aspect
@Slf4j
public class BeforeControllerAdvice {

	private final static String ENV_LOG_KAFKA_MESSAGE_SOURCE = "message.source";
	private final static String ENV_APPLICATION_NAME = "application.name";

	@Autowired
	private FlowControlService flowControlService;
	@Autowired
	private PipeService penetrationService;

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

		//得到action方法
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method action = methodSignature.getMethod();

		//查看是否标注了@Authentication，如果标注了，session里应该有信息
		if (action.getAnnotation(Authentication.class) != null) {
			boolean hasUserInfo = request.getSession().getAttribute("uid") != null
							&& StringUtils.isNotBlank(request.getSession().getAttribute("uid").toString());
			if (!hasUserInfo) {
				throw new GenericException("2411006", "unauthenticated");
			}
		}

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

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getAnnotation(RequestBody.class) != null) {
				requestBodyList.add(parameterContent[i]);
			} else if (parameterContent[i] instanceof HttpServletResponse) {
				response = (HttpServletResponse) parameterContent[i];
			} else if (parameterContent[i] instanceof BindingResult) {
				result = (BindingResult) parameterContent[i];
			}
		}

		//触发action, 完成参数校验部分
		Object object = joinPoint.proceed();
		if (result != null && result.hasErrors()) {
			ResultWrapper<String> resultWrapper = new ResultWrapper<>();
			resultWrapper.setCode(200);
			resultWrapper.setMessage(result.getFieldError().getDefaultMessage());
			return resultWrapper;
		}
		log.debug("local response: {}", object);

		//透传
		PipeConfig pipeConfig = action.getAnnotation(PipeConfig.class);
		if (pipeConfig != null) {
			object = penetrationService.penetrate(request, response, action, requestBodyList, methodSignature.getDeclaringType(), request.getParameterMap());
		}
		log.debug("penetration response: {}", object);

		return object;
	}

	@AfterThrowing(value = "init()", throwing = "ex")
	private void filterAfterThrowing(Throwable ex) {
		log.warn("has some exception.", ex);
	}

}