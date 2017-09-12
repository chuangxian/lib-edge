package myzd.filter;

import lombok.extern.slf4j.Slf4j;
import myzd.domain.TransferAuditor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * Created by zks on 2017/8/1.
 * 获取request内容
 */
@Component
@Slf4j
public class TransferRequestFilter implements Filter {

  @Autowired
  private TransferAuditor transferAuditor;


  public void destroy() {
    // Nothing to do
  }

  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {
    //因为request body只能获取一次, 所以将request的内容存在transferAuditor中
    ResettableStreamHttpServletRequest wrappedRequest = new ResettableStreamHttpServletRequest(
      (HttpServletRequest) request);
    log.debug("do transfer request filter, get request content. method: {}", wrappedRequest.getMethod());
    String body = IOUtils.toString(wrappedRequest.getReader());
    transferAuditor.audit(wrappedRequest.getRequestURI(), wrappedRequest.getUserPrincipal(), body, wrappedRequest.getParameterMap());
    wrappedRequest.resetInputStream();
    chain.doFilter(wrappedRequest, response);
  }

  public void init(FilterConfig arg0) throws ServletException {
    // Nothing to do
  }

  private static class ResettableStreamHttpServletRequest extends
    HttpServletRequestWrapper {

    private byte[] rawData;
    private HttpServletRequest request;
    private ResettableServletInputStream servletStream;

    private ResettableStreamHttpServletRequest(HttpServletRequest request) {
      super(request);
      this.request = request;
      this.servletStream = new ResettableServletInputStream();
    }


    private void resetInputStream() {
      servletStream.stream = new ByteArrayInputStream(rawData);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      if (rawData == null) {
        rawData = IOUtils.toByteArray(this.request.getReader());
        servletStream.stream = new ByteArrayInputStream(rawData);
      }
      return servletStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
      if (rawData == null) {
        rawData = IOUtils.toByteArray(this.request.getReader());
        servletStream.stream = new ByteArrayInputStream(rawData);
      }
      return new BufferedReader(new InputStreamReader(servletStream));
    }


    private class ResettableServletInputStream extends ServletInputStream {
      private InputStream stream;

      @Override
      public int read() throws IOException {
        return stream.read();
      }

      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener listener) {

      }
    }
  }

}