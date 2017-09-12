package myzd.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.domain.HttpRequestDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by David on 19/07/2017.
 * 处理prometheus
 */
@Component
@Slf4j
public class PrometheusEventHandler {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private PrometheusMetrics prometheusMetrics;

  public void handle(String message) throws IOException {
    log.debug("prometheus message: {}", message);
    prometheusMetrics.addPrometheusMetrics(objectMapper.readValue(message, HttpRequestDomain.class));
  }

}
