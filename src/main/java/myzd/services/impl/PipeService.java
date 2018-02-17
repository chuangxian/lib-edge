package myzd.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.JwtAuthorization;
import myzd.annotations.PipeConfig;
import myzd.annotations.SessionAuthorization;
import myzd.domain.exceptions.GenericException;
import myzd.domain.request.PagedResult;
import myzd.domain.request.ResultWrapper;
import myzd.utils.RequestHelper;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;

/**
 * @author zks
 * 透传服务
 */
@Component
@Slf4j
public class PipeService {

  private final Environment env;
  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;

  private final static String SERVER_CONTEXT_PATH = "server.context.path";

  @Autowired
  public PipeService(Environment env, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
    this.env = env;
    this.okHttpClient = okHttpClient;
    this.objectMapper = objectMapper;
  }

  public Object penetrate(HttpServletRequest request, Method method, Class controllerClass, Map<String, String[]> filterParam) throws GenericException {
    try {
      String requestMethod = request.getMethod();
      log.debug("penetrate request method: {}", requestMethod);
      if ("OPTIONS".equals(requestMethod)) {
        return null;
      }
      String mappingUrl = getMappingUrl(requestMethod, method);
      URL clientUrl = getRequestUri(request, method, controllerClass, mappingUrl, filterParam);
      log.debug("clientUrl:{}", clientUrl);
      return doRequestOkHttp(clientUrl.toURI(), request, controllerClass, method);
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @param method method
   * @return String
   */
  private String getMappingUrl(String requestMethod, Method method) {
    String[] mappingValue;
    switch (requestMethod) {
      case "POST":
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        mappingValue = postMapping.value().length != 0 ? postMapping.value() : postMapping.path();
        break;
      case "PUT":
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        mappingValue = putMapping.value().length != 0 ? putMapping.value() : putMapping.path();
        break;
      case "DELETE":
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        mappingValue = deleteMapping.value().length != 0 ? deleteMapping.value() : deleteMapping.path();
        break;
      default:
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        mappingValue = getMapping.value().length != 0 ? getMapping.value() : getMapping.path();
        break;
    }
    return mappingValue[0];
  }

  /**
   * 获取带参数的url链接
   *
   * @return URI
   * @throws URISyntaxException exception
   */
  private URL getRequestUri(HttpServletRequest request, Method method, Class controllerClass, String mappingUrl, Map<String, String[]> filterParam)
    throws URISyntaxException, UnsupportedEncodingException, MalformedURLException, GenericException {
    PipeConfig controllerPipeConfig = (PipeConfig) controllerClass.getAnnotation(PipeConfig.class);
    String requestUri = request.getRequestURI();
    log.debug("requestUri:{}", requestUri);
    String finalRequestUrl = mappingUrl;
    PipeConfig pipeConfig = method.getAnnotation(PipeConfig.class);
    if (pipeConfig != null && StringUtils.isNoneBlank(pipeConfig.clientUrl())) {
      finalRequestUrl = pipeConfig.clientUrl();
    }
    String serverContextPath = env.getProperty(SERVER_CONTEXT_PATH);
    if (StringUtils.isNotBlank(serverContextPath) && requestUri.startsWith(serverContextPath)) {
      finalRequestUrl = requestUri.substring(serverContextPath.length());
    }
    String requestHost = null;
    if (controllerPipeConfig != null && StringUtils.isNoneBlank(controllerPipeConfig.clientHost())) {
      requestHost = env.getProperty(controllerPipeConfig.clientHost());
    }
    if (pipeConfig != null && StringUtils.isNoneBlank(pipeConfig.clientHost())) {
      requestHost = env.getProperty(pipeConfig.clientHost());
    }
    if (requestHost == null) {
      throw new GenericException("1911003", "透传Host不能为空");
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
    Map<String, String> paramMap = getPathParamMap(mappingUrl, requestUri);
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      finalRequestUrl = finalRequestUrl.replace(entry.getKey(), entry.getValue());
    }
    if (finalRequestUrl.startsWith("/")) {
      finalRequestUrl = finalRequestUrl.substring(1);
    }
    log.debug("finalRequestUri:{}", finalRequestUrl);
    HttpUrl.Builder builder = getBuilder(url, path, finalRequestUrl, filterParam);
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
  private HttpUrl.Builder getBuilder(URL url, String path, String finalRequestUrl, Map<String, String[]> filterParam) {
    HttpUrl.Builder builder = new HttpUrl.Builder();
    builder.scheme(url.getProtocol());
    builder.host(url.getHost());
    builder.addPathSegments(path);
    builder.addPathSegments(finalRequestUrl);
    if (url.getPort() > 0) {
      builder.port(url.getPort());
    }
    filterParam.forEach((key, value) -> {
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
    int startIndex = 0;
    //若requestUri与mapping中定义的路径长度不同, 则比较第一个相同的字符, 为requestUri比较的起点
    if (urlModelArr.length != requestUriArr.length) {
      for (int i = 0; i < requestUriArr.length; i++) {
        if (urlModelArr[0].equals(requestUriArr[i])) {
          startIndex = i;
        }
      }
    }
    for (int i = 0; i < urlModelArr.length; i++) {
      int j = startIndex + i;
      if (j < requestUriArr.length) {
        if (urlModelArr[i].startsWith("{") && urlModelArr[i].endsWith("}") && !urlModelArr[i].equals(requestUriArr[j])) {
          String paramValue = requestUriArr[j];
          if (urlModelArr[i].startsWith("{") && urlModelArr[i].endsWith("}") && !urlModelArr[i].equals(paramValue)) {
            try {
              // 如果参数值为空或encode过, 则直接透传, 否则参数需要encode
              // 为了使参数encode出错不影响程序流程, catch encode的异常
              if (StringUtils.isBlank(paramValue) || !paramValue.equals(URLDecoder.decode(paramValue, "UTF-8"))) {
                pathParamMap.put(urlModelArr[i], paramValue);
              } else {
                pathParamMap.put(urlModelArr[i], URLEncoder.encode(paramValue, "UTF-8"));
              }
            } catch (UnsupportedEncodingException e) {
              log.error("encode request params error. param: {}.{}", paramValue, e);
            }
          }
        }
      }
    }
    return pathParamMap;
  }

  /**
   * 构造一个requestOkHttp请求并执行
   *
   * @param clientUri clientUri
   * @param request   request
   * @param method    method
   * @throws IOException      IOException
   * @throws GenericException GenericException
   */
  private Object doRequestOkHttp(URI clientUri, HttpServletRequest request, Class controllerClass, Method method) throws IOException, GenericException {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //token
    String token = String.format("Bearer %s", getRequestAuthorizationToken(request, controllerClass, method));
    log.debug("request token: {}", token);
    //authorization annotations
    JwtAuthorization jwtAuthorization = method.getAnnotation(JwtAuthorization.class);
    SessionAuthorization sessionAuthorization = method.getAnnotation(SessionAuthorization.class);
    //request method
    String requestMethod = request.getMethod();
    //request body
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), IOUtils.toByteArray(request.getReader()));
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
    if ("POST".equals(requestMethod)) {
      clientRequest = clientRequest.newBuilder().post(body).build();
    } else if ("PUT".equals(requestMethod)) {
      clientRequest = clientRequest.newBuilder().put(body).build();
    } else if ("DELETE".equals(requestMethod)) {
      clientRequest = clientRequest.newBuilder().delete(body).build();
    }
    Response clientResponse = okHttpClient.newCall(clientRequest).execute();
    ResponseBody responseBody = clientResponse.body();
    if (responseBody != null) {
      //如果接口的返回值不为void，则responseBody的内容应该依据返回值内部的字段进行过滤
      Class<?> returnType = method.getReturnType();
      if ("void".equals(returnType.getSimpleName())) {
        log.debug("返回值类型: {}", returnType.getSimpleName());
        return responseBody.string();
      } else {
        //过滤参数信息并装配
        if (!method.getReturnType().getSimpleName().contains("ResultWrapper")) {
          log.debug("不是ResultWrapper");
          return objectMapper.readValue(responseBody.string(), method.getReturnType());
        }
        //当返回值类型存在多层嵌套时
        JavaType javaType = getJavaTypeByReturnType(method);
        return objectMapper.readValue(responseBody.string(), javaType);
      }
    } else {
      log.info("{} response is null. {}", requestMethod, clientUri);
    }
    return null;
  }

  private JavaType getJavaTypeByReturnType(Method method) {
    ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
    log.debug("parameterizedType={}", parameterizedType);
    String[] typeStrings = parameterizedType.getTypeName().split("<");
    //-------------------------如果没有ResultWrapper嵌套
    log.debug("进入处理逻辑");
    if ((!typeStrings[1].contains("List") && !typeStrings[1].contains("PagedResult"))) {
      log.debug("类型如ResultWrapper<Users>");
      return objectMapper.getTypeFactory().constructParametricType(ResultWrapper.class, (Class<?>) parameterizedType.getActualTypeArguments()[0]);
    }
    //---------------------------目前只剩List和PagedResult嵌套
    log.debug("取得最内层Class过程");
    Type globalType = parameterizedType.getActualTypeArguments()[0];
    for (int i = 1; i < typeStrings.length - 1; i++) {
      log.debug("字符串数组当前值：{} ", typeStrings[i]);
      if (!typeStrings[i].contains("List") && !typeStrings[i].contains("PagedResult")) {
        break;//当此处不再是List或ResultWrapper的时候，证明已经到末尾
      }
      if (typeStrings[i].contains("List")) {
        globalType = ((ParameterizedTypeImpl) globalType).getActualTypeArguments()[0];
        log.debug("list的泛型值为: {}", globalType);
        continue;
      }
      globalType = ((ParameterizedType) globalType).getActualTypeArguments()[0];
      log.debug("非List的泛型值为: {}", globalType);
    }
    log.debug("第一次装入:");
    log.debug("字符串数组当前值: {}", typeStrings[typeStrings.length - 2]);
    JavaType javaType = typeStrings[typeStrings.length - 2].contains("List") ?
      objectMapper.getTypeFactory().constructParametricType(List.class, (Class<?>) globalType) :
      objectMapper.getTypeFactory().constructParametricType(PagedResult.class, (Class<?>) globalType);
    //最后装入阶段
    log.debug("最后装入阶段:");
    for (int i = typeStrings.length - 3; i >= 1; i--) {
      if (typeStrings[i].contains("PagedResult")) {
        log.debug("嵌入PagedWrapper:");
        javaType = objectMapper.getTypeFactory().constructParametricType(ResultWrapper.class, javaType);
        continue;
      }
      log.debug("嵌入List:");
      javaType = objectMapper.getTypeFactory().constructParametricType(List.class, javaType);
    }
    //最后将结果嵌套嵌入ResultWrapper
    log.debug("嵌入ResultWrapper:");
    javaType = objectMapper.getTypeFactory().constructParametricType(ResultWrapper.class, javaType);
    return javaType;
  }

  /**
   * 解密, 加密token信息
   *
   * @param request         request
   * @param controllerClass controller
   * @param method          method
   * @return token
   * @throws UnsupportedEncodingException UnsupportedEncodingException
   */
  private String getRequestAuthorizationToken(HttpServletRequest request, Class controllerClass, Method method) throws UnsupportedEncodingException {
    JwtAuthorization jwtAuthorization = method.getAnnotation(JwtAuthorization.class);
    SessionAuthorization sessionAuthorization = method.getAnnotation(SessionAuthorization.class);
    String authorizationToken = request.getHeader("Authorization");
    log.debug("authorizationToken: {}", authorizationToken);
    String token = null;
    if (StringUtils.isNoneBlank(authorizationToken) && authorizationToken.startsWith("Bearer ")) {
      token = authorizationToken.substring(7);
    }
    //若两个authorization注解都为空, 则不需要token
    if (jwtAuthorization == null && sessionAuthorization == null) {
      return token;
    }
    PipeConfig controllerConfig = (PipeConfig) controllerClass.getAnnotation(PipeConfig.class);
    PipeConfig actionConfig = method.getAnnotation(PipeConfig.class);
    Map<String, String> userIdentityMap = new HashMap<>();
    //若jwtAuthorization不为空则需要解密header内容
    if (jwtAuthorization != null && token != null && StringUtils.isNoneBlank(token)) {
      //解密
      String envDecryption = StringUtils.isNoneBlank(controllerConfig.envDecryption()) ? controllerConfig.envDecryption() : actionConfig.envDecryption();
      Algorithm algorithm = Algorithm.HMAC256(env.getProperty(envDecryption));
      log.debug("token: {}", token);
      DecodedJWT body = JWT.require(algorithm).acceptIssuedAt(300).build().verify(token);
      body.getClaims().forEach((key, value) -> userIdentityMap.put(key, value.asString()));
    }
    if (sessionAuthorization != null) {
      HttpSession httpSession = request.getSession();
      Enumeration<String> sessionAttributeNames = httpSession.getAttributeNames();
      for (Enumeration e = sessionAttributeNames; e.hasMoreElements(); ) {
        String key = e.nextElement().toString();
        String value = String.valueOf(httpSession.getAttribute(key));
        userIdentityMap.put(key, value);
      }
    }
    //加密
    String envEncryption = StringUtils.isNoneBlank(controllerConfig.envEncryption()) ? controllerConfig.envEncryption() : actionConfig.envEncryption();
    Algorithm algorithm = Algorithm.HMAC256(env.getProperty(envEncryption));
    JWTCreator.Builder builder = JWT.create();
    log.debug("userIdentityMap: {}", userIdentityMap);
    userIdentityMap.forEach(builder::withClaim);
    builder.withIssuedAt(new Date());
    return builder.sign(algorithm);
  }
}