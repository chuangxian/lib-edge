package myzd.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.RequestFlowHandle;
import myzd.domain.HttpRequestDomain;
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
import java.text.SimpleDateFormat;
import java.util.*;

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

  public PrometheusRequestInterceptor(TransferKafkaService transferKafkaService, ObjectMapper objectMapper, Environment env, RequestFlowHandlerService requestFlowHandlerService) {
    this.transferKafkaService = transferKafkaService;
    this.objectMapper = objectMapper;
    this.env = env;
    this.requestFlowHandlerService = requestFlowHandlerService;
  }

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    httpRequestDomain = new HttpRequestDomain();
    startTime = System.currentTimeMillis();
    HttpSession session = request.getSession();
    Random random = new Random();
    request.getContextPath();
    String apiRequestTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
    String clientIpAddr = RequestHelper.getRealIp(request);
    String requestUrl = String.valueOf(request.getRequestURL());
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();
    List<String> paras = new ArrayList<>();
    String requestId = String.valueOf(requestUri.hashCode());
    String responseBody = String.valueOf((Math.abs(random.nextInt() % 100)));
    Map<String, String[]> parameters = request.getParameterMap();
    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      paras.add(entry.getKey() + " " + Arrays.toString(entry.getValue()));
    }
    String stringOfParas = "";
    for (String para : paras) {
      stringOfParas = stringOfParas + " " + para + " ";
    }
    stringOfParas = stringOfParas.substring(0, stringOfParas.length());
    httpRequestDomain.setRequestId(requestId);
    httpRequestDomain.setRemoteAddr(clientIpAddr);
    httpRequestDomain.setRequestTime(apiRequestTime);
    httpRequestDomain.setRequestUrl(requestUrl);
    httpRequestDomain.setRequestUri(requestUri);
    httpRequestDomain.setRequestMethod(requestMethod);
    httpRequestDomain.setParameter(stringOfParas);
    httpRequestDomain.setResponseBody(responseBody);
    httpRequestDomain.setTimestamp(startTime);
    httpRequestDomain.setSessionId(session.getId());
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
    String responseTime = String.valueOf(endTime - startTime);
    httpRequestDomain.setResponseTime(responseTime);
    transferKafkaService.sendMessage(env.getProperty("edge.gateway.topic.log"), objectMapper.writeValueAsString(httpRequestDomain));
  }
}
