package myzd.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by admin on 2017/7/27.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPermission {

  private String role;
  private List<String> authority;

}
