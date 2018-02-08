package myzd.api.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

/**
 * @author zks
 * @since 2018/1/23 11:31
 */
@Data
@NoArgsConstructor
public class Test1 {

  private Long id;
  @Size(min = 1, max = 5, message = "mess")
  private String name;
}
