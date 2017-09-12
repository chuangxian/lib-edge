package myzd.services.impl;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import lombok.extern.slf4j.Slf4j;
import myzd.domain.HttpRequestDomain;
import org.springframework.stereotype.Component;

/**
 * Created by Chen Baichuan on 19/07/2017.
 */
@Component
@Slf4j
public class PrometheusMetrics {

  private static final Counter requests = Counter.build().name("requests_total")
    .labelNames("request_id", "client_ip", "url", "uri", "method").help("requests_total").register();

  private static final Summary responseTime = Summary.build()
    .quantile(0.5, 0.05)
    .quantile(0.9, 0.01)
    .name("response_time")
    .labelNames("request_id", "client_ip", "url", "uri", "method").help("response_time").register();

  private static final Summary responseBody = Summary.build()
    .quantile(0.5, 0.05)
    .quantile(0.9, 0.01)
    .name("response_body")
    .labelNames("request_id", "client_ip", "url", "uri", "method").help("response_time").register();

  private static final Gauge avgOfResponseTime = Gauge.build().name("average_response_time")
    .labelNames("request_id", "client_ip", "url", "uri", "method").help("average_response_time").register();

  private static final Gauge avgOfResponseBody = Gauge.build().name("average_response_body")
    .labelNames("request_id", "client_ip", "url", "uri", "method").help("average_response_body").register();

  public void addPrometheusMetrics(HttpRequestDomain httpRequestDomain) {
    String requestId = httpRequestDomain.getRequestId();
    String clientIp = httpRequestDomain.getRemoteAddr();
    String requestUrl = httpRequestDomain.getRequestUrl();
    String requestUri = httpRequestDomain.getRequestUri();
    String requestMethod = httpRequestDomain.getRequestMethod();
//    String requestTime = httpRequestDomain.getRequestTime();
    String responseTimeHere = httpRequestDomain.getResponseTime();
    String responseBodyHere = httpRequestDomain.getResponseBody();

    requests.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).inc();
    responseTime.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).observe(Double.parseDouble(responseTimeHere));
    responseBody.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).observe(Double.parseDouble(responseBodyHere));
    avgOfResponseTime.labels(requestId, clientIp, requestUrl, requestUri, requestMethod)
      .set(responseTime.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).get().sum /
        responseTime.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).get().count);
    avgOfResponseBody.labels(requestId, clientIp, requestUrl, requestUri, requestMethod)
      .set(responseBody.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).get().sum /
        responseBody.labels(requestId, clientIp, requestUrl, requestUri, requestMethod).get().count);
  }
}
