package myzd.domain.request;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ResultWrapper<T> {
  private int code;
  private String message;
  private T data;

  @SuppressWarnings("unchecked")
  public ResultWrapper(T data) {
    this.code = 1000000;
    this.message = "OK";
    this.data = data;
  }

  public ResultWrapper(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }
}
