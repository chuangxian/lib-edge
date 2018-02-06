package myzd.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.*;
import myzd.domain.exceptions.GenericException;
import myzd.domain.request.ListResult;
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
  private JwtService jwtService;

  @Autowired
  public PipeService(Environment env, OkHttpClient okHttpClient, ObjectMapper objectMapper, JwtService jwtService) {
		this.env = env;
		this.okHttpClient = okHttpClient;
		this.objectMapper = objectMapper;
		this.jwtService = jwtService;
  }

  public Object penetrate(HttpServletRequest request, HttpServletResponse response, Method method, List requestBody, Class controllerClass, Map<String, String[]> filterParam) throws GenericException {
	try {
	  String requestMethod = request.getMethod();
	  log.debug("penetrate request method: {}", requestMethod);
	  if ("OPTIONS".equals(requestMethod)) {
			return null;
	  }

	  //得到@RequestMapping注解里的路径
	  String mappingUrl = getMappingUrl(requestMethod, method);
	  //
	  URL clientUrl = getRequestUri(request, method, controllerClass, mappingUrl, filterParam);
	  log.debug("clientUrl:{}", clientUrl);

	  //发起请求，返回response
	  return doRequestOkHttp(clientUrl.toURI(), request, response, requestBody, controllerClass, method);
	} catch (IOException | URISyntaxException e) {
	  e.printStackTrace();
	}
	return null;
  }

  /**
	 * 得到@RequestMapping注解里的路径
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

  	//得到controller类上的@PipeConfig
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
	private Object doRequestOkHttp(URI clientUri, HttpServletRequest request, HttpServletResponse response, List requestBody, Class controllerClass, Method method)
					throws IOException, URISyntaxException, GenericException {
		//把http头部的token解码
		String token = String.format("Bearer %s", getRequestAuthorizationToken(request, controllerClass, method));
		log.debug("request token: {}", token);

		//构建一个request请求
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), toByte(requestBody));
		Request clientRequest = new Request.Builder()
						.url(clientUri.toURL())
						.header("Content-Type", StringUtils.isNoneBlank(request.getContentType()) ?
										request.getContentType() : "application/json")
						.header("X-Real-IP", RequestHelper.getRealIp(request))    //加入客户端真实ip。
						.header("Accept", request.getHeader("Accept")).build();

		//如果不存在@Authentication，则不传token给内部服务
		if (method.getAnnotation(Authentication.class) != null) {
			clientRequest = clientRequest.newBuilder().header("Authorization", token).build();
		}

		//得到请求的方法
		String requestMethod = request.getMethod();

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
			//封装成ResultWrapper，错误信息写在message里
			return new ResultWrapper<String>(1000000, clientResponse.body().toString(), null);
		} else {
			//其他结果，原样返回
			setHeader("Location", response, clientResponse);
			setHeader("Content-Type", response, clientResponse);
			setHeader("Content-Disposition", response, clientResponse);
			response.setStatus(clientResponse.code());
		}
		return null;
	}

	private void setHeader(String header, HttpServletResponse response, Response clientResponse){
  	String headerContent = null;
  	if((headerContent = clientResponse.header(header))!=null && StringUtils.isNoneBlank(headerContent)){
  		response.setHeader(header, headerContent);
		}
	}

	private Object filterValue(Response clientResponse, HttpServletResponse response, Method method) throws IOException, GenericException{
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ResponseBody clientResponseBody = clientResponse.body();

		//分两种情况：json和文件
		if(clientResponse.header("Content-Type").contains("application/json")){
			//如果是ResultWrapper，返回值过滤
			JavaType javaType = getJavaTypeByReturnType(method);
			return objectMapper.readValue(clientResponseBody.bytes(), javaType);
		}else {
			//如果不是json，直接返回
			if(response == null){
				throw new GenericException("1000000", "参数中不存在HttpServletResponse");
			}

			//替换头部内容
			SetHeader setHeader = null;
			if((setHeader = method.getAnnotation(SetHeader.class))!=null){
				for(String value:setHeader.value()){
					String[] header = value.split(":", 2);
					response.setHeader(header[0], header[1]);
				}
			}

			IOUtils.copy(clientResponseBody.byteStream(), response.getOutputStream());
			return null;
		}
	}

  private JavaType getJavaTypeByReturnType(Method method) throws GenericException{
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
				throw new GenericException("1000000", "返回格式应该是ResultWrapper");
			}
			String type = typeStrings[i-1];
			log.debug("字符串数组当前值: {}", typeStrings[i]);
			if(globalType instanceof ParameterizedType){
				globalType = ((ParameterizedType)globalType).getActualTypeArguments()[0];
			}
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
  private String getRequestAuthorizationToken(HttpServletRequest request, Class controllerClass, Method method)
					throws UnsupportedEncodingException, GenericException{

		Authentication authentication = method.getAnnotation(Authentication.class) == null?
						(Authentication)controllerClass.getAnnotation(Authentication.class):
						method.getAnnotation(Authentication.class);

		//若@authorization为空, 则不需要token
		if(authentication == null){
			return null;
		}

		//若@authorization不为空得到头部内容
		String authorizationToken = request.getHeader("Authorization");
		log.debug("authorizationToken: {}", authorizationToken);

		Map<String, String> userIdentityMap = new HashMap<>();

		if(authentication.mode() == Authentication.Mode.JWT){
			userIdentityMap = jwtService.decodeJwt(authorizationToken, authentication);
		} else{
			//信息放在session里面了
			HttpSession httpSession = request.getSession();
			Enumeration<String> sessionAttributeNames = httpSession.getAttributeNames();
			for (Enumeration e = sessionAttributeNames; e.hasMoreElements(); ) {
				String key = e.nextElement().toString();
				String value = String.valueOf(httpSession.getAttribute(key));
				userIdentityMap.put(key, value);
			}
		}

		//得到了userIdentityMap，加密后返回
		return jwtService.encodeJwt(userIdentityMap, authentication);
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