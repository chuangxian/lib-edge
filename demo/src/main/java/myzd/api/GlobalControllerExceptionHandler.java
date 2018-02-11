package myzd.api;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * @author Created by Mike He on 2017/3/7.
 */
@Slf4j
@ControllerAdvice(basePackages = "myzd.api.controllers")
public class GlobalControllerExceptionHandler {

	public static Map<String, String> errorResponse(Throwable throwable) {
		Throwable cause = Throwables.getRootCause(throwable);
		String message = String.valueOf(cause.getMessage());
		return ImmutableMap.of("message", message);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Exception.class})
	public Map<String, String> handleException(Throwable e) {
		log.warn("HANDLED EXCEPTION: {}", e.getMessage(), e);
		return errorResponse(e);
	}

}
