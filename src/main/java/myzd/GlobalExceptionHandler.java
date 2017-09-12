package myzd;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import myzd.domain.exceptions.GenericException;
import myzd.domain.exceptions.TooManyRequestsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;


@Slf4j
@ControllerAdvice(basePackages = {"myzd.api.controllers"})
public class GlobalExceptionHandler {

  public static Map<String, String> errorResponse(Throwable throwable) {
    Throwable cause = Throwables.getRootCause(throwable);
    String message = String.valueOf(cause.getMessage());
    if (GenericException.class.isInstance(throwable)) {
      return ImmutableMap.of("message", message, "code", ((GenericException) throwable).getCode());
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
    return ImmutableMap.of("code", "1911001", "message", e.getMessage());
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  @ExceptionHandler({TooManyRequestsException.class})
  public Map<String, String> handleException(TooManyRequestsException e) {
    return ImmutableMap.of("code", "1911002", "message", e.getMessage());
  }
}
