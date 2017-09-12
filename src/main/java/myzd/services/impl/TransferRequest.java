package myzd.services.impl;

import lombok.extern.slf4j.Slf4j;
import myzd.annotations.NormalRequest;
import myzd.domain.TransferAuditor;
import myzd.domain.TransferRequestParameters;
import myzd.domain.exceptions.GenericException;
import myzd.utils.RequestHelper;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
  private Environment envHelper;

  /**
   * 透传
   *
   * @param transferRequestParameters 参数
   */
  public void transferRequest(TransferRequestParameters transferRequestParameters) {
    log.info("transfer request.");
    HttpServletResponse response = transferRequestParameters.getHttpServletResponse();
    if (response.getStatus() != HttpStatus.OK.value()) {
      log.warn("httpServletResponse status is not 200. skip transfer request. status: {}", response.getStatus());
      return;
    }
    HttpServletRequest request = transferRequestParameters.getHttpServletRequest();
    HandlerMethod handlerMethod = transferRequestParameters.getHandlerMethod();
    String prefix = transferRequestParameters.getPrefix();
    String token = transferRequestParameters.getToken();
    transferAuditor = transferRequestParameters.getTransferAuditor();
    try {
      NormalRequest normalRequest = handlerMethod.getMethod().getAnnotation(NormalRequest.class);
      URL clientUrl = getUri(normalRequest, request, prefix);
      log.debug("clientUrl: {}", clientUrl);
      log.debug("requestMethod: {}", request.getMethod());
      if ("GET".equals(request.getMethod())) {
        doGetRequestOkHttp(clientUrl.toURI(), request, response, token);
      } else if ("POST".equals(request.getMethod())) {
        doPostRequestOkHttp(clientUrl.toURI(), request, response, token);
      } else if ("PUT".equals(request.getMethod())) {
        doPutRequestOkHttp(clientUrl.toURI(), request, response, token);
      } else if ("DELETE".equals(request.getMethod())) {
        doDeleteRequestOkHttp(clientUrl.toURI(), request, response, token);
      } else {
        log.debug("unknown request method.", request.getMethod());
      }
    } catch (IOException | URISyntaxException | GenericException e) {
      e.printStackTrace();
    }
  }

  /**
   * OkHttpClient get
   */
  private void doGetRequestOkHttp(URI clientUri, HttpServletRequest request, HttpServletResponse response, String token) throws IOException, GenericException {
    Request clientRequest = new Request.Builder()
      .url(clientUri.toURL())
      .header("Authorization", token)
      .header("Content-Type", StringUtils.isNoneBlank(request.getContentType()) ? request.getContentType() : "application/json")
      .header("Accept", request.getHeader("Accept"))
      .header("X-Real-IP", RequestHelper.getRealIp(request))    //加入客户端真实ip。
      .build();
    Response clientResponse = okHttpClient.newCall(clientRequest).execute();
    response.setHeader("Content-Type", clientResponse.header("Content-Type"));
    ResponseBody responseBody = clientResponse.body();
    if (responseBody != null) {
      IOUtils.copy(responseBody.byteStream(), response.getOutputStream());
    } else {
      log.info("response body is null. uri: {}", clientUri);
    }
    log.debug("{}", response.getHeaderNames());
  }

  /**
   * OkHttpClient post
   */
  private void doPostRequestOkHttp(URI clientUri, HttpServletRequest request, HttpServletResponse response, String token) throws IOException, GenericException {
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), transferAuditor.getBody());
    Request clientRequest = new Request.Builder()
      .url(clientUri.toURL())
      .header("Authorization", token)
      .header("Content-Type", StringUtils.isNoneBlank(request.getContentType()) ? request.getContentType() : "application/json")
      .header("Accept", request.getHeader("Accept"))
      .header("X-Real-IP", RequestHelper.getRealIp(request))    //加入客户端真实ip。
      .post(body)
      .build();
    Response clientResponse = okHttpClient.newCall(clientRequest).execute();
    response.setHeader("Content-Type", clientResponse.header("Content-Type"));
    ResponseBody responseBody = clientResponse.body();
    if (responseBody != null) {
      IOUtils.copy(responseBody.byteStream(), response.getOutputStream());
    } else {
      log.info("post response is null.", clientUri);
    }
  }

  /**
   * OkHttpClient put
   */
  private void doPutRequestOkHttp(URI clientUri, HttpServletRequest request, HttpServletResponse response, String token) throws IOException, GenericException {
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), transferAuditor.getBody());
    Request clientRequest = new Request.Builder()
      .url(clientUri.toURL())
      .header("Authorization", token)
      .header("Content-Type", StringUtils.isNoneBlank(request.getContentType()) ? request.getContentType() : "application/json")
      .header("Accept", request.getHeader("Accept"))
      .header("X-Real-IP", RequestHelper.getRealIp(request))    //加入客户端真实ip。
      .put(body)
      .build();
    Response clientResponse = okHttpClient.newCall(clientRequest).execute();
    response.setHeader("Content-Type", clientResponse.header("Content-Type"));
    ResponseBody responseBody = clientResponse.body();
    if (responseBody != null) {
      IOUtils.copy(responseBody.byteStream(), response.getOutputStream());
    } else {
      log.info("post response is null.", clientUri);
    }
  }

  /**
   * OkHttpClient delete
   */
  private void doDeleteRequestOkHttp(URI clientUri, HttpServletRequest request, HttpServletResponse response, String token) throws IOException, GenericException {
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), transferAuditor.getBody());
    Request clientRequest = new Request.Builder()
      .url(clientUri.toURL())
      .header("Authorization", token)
      .header("Content-Type", StringUtils.isNoneBlank(request.getContentType()) ? request.getContentType() : "application/json")
      .header("Accept", request.getHeader("Accept"))
      .header("X-Real-IP", RequestHelper.getRealIp(request))    //加入客户端真实ip。
      .delete(body)
      .build();
    Response clientResponse = okHttpClient.newCall(clientRequest).execute();
    response.setHeader("Content-Type", clientResponse.header("Content-Type"));
    ResponseBody responseBody = clientResponse.body();
    if (responseBody != null) {
      IOUtils.copy(responseBody.byteStream(), response.getOutputStream());
    } else {
      log.info("response body is null. uri: {}", clientUri);
    }
  }

  /**
   * 获取带参数的url链接
   *
   * @param normalRequest normalRequest
   * @param request       request
   * @return URI
   * @throws URISyntaxException exception
   */
  private URL getUri(NormalRequest normalRequest, HttpServletRequest request, String prefix) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException, GenericException {
    String requestUri = transferAuditor.getRequestUri().replace(prefix, "");
    if (StringUtils.isNoneBlank(normalRequest.clientUrl())) {
      requestUri = normalRequest.clientUrl();
    }
    String finalRequestUrl = requestUri;
    if (requestUri.startsWith("/")) {
      finalRequestUrl = requestUri.substring(1);
    }
    String requestHost = null;
    if (StringUtils.isNoneBlank(normalRequest.clientHost())) {
      requestHost = envHelper.getProperty(normalRequest.clientHost());
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
    HttpUrl.Builder builder = new HttpUrl.Builder();
    builder.scheme(url.getProtocol());
    builder.host(url.getHost());
    builder.addPathSegments(path);
    builder.addPathSegments(finalRequestUrl);
    transferAuditor.getParams().forEach((key, value) -> builder.addQueryParameter(key, value[0]));
    return builder.build().url();
  }

}
