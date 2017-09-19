package myzd.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.GlobalNormalRequest;
import myzd.annotations.JwtAuthorization;
import myzd.annotations.NormalRequest;
import myzd.annotations.SessionAuthorization;
import myzd.domain.TransferAuditor;
import myzd.domain.TransferRequestParameters;
import myzd.domain.ValidateMessage;
import myzd.domain.exceptions.GenericException;
import myzd.utils.RequestHelper;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zks on 2017/8/2.
 * 透传服务
 */
@Component
@Slf4j
public class TransferRequest {

  @Autowired
  private TransferAuditor transferAuditor;
  @Autowired
  private OkHttpClient okHttpClient;
  @Autowired
  private Environment env;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ValidateMessage validateMessage;

  /**
   * 透传
   *
   * @param transferRequestParameters 参数
   */
  public void transferRequest(TransferRequestParameters transferRequestParameters) throws GenericException {
    log.info("transfer request.");
    HttpServletResponse response = transferRequestParameters.getHttpServletResponse();
    if (response.getStatus() != HttpStatus.OK.value()) {
      log.error("httpServletResponse status is not 200. skip transfer request. status: {}", response.getStatus());
      return;
    }
    if (validateMessage.getMessageMap() != null && validateMessage.getMessageMap().get("validateMessage") != null) {
      log.error("validateMessage is not null, skip transfer request. validateMessage: {}", validateMessage);
      return;
    }
    HttpServletRequest request = transferRequestParameters.getHttpServletRequest();
    HandlerMethod handlerMethod = transferRequestParameters.getHandlerMethod();
    String prefix = transferRequestParameters.getPrefix();
    String token = transferRequestParameters.getToken();
    transferAuditor = transferRequestParameters.getTransferAuditor();
    GlobalNormalRequest globalNormalRequest = transferRequestParameters.getGlobalNormalRequest();
    try {
      log.debug("method:{}", request.getMethod());
      String urlModel = getUrlModel(handlerMethod);
      URL clientUrl = getUri(handlerMethod, globalNormalRequest, urlModel, prefix);
      log.debug("clientUrl:{}", clientUrl);
      doRequestOkHttp(clientUrl.toURI(), handlerMethod.getMethod().getAnnotation(JwtAuthorization.class),
        handlerMethod.getMethod().getAnnotation(SessionAuthorization.class),
        request, response, token, handlerMethod.getMethod().getReturnType());
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param handlerMethod handlerMethod
   * @return String
   */
  private String getUrlModel(HandlerMethod handlerMethod) {
    String urlModel = null;
    Annotation[] annotations = handlerMethod.getMethod().getDeclaredAnnotations();
    for (Annotation annotation : annotations) {
      if (annotation.annotationType() == GetMapping.class) {
        GetMapping getMapping = (GetMapping) annotation;
        //拿到url
        if (getMapping.value().length != 0) {
          urlModel = getMapping.value()[0];
        } else {
          urlModel = getMapping.path()[0];
        }
      } else if (
        annotation.annotationType() == PostMapping.class) {
        PostMapping postMapping = (PostMapping) annotation;
        //拿到url
        if (postMapping.value().length != 0) {
          urlModel = postMapping.value()[0];
        } else {
          urlModel = postMapping.path()[0];
        }
      } else if (
        annotation.annotationType() == PutMapping.class) {
        PutMapping putMapping = (PutMapping) annotation;
        //拿到url
        if (putMapping.value().length != 0) {
          urlModel = putMapping.value()[0];
        } else {
          urlModel = putMapping.path()[0];
        }
      } else if (
        annotation.annotationType() == DeleteMapping.class) {
        DeleteMapping deleteMapping = (DeleteMapping) annotation;
        //拿到url
        if (deleteMapping.value().length != 0) {
          urlModel = deleteMapping.value()[0];
        } else {
          urlModel = deleteMapping.path()[0];
        }
      }
    }
    return urlModel;
  }

  /**
   * 获取带参数的url链接
   *
   * @return URI
   * @throws URISyntaxException exception
   */
  private URL getUri(HandlerMethod handlerMethod, GlobalNormalRequest globalNormalRequest, String urlModel, String prefix)
    throws URISyntaxException, UnsupportedEncodingException, MalformedURLException, GenericException {
    String requestUri = transferAuditor.getRequestUri().replace(prefix, "");
    log.debug("requestUri:{}", requestUri);
    String finalRequestUrl = requestUri;
    NormalRequest normalRequest = handlerMethod.getMethod().getAnnotation(NormalRequest.class);
    if (normalRequest != null && StringUtils.isNoneBlank(normalRequest.clientUrl())) {
      finalRequestUrl = normalRequest.clientUrl();
    }
    if (requestUri.startsWith("/")) {
      finalRequestUrl = requestUri.substring(1);
    }
    String requestHost = null;
    if (globalNormalRequest != null && StringUtils.isNoneBlank(globalNormalRequest.clientHost())) {
      requestHost = env.getProperty(globalNormalRequest.clientHost());
    }
    if (normalRequest != null && StringUtils.isNoneBlank(normalRequest.clientHost())) {
      requestHost = env.getProperty(normalRequest.clientHost());
    }
    if (StringUtils.isBlank(requestHost)) {
      throw new GenericException("1910010", "client host in normal request annotation must not be null.");
    }
    URL url = new URL(requestHost);
    String path = url.getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    //get a map which have many <key,value> like <"{orderNumber}","123">
    Map<String, String> paramMap = getPathParamMap(urlModel, requestUri);
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      finalRequestUrl = finalRequestUrl.replace(entry.getKey(), entry.getValue());
    }
    if (finalRequestUrl.startsWith("/")) {
      finalRequestUrl = finalRequestUrl.substring(1);
    }
    log.debug("finalRequestUri:{}", finalRequestUrl);
    HttpUrl.Builder builder = getBuilder(url, path, finalRequestUrl);
    return builder.build().url();
  }

  /**
   * 返回一个builder（已经构造好url)
   *
   * @param url             url
   * @param path            path
   * @param finalRequestUrl finalRequestUrl
   * @return HttpUrl.Builder
   */
  private HttpUrl.Builder getBuilder(URL url, String path, String finalRequestUrl) {
    HttpUrl.Builder builder = new HttpUrl.Builder();
    builder.scheme(url.getProtocol());
    builder.host(url.getHost());
    builder.addPathSegments(path);
    builder.addPathSegments(finalRequestUrl);
    // builder.addPathSegments(id);
    transferAuditor.getParams().forEach((key, value) -> {
      log.debug("request params: {}: {}", key, value);
      builder.addQueryParameter(key, value[0]);
    });
    return builder;
  }

  /**
   * return a map which have many <key,value> like <"{orderNumber}","123">
   *
   * @param urlModel   urlModel
   * @param requestUri requestUri
   */
  private Map<String, String> getPathParamMap(String urlModel, String requestUri) {
    if (urlModel.startsWith("/")) {
      urlModel = urlModel.substring(1);
    }
    if (requestUri.startsWith("//")) {
      requestUri = requestUri.substring(2);
    }
    if (requestUri.startsWith("/")) {
      requestUri = requestUri.substring(1);
    }
    log.debug("mapUri:{}", urlModel);
    log.debug("requestUri:{}", requestUri);
    String[] urlModelArr = urlModel.split("/");
    String[] requestUriArr = requestUri.split("/");
    Map<String, String> pathParamMap = new HashMap<>();
    for (int i = 0; i < urlModelArr.length; i++) {
      if (i < requestUriArr.length) {
        if (urlModelArr[i].startsWith("{") && urlModelArr[i].endsWith("}") && !urlModelArr[i].equals(requestUriArr[i])) {
          pathParamMap.put(urlModelArr[i], requestUriArr[i]);
        }
      }
    }
    return pathParamMap;
  }

  /**
   * 构造一个requestOkHttp请求并执行
   *
   * @param clientUri        clientUri
   * @param jwtAuthorization JwtAuthorization
   * @param request          request
   * @param response         response
   * @param token            token
   * @param returnType       returnType
   * @throws IOException      IOException
   * @throws GenericException GenericException
   */
  private void doRequestOkHttp(URI clientUri, JwtAuthorization jwtAuthorization, SessionAuthorization sessionAuthorization,
                               HttpServletRequest request, HttpServletResponse response, String token, Class<?> returnType
  ) throws IOException, GenericException {
    String method = request.getMethod();
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), transferAuditor.getBody());
    Request clientRequest = new Request.Builder()
      .url(clientUri.toURL())
      .header("Content-Type", StringUtils.isNoneBlank(request.getContentType()) ?
        request.getContentType() : "application/json")
      .header("X-Real-IP", RequestHelper.getRealIp(request))    //加入客户端真实ip。
      .header("Accept", request.getHeader("Accept")).build();
    //如果不存在JwtAuthorization或者SessionAuthorization配置，则不传token给内部服务
    if (jwtAuthorization != null || sessionAuthorization != null) {
      clientRequest = clientRequest.newBuilder().header("Authorization", token).build();
    }
    if ("POST".equals(method)) {
      clientRequest = clientRequest.newBuilder().post(body).build();
    } else if ("PUT".equals(method)) {
      clientRequest = clientRequest.newBuilder().put(body).build();
    } else if ("DELETE".equals(method)) {
      clientRequest = clientRequest.newBuilder().delete(body).build();
    }
    Response clientResponse = okHttpClient.newCall(clientRequest).execute();
    response.setHeader("Content-Type", clientResponse.header("Content-Type"));
    ResponseBody responseBody = clientResponse.body();
    if (validateMessage.getMessageMap() == null) {
      validateMessage.setMessageMap(new HashMap<>());
    }
    Map<String, String> messageMap = validateMessage.getMessageMap();
    if (messageMap.get("validateMessage") != null) {
      //如果validateMessage的get("validateMessage")中有string，则说明此参数数值或者格式错误
      String json = objectMapper.writeValueAsString(ImmutableMap.of("code", "1911001", "message", messageMap.get("validateMessage")));
      IOUtils.copy(new ByteArrayInputStream(json.getBytes()), response.getWriter());
    } else {
      if (responseBody != null) {
        //如果接口的返回值不为void，则responseBody的内容应该依据返回值内部的字段进行过滤
        if (returnType.getSimpleName().equals("void")) {
          log.debug("返回值类型: {}", returnType.getSimpleName());
          IOUtils.copy(responseBody.byteStream(), response.getWriter());
        } else {
          IOUtils.copy(new ByteArrayInputStream
            (objectMapper.writeValueAsString(objectMapper.readValue(responseBody.string(), returnType)).getBytes()), response.getWriter());
        }
      } else {
        log.info("{} response is null. {}", method, clientUri);
      }
    }
  }
}
