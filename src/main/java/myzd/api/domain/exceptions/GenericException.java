package myzd.api.domain.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Administrator on 2017/3/15.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class GenericException extends Exception {
  private String code;

  public GenericException(String code, String message) {
    super(message);
    this.setCode(code);
  }

  @Data
  public static class GenericExceptionApiDoc {
    private String message;
    private String code;
  }
}
