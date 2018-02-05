package myzd.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.Authentication;
import myzd.domain.exceptions.GenericException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.tools.java.Environment;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtService {

	@Autowired
	private org.springframework.core.env.Environment env;

	public Map<String, String> decodeJwt(String authorizationToken, Authentication authentication)
					throws GenericException, UnsupportedEncodingException {

		String token = null;
		if (StringUtils.isNoneBlank(authorizationToken) && authorizationToken.startsWith("Bearer ")) {
			token = authorizationToken.substring(7);
		} else {
			throw new GenericException("1000000", "token格式不正确");
		}

		Map<String, String> userIdentityMap = new HashMap<>();

		//解密header内容
		if (token != null && StringUtils.isNoneBlank(token)) {
			//得到面向edge的密钥
			String envDecryption = env.resolvePlaceholders(authentication.envDecryption());
			//解密的算法
			Algorithm algorithm = getAlgorithm(authentication, envDecryption);
			log.debug("token: {}", token);

			DecodedJWT body = JWT.require(algorithm).acceptIssuedAt(300).build().verify(token);
			body.getClaims().forEach((key, value) -> userIdentityMap.put(key, value.asString()));
		}
		return userIdentityMap;
	}

	public String encodeJwt(Map<String, String> userIdentityMap, Authentication authentication)
					throws UnsupportedEncodingException{
		//得到面向service的密钥
		String envEncryption = env.resolvePlaceholders(authentication.envEncryption());

		//把userIdentity内容加密
		log.debug("userIdentityMap: {}", userIdentityMap);

		Algorithm algorithm = getAlgorithm(authentication, envEncryption);
		JWTCreator.Builder builder = JWT.create();
		userIdentityMap.forEach(builder::withClaim);
		builder.withIssuedAt(new Date());
		return builder.sign(algorithm);
	}

	private Algorithm getAlgorithm(Authentication authentication, String secret) throws UnsupportedEncodingException {
		switch (authentication.algorithm()){
			case HS256: return Algorithm.HMAC256(secret);
			default: return Algorithm.HMAC256(secret);
		}
	}
}
