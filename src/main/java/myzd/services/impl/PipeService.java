package myzd.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.Filename;
import myzd.annotations.JwtAuthorization;
import myzd.annotations.PipeConfig;
import myzd.annotations.SessionAuthorization;
import myzd.domain.exceptions.GenericException;
import myzd.domain.request.ListResult;
import myzd.domain.request.PagedResult;
import myzd.domain.request.ResultWrapper;
import myzd.utils.RequestHelper;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
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

  @Value("${filename}")
  private String filename;

  @Autowired
  public PipeService(Environment env, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
		this.env = env;
		this.okHttpClient = okHttpClient;
		this.objectMapper = objectMapper;
  }

  public Object penetrate(HttpServletRequest request, HttpServletResponse response, Method method, List requestBody, Class controllerClass, Map<String, String[]> filterParam) throws GenericException {
	try {
	  String requestMethod = request.getMethod();
	  log.debug("penetrate request method: {}", requestMethod);
	  if ("OPTIONS".equals(requestMethod)) {
			return null;
	  }
	  String mappingUrl = getMappingUrl(requestMethod, method);
	  URL clientUrl = getRequestUri(request, method, controllerClass, mappingUrl, filterParam);
	  log.debug("clientUrl:{}", clientUrl);
	  //得到返回内容
	  return doRequestOkHttp(clientUrl.toURI(), request, response, requestBody, controllerClass, method);
	} catch (IOException | URISyntaxException e) {
	  e.printStackTrace();
	}
	return null;
  }

  /**
   * @param method method
   * @return String
   */
  public String getMappingUrl(String requestMethod, Method method) {
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
  public URL getRequestUri(HttpServletRequest request, Method method, Class controllerClass, String mappingUrl, Map<String, String[]> filterParam)
					throws URISyntaxException, UnsupportedEncodingException, MalformedURLException, GenericException {

  	//得到controller类上的@PipeConfig.clientHost
  	PipeConfig controllerPipeConfig = (PipeConfig) controllerClass.getAnnotation(PipeConfig.class);

  	//得到http请求内的uri
  	String requestUri = request.getRequestURI();
		log.debug("requestUri:{}", requestUri);

		String finalRequestUrl = mappingUrl;

		//得到方法上的@PipeConfig.clientUrl（选填）
		PipeConfig pipeConfig = method.getAnnotation(PipeConfig.class);
		if (pipeConfig != null && StringUtils.isNoneBlank(pipeConfig.clientUrl())) {
			finalRequestUrl = env.resolvePlaceholders(pipeConfig.clientUrl());
		}

		//得到要透传的服务的host
		String requestHost = null;
		if (controllerPipeConfig != null && StringUtils.isNoneBlank(controllerPipeConfig.clientHost())) {
			requestHost = env.resolvePlaceholders(controllerPipeConfig.clientHost());
		}
		if (pipeConfig != null && StringUtils.isNoneBlank(pipeConfig.clientHost())) {
			requestHost = env.resolvePlaceholders(controllerPipeConfig.clientHost());
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

		//得到最终url
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
  public HttpUrl.Builder getBuilder(URL url, String path, String finalRequestUrl, Map<String, String[]> filterParam) {
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
  public Map<String, String> getPathParamMap(String urlModel, String requestUri) {
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
  public Object doRequestOkHttp(URI clientUri, HttpServletRequest request, HttpServletResponse response, List requestBody, Class controllerClass, Method method) throws IOException, URISyntaxException, GenericException {
		//把http头部的token解码
		String token = String.format("Bearer %s", getRequestAuthorizationToken(request, controllerClass, method));
		log.debug("request token: {}", token);

		//查看是否有jwt注解或者session注解
		JwtAuthorization jwtAuthorization = method.getAnnotation(JwtAuthorization.class);
		SessionAuthorization sessionAuthorization = method.getAnnotation(SessionAuthorization.class);

		//request method
		String requestMethod = request.getMethod();

		//构建一个request请求
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), toByte(requestBody));
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

		//根据response的code区分
		if (clientResponse.code() == 200) {
			return filterValue(clientResponse, response, method);
		} else if (clientResponse.code() >= 500) {
			//封装成ResultWrapper
			ResultWrapper resultWrapper = new ResultWrapper();
			resultWrapper.setCode(1000000);
			//把异常信息写到message里
			resultWrapper.setMessage(clientResponse.body().toString());
		} else {
			//其他结果，原样返回
			setHeader("Location", response, clientResponse);
			setHeader("Content-Type", response, clientResponse);
			setHeader("Content-Disposition", response, clientResponse);
			response.setStatus(clientResponse.code());
		}
		return null;
	}

	public void setHeader(String header, HttpServletResponse response, Response clientResponse){
  	String headerContent = null;
  	if((headerContent = clientResponse.header(header))!=null && StringUtils.isNoneBlank(headerContent)){
  		response.setHeader(header, headerContent);
		}
	}

  public Object filterValue(Response clientResponse, HttpServletResponse response, Method method) throws IOException{
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ResponseBody clientResponseBody = clientResponse.body();

		if(clientResponse.header("Content-Type").contains("application/json")){
			//如果是ResultWrapper，返回值过滤
			JavaType javaType = getJavaTypeByReturnType(method);
			return objectMapper.readValue(clientResponseBody.bytes(), javaType);
		}else {
			//如果不是json，直接返回
			//得到文件名
			if(method.getAnnotation(Filename.class)!=null){
				filename = env.resolvePlaceholders(method.getAnnotation(Filename.class).value());
			}
			response.setHeader("Content-disposition", "attachment;filename="+filename);
			IOUtils.copy(clientResponseBody.byteStream(), response.getOutputStream());
			return null;
		}
	}

  public JavaType getJavaTypeByReturnType(Method method) {
		ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
		log.debug("parameterizedType={}", parameterizedType);

		//把嵌套的类型分割开来
		String[] typeStrings = parameterizedType.getTypeName().split("<");

		if (typeStrings[0].contains("ResultWrapper")) {
			log.debug("类型是ResultWrapper<>");

			//globalType可能是基本类型,POJO,PagedResult,ListResult
			Type globalType = parameterizedType.getActualTypeArguments()[0];

			int i = 0;
			for (i = 1; i < typeStrings.length; i++) {
				log.debug("字符串数组当前值：{} ", typeStrings[i]);

				if (typeStrings[i].contains("ListResult")) {
					globalType = ((ParameterizedType) globalType).getActualTypeArguments()[0];
					log.debug("第" + i + "层，ListResult的泛型值为: {}", globalType);
				} else if (typeStrings[i].contains("PagedResult")) {
					globalType = ((ParameterizedType) globalType).getActualTypeArguments()[0];
					log.debug("第" + i + "层，PagedResult泛型值为: {}", globalType);
				} else if (typeStrings[i].contains("List")) {
					globalType = ((ParameterizedType) globalType).getActualTypeArguments()[0];
					log.debug("第" + i + "层，List的泛型值为: {}", globalType);
				} else {
					//当此处不再是ListResult或PagedResult或List的时候，证明已经到末尾
					break;
				}
			}

			log.debug("first load");
			JavaType javaType = null;
			if(i-1<0){
				throw new RuntimeException("返回格式应该是ResultWrapper");
			}
			String type = typeStrings[i-1];
			log.debug("字符串数组当前值: {}", typeStrings[i]);

			if (type.contains("ListResult")) {
				javaType = objectMapper.getTypeFactory().constructParametricType(ListResult.class, (Class<?>) globalType);
			} else if (type.contains("PagedResult")) {
				javaType = objectMapper.getTypeFactory().constructParametricType(PagedResult.class, (Class<?>) globalType);
			} else if (type.contains("List")) {
				javaType = objectMapper.getTypeFactory().constructParametricType(List.class, (Class<?>) globalType);
			}

			log.debug("second load");
			for (int j = typeStrings.length - 3; j > 0; j--) {
				if (typeStrings[j].contains("PagedResult")) {
					log.debug("嵌入PagedResult:");
					javaType = objectMapper.getTypeFactory().constructParametricType(PagedResult.class, javaType);
				} else if (typeStrings[j].contains("ListResult")) {
					log.debug("嵌入ListResult:");
					javaType = objectMapper.getTypeFactory().constructParametricType(ListResult.class, javaType);
				} else {
					log.debug("嵌入List:");
					javaType = objectMapper.getTypeFactory().constructParametricType(List.class, javaType);
				}
			}

			//最后将结果嵌套嵌入ResultWrapper
			log.debug("final load");
			if(javaType == null){
				javaType = objectMapper.getTypeFactory().constructParametricType(ResultWrapper.class, (Class<?>)globalType);
			}else {
				javaType = objectMapper.getTypeFactory().constructParametricType(ResultWrapper.class, javaType);
			}
			return javaType;
		}
		return null;
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
  public String getRequestAuthorizationToken(HttpServletRequest request, Class controllerClass, Method method) throws UnsupportedEncodingException {
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

	private byte[] toByte(List requestBodyList) {
  	if(requestBodyList.size() == 0){return new byte[0];}
		StringBuilder strb = new StringBuilder();
		for (Object o : requestBodyList) {
			try {
				strb.append(objectMapper.writeValueAsString(o));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return new String(strb).getBytes();
	}
}