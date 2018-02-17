package myzd.api.domain;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserInfo {
  @Size(min = 5, max = 30, message = "the length of the name should between 5-30!")
  private String name;
}
