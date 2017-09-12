package myzd.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.annotations.RequestFlowHandle;
import myzd.domain.HttpRequestDomain;
import myzd.domain.RequestFlow;
import myzd.domain.exceptions.TooManyRequestsException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by zks on 2017/9/6.
 */
@Component
@Slf4j
public class RequestFlowHandlerService {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 单个用户,IP 流量控制
   *
   * @param httpRequestDomain httpRequestDomain: http info
   * @param requestFlowHandle requestFlowHandle: 流控注解
   * @throws TooManyRequestsException too many requests exception
   */
  public void handleRequestFlow(HttpRequestDomain httpRequestDomain, RequestFlowHandle requestFlowHandle) throws TooManyRequestsException {
    int secondTimes = requestFlowHandle.secondTimes();
    int minuteTimes = requestFlowHandle.minuteTimes();
    int hourTimes = requestFlowHandle.hourTimes();
    int dayTimes = requestFlowHandle.dayTimes();
    //根据注解中设定的值, 进行流量检查
    authorizeRequest(httpRequestDomain, secondTimes, minuteTimes, hourTimes, dayTimes);
  }

  /**
   * 分别检查IP, sessionId 的访问量
   *
   * @param httpRequestDomain httpRequestDomain
   * @param secondTimes       configured second times
   * @param minuteTimes       configured minute times
   * @param hourTimes         configured hour times
   * @param dayTimes          configured day times
   * @throws TooManyRequestsException too many requests exception
   */
  private void authorizeRequest(HttpRequestDomain httpRequestDomain, int secondTimes, int minuteTimes, int hourTimes, int dayTimes) throws TooManyRequestsException {
    String clientIp = httpRequestDomain.getRemoteAddr();
    String sessionId = httpRequestDomain.getSessionId();
    String requestUri = httpRequestDomain.getRequestUri();
    String requestMethod = httpRequestDomain.getRequestMethod();
    long requestTimestamp = httpRequestDomain.getTimestamp();
    verifiedRequest(clientIp, requestUri, requestMethod, requestTimestamp, secondTimes, minuteTimes, hourTimes, dayTimes);
    verifiedRequest(sessionId, requestUri, requestMethod, requestTimestamp, secondTimes, minuteTimes, hourTimes, dayTimes);
  }

  /**
   * 生成redis存储的key, 根据key获取redis存的信息, 进行验证
   *
   * @param authorizationId  authorizationId: ip or sessionId
   * @param uri              request uri
   * @param method           request method
   * @param requestTimestamp request timestamp
   * @param secondTimes      configured second times
   * @param minuteTimes      configured minute times
   * @param hourTimes        configured hour times
   * @param dayTimes         configured day times
   * @throws TooManyRequestsException too many requests exception
   */
  private void verifiedRequest(String authorizationId, String uri, String method, long requestTimestamp, int secondTimes, int minuteTimes, int hourTimes, int dayTimes) throws TooManyRequestsException {
    String key = String.format("%s-%s-%s", uri, method, authorizationId);
    log.debug("key: {}", key);
    verifiedRedisInfo(key, requestTimestamp, secondTimes, minuteTimes, hourTimes, dayTimes);
  }


  /**
   * 流控
   *
   * @param key              key: redis key
   * @param requestTimestamp request timestamp
   * @param secondTimes      configured second times
   * @param minuteTimes      configured minute times
   * @param hourTimes        configured hour times
   * @param dayTimes         configured day times
   * @throws TooManyRequestsException too many requests exception
   */
  private void verifiedRedisInfo(String key, long requestTimestamp, int secondTimes, int minuteTimes, int hourTimes, int dayTimes) throws TooManyRequestsException {
    //取出数据, 若不为空, 判断请求时间间隔, 满足则请求次数+1, 若为空,则新建
    String requestFlowStr = redisTemplate.opsForValue().get(key);
    RequestFlow requestFlow;
    try {
      if (StringUtils.isNoneBlank(requestFlowStr)) {
        //检查访问次数是否超出限制
        requestFlow = objectMapper.readValue(requestFlowStr, RequestFlow.class);
        //若果在间隔时间内, 则访问次数+1, 若不在, 则重置次数为1
        requestFlow.setSecondTimes(requestTimestamp - requestFlow.getTimestamp() < 1000 ? requestFlow.getSecondTimes() + 1 : 1);
        requestFlow.setMinuteTimes(requestTimestamp - requestFlow.getTimestamp() < 60000 ? requestFlow.getMinuteTimes() + 1 : 1);
        requestFlow.setHourTimes(requestTimestamp - requestFlow.getTimestamp() < 3600000 ? requestFlow.getHourTimes() + 1 : 1);
        requestFlow.setDayTimes(requestTimestamp - requestFlow.getTimestamp() < 86400000 ? requestFlow.getDayTimes() + 1 : 1);
        if ((secondTimes > 0 && requestFlow.getSecondTimes() > secondTimes) ||
          (minuteTimes > 0 && requestFlow.getMinuteTimes() > minuteTimes) ||
          (hourTimes > 0 && requestFlow.getHourTimes() > hourTimes) ||
          (dayTimes > 0 && requestFlow.getDayTimes() > dayTimes)
          ) {
          log.debug("too many exception. limit: {}, {}, {}, {}; fact: {}, {}, {}, {}",
            secondTimes, minuteTimes, hourTimes, dayTimes,
            requestFlow.getSecondTimes(), requestFlow.getMinuteTimes(), requestFlow.getHourTimes(), requestFlow.getDayTimes()
          );
          throw new TooManyRequestsException("too many exception.");
        }
      } else {
        requestFlow = new RequestFlow();
        requestFlow.setSecondTimes(1);
        requestFlow.setMinuteTimes(1);
        requestFlow.setHourTimes(1);
        requestFlow.setDayTimes(1);
      }
      requestFlow.setTimestamp(requestTimestamp);
      redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(requestFlow));
      log.debug("requestFlow: {}", requestFlow);
    } catch (IOException e) {
      log.error("Analysis request flow redis info error.", e);
    }
  }

}
