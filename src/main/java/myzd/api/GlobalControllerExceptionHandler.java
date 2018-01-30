package myzd.api;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import myzd.api.domain.exceptions.GenericException;
import myzd.api.domain.exceptions.IllegalArgumentException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * Created by Mike He on 2017/3/7.
 */
@Slf4j
@ControllerAdvice(basePackages = "myzd.api.controllers")
public class GlobalControllerExceptionHandler {

  public static Map<String, String> errorResponse(Throwable throwable) {
    Throwable cause = Throwables.getRootCause(throwable);
    String message = String.valueOf(cause.getMessage());
    if (GenericException.class.isInstance(throwable)) {
      return ImmutableMap.of("message", message, "code", ((GenericException) throwable).getCode());
    }
    return ImmutableMap.of("message", message);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler({Exception.class})
  public Map<String, String> handleException(Throwable e) {
    log.warn("HANDLED EXCEPTION: {}", e.getMessage(), e);
    return errorResponse(e);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({IllegalArgumentException.class})
  public Map<String, String> handleException(IllegalArgumentException e) {
    return ImmutableMap.of("code", "e_illegal_argument", "argumentName", e.getArgumentName());
  }

}
