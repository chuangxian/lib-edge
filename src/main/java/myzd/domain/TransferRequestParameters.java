package myzd.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zks on 2017/9/4.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferRequestParameters {
  private HttpServletRequest httpServletRequest;
  private HttpServletResponse httpServletResponse;
  private HandlerMethod handlerMethod;
  private String prefix;
  private String token;
  private TransferAuditor transferAuditor;
}
