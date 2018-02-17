package libedge.utils;


import libedge.domain.exceptions.GenericException;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * @author Created by Chen baichuan 27/7/2017.
 * Request Utils
 */
public class RequestHelper {

	private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	/**
	 * 获取真实IP
	 *
	 * @param request HttpServletRequest
	 * @return ip
	 * @throws GenericException IP不正确
	 */
	public static String getRealIp(HttpServletRequest request) throws GenericException {
		String forwardedIp = request.getHeader("X-Forwarded-For");
		String clientRealIp;
		//检查X-Forwarded-For是否为空。
		if (forwardedIp == null || forwardedIp.length() == 0 || forwardedIp.equalsIgnoreCase("unknown")) {
			forwardedIp = request.getHeader("Proxy-Client-IP");
		}
		if (forwardedIp == null || forwardedIp.length() == 0 || forwardedIp.equalsIgnoreCase("unknown")) {
			forwardedIp = request.getHeader("WL-Proxy-Client-IP");
		}
		if (forwardedIp == null || forwardedIp.length() == 0 || forwardedIp.equalsIgnoreCase("unknown")) {
			clientRealIp = request.getRemoteAddr();
		} else {
			clientRealIp = forwardedIp.split(",")[0];
			if (PATTERN.matcher(clientRealIp).matches()) {
				//检查X-Forwarded-For中的源客户端ip是否为一个正确的ip地址。
				return clientRealIp.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : clientRealIp;
			} else {
				throw new GenericException("1910007", "IP不正确，X-Forwarded-For可能被伪造为不正确的ip地址。");
			}
		}
		return clientRealIp.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : clientRealIp;
	}
}
