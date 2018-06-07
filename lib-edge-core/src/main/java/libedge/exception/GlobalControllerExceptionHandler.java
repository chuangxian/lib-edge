package libedge.exception;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import libedge.domain.exceptions.GenericException;
import libedge.domain.exceptions.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

/**
 * @author Created by Mike He on 2017/3/7.
 */
@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {

	public static Map<String, String> errorResponse(Throwable throwable) {
		Throwable cause = Throwables.getRootCause(throwable);
		String message = String.valueOf(cause.getMessage());
		if (GenericException.class.isInstance(throwable)) {
			return ImmutableMap.of("message", message, "code", ((GenericException) throwable).getCode());
		}
		if(throwable instanceof UndeclaredThrowableException){
			Throwable exception = ((UndeclaredThrowableException) throwable).getUndeclaredThrowable();
			if(exception instanceof GenericException){
				return ImmutableMap.of("message", message, "code", ((GenericException) exception).getCode());
			}
		}
		log.error("Expected error. ", throwable);
		return ImmutableMap.of("message", message, "code", "1998001");
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler({Exception.class})
	public Map<String, String> handleException(Throwable e) {
		log.warn("HANDLED EXCEPTION: {}", e.getMessage(), e);
		return errorResponse(e);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler({IllegalArgumentException.class})
	public Map<String, String> handleException(IllegalArgumentException e) {
		e.printStackTrace();
		return ImmutableMap.of("code", "1911001", "message", e.getMessage());
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
	@ExceptionHandler({TooManyRequestsException.class})
	public Map<String, String> handleException(TooManyRequestsException e) {
		return ImmutableMap.of("code", "1911002", "message", e.getMessage());
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler({ConstraintViolationException.class})
	public Map<String, String> handleException(ConstraintViolationException e) {
		StringBuilder message = new StringBuilder();
		for (ConstraintViolation<?> s : e.getConstraintViolations()) {
			message.append(s.getMessage());
		}
		log.warn("参数校验失败: {}", message.toString());
		return ImmutableMap.of("code", "1911001", "message", message.toString());
	}

  @ResponseBody
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  @ExceptionHandler({IOException.class})
  public Object handleException(IOException e) {
    String searchStr = "Broken pipe";
    if (StringUtils.containsIgnoreCase(ExceptionUtils.getRootCauseMessage(e), searchStr)) {
      log.error("Broken pipe", e);
      return null;
    } else {
      return new HttpEntity<>(e.getMessage());
    }
  }

}
