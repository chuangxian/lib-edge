package libedge.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

/**
 * @author yrw
 */
@Component
@Slf4j
public class JwtService {

	@Value("${jwt.algorithm}")
	private String jwtAlgorithm;

	@Value("${jwt.secret.envDecryption}")
	private String envDecryption;

	@Value("${token.jwt.expire.seconds}")
	private Integer tokenExpire;

	public String encodeJwt(Map<String, String> userIdentityMap, String envEncryption)
					throws UnsupportedEncodingException {
		//得到面向service的密钥
		//把userIdentity内容加密
		Algorithm algorithm = getAlgorithm(jwtAlgorithm, envEncryption);
		Date nowDate = new Date();
		Date expire = DateUtils.addSeconds(nowDate, tokenExpire);
		JWTCreator.Builder builder = JWT.create()
						.withIssuedAt(new Date())
						.withExpiresAt(expire);
		userIdentityMap.forEach(builder::withClaim);
		return builder.sign(algorithm);
	}

	private Algorithm getAlgorithm(String algorithm, String secret) throws UnsupportedEncodingException {
		switch (algorithm) {
			case "HS256":
				return Algorithm.HMAC256(secret);
			default:
				return Algorithm.HMAC256(secret);
		}
	}
}
