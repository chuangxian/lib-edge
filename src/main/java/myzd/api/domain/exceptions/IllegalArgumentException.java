package myzd.api.domain.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Administrator on 2017/3/15.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class IllegalArgumentException extends Exception {
  private String argumentName;

  public IllegalArgumentException(String argumentName) {
    this.setArgumentName(argumentName);
  }

  @Data
  public static class IllegalArgumentExceptionApiDoc {
    private String argumentName;
    private String code;
  }
}
