package libedge.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @param <T>
 * @author yrw
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultWrapper<T> {
  private String code;
  private String message;
  private T data;

  @SuppressWarnings("unchecked")
  public ResultWrapper(T data) {
    this.code = "1000000";
    this.message = "OK";
    this.data = data;
  }
}
