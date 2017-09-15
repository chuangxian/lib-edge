package myzd.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.RequestFlowHandle;
import myzd.domain.HttpRequestDomain;
import myzd.domain.visitlog.TemplateEnum;
import myzd.services.impl.RequestFlowHandlerService;
import myzd.services.impl.TransferKafkaService;
import myzd.utils.RequestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zks on 2017/9/4.
 * 处理请求日志:
 * 1. kafka
 * 2. prometheus
 */
@Slf4j
@Component
public class PrometheusRequestInterceptor extends HandlerInterceptorAdapter {

  private static long startTime;

  @Autowired
  private TransferKafkaService transferKafkaService;
  @Autowired
  private RequestFlowHandlerService requestFlowHandlerService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;
  private HttpRequestDomain httpRequestDomain;
  private Map<String, Object> contentMap;
  private String messageSource;
  private String format;
  private String serviceName;
  private String edgeLogTopic;

  public PrometheusRequestInterceptor(TransferKafkaService transferKafkaService, ObjectMapper objectMapper, Environment env, RequestFlowHandlerService requestFlowHandlerService) {
    this.transferKafkaService = transferKafkaService;
    this.objectMapper = objectMapper;
    this.env = env;
    this.requestFlowHandlerService = requestFlowHandlerService;
    this.messageSource = this.env.getProperty("message.source");
    this.format = this.env.getProperty("log.access.format");
    this.serviceName = this.env.getProperty("application.name");
    this.edgeLogTopic = this.env.getProperty("edge.gateway.topic.log");
  }

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if ("OPTIONS".equals(request.getMethod())) return true;
    httpRequestDomain = new HttpRequestDomain();
    contentMap = new LinkedHashMap<>();
    startTime = System.currentTimeMillis();
    HttpSession session = request.getSession();
    request.getContextPath();
    String clientIpAddr = RequestHelper.getRealIp(request);
    String requestUrl = String.valueOf(request.getRequestURL());
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();
    int size = 0;
    int responseStatus = response.getStatus();
    httpRequestDomain.setRemoteAddr(clientIpAddr);
    httpRequestDomain.setRequestUrl(requestUrl);
    httpRequestDomain.setRequestUri(requestUri);
    httpRequestDomain.setRequestMethod(requestMethod);
    httpRequestDomain.setTimestamp(startTime);
    httpRequestDomain.setSessionId(session.getId());
    contentMap.put(TemplateEnum.MESSAGE_SOURCE, messageSource);
    contentMap.put(TemplateEnum.REMOTE_HOST, clientIpAddr);
    contentMap.put(TemplateEnum.REQUEST_METHOD, requestMethod);
    contentMap.put(TemplateEnum.RESPONSE_STATUS, responseStatus);
    contentMap.put(TemplateEnum.RESPONSE_BODY_SIZE, size);
    contentMap.put(TemplateEnum.REQUEST_URI, requestUri);
    contentMap.put(TemplateEnum.SERVICE_NAME, serviceName);
    HandlerMethod handlerMethod = HandlerMethod.class.cast(handler);
    if (handlerMethod.hasMethodAnnotation(RequestFlowHandle.class)) {
      RequestFlowHandle requestFlowHandle = handlerMethod.getMethodAnnotation(RequestFlowHandle.class);
      requestFlowHandlerService.handleRequestFlow(httpRequestDomain, requestFlowHandle);
    } else {
      log.debug("request have not request flow annotation");
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception arg3) throws Exception {
    if ("OPTIONS".equals(request.getMethod())) return;
    long endTime = System.currentTimeMillis();
    String responseTime = String.valueOf((double) (endTime - startTime) / 1000);
    contentMap.put(TemplateEnum.RESPONSE_TIME, responseTime);
    List<String> keyList = Arrays.asList(format.split("\\|"));
    StringBuilder message = new StringBuilder();
    for (String key : keyList) {
      log.debug(key);
      message.append("|");
      message.append(contentMap.get(key));
    }
    transferKafkaService.sendMessage(edgeLogTopic, String.valueOf(message).substring(1));
  }
}
