package myzd.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.domain.RoleAuthority;
import myzd.domain.exceptions.GenericException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Created by zks on 2017/7/27.
 * 负责检查用户权限
 * 配置示例:
 * role.authority.config=[{"role":"agent","authority":["/doctor-app/*","/ims/*"]},{"role":"patient","authority":[]}]
 */
@Slf4j
@Component
public class CheckAuthority {

  @Autowired
  private ObjectMapper objectMapper;
  private String roleAuthorityConfig;
  @Autowired
  private Environment environment;

  /**
   * 根据访问路径和用户角色判断用户是否有权访问
   *
   * @param path     访问路径
   * @param userRole 用户角色
   * @throws GenericException exception
   */
  public void checkRole(String path, String userRole) throws GenericException {
    path = path.replace("//", "/");
    log.debug("path: {}", path);
    if (StringUtils.isNoneBlank(userRole)) {
      try {
        if (StringUtils.isBlank(roleAuthorityConfig)) {
          roleAuthorityConfig = environment.getProperty("role.authority.config");
        }
        List<RoleAuthority> list = objectMapper.readValue(roleAuthorityConfig, new TypeReference<List<RoleAuthority>>() {
        });
        log.debug("role list: {}", list);
        boolean check = false;
        for (RoleAuthority roleAuthority :
          list) {
          if (userRole.toLowerCase().endsWith(roleAuthority.getRole().toLowerCase())) {
            if (roleAuthority.getAuthority() != null && roleAuthority.getAuthority().size() > 0) {
              for (String authority :
                roleAuthority.getAuthority()) {
                String urlAuthority = authority;
                if (authority.contains("*")) {
                  urlAuthority = authority.substring(0, authority.indexOf("*"));
                }
                if (path.startsWith(urlAuthority)) {
                  check = true;
                }
              }
            }
          }
        }
        if (!check) {
          log.info("user does not have permission. role: {}, path: {}", userRole, path);
          throw new GenericException("1511006", "Permission denied");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      log.info("user role is null. ");
    }
  }
}
