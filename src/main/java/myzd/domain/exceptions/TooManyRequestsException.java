package myzd.domain.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by zks on 2017/9/6.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TooManyRequestsException extends Exception {
  public TooManyRequestsException(String message) {
    super(message);
  }
}
