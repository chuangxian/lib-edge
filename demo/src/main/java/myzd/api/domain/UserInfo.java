package myzd.api.domain;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserInfo {
  @Size(min = 1, max = 5, message = "mess")
  private String name;
}
