package myzd.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import myzd.domain.FlowControl;
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
public class FlowControlService {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 单个用户,IP 流量控制
   *
   * @param clientIp         访问IP
   * @param sessionId        sessionId
   * @param requestUri       requestUri
   * @param requestMethod    Method
   * @param requestTimestamp requestTimestamp
   * @param flowControl      流控策略
   * @throws TooManyRequestsException
   */
  public void flowController(String clientIp,
                             String sessionId,
                             String requestUri,
                             String requestMethod,
                             long requestTimestamp,
                             myzd.annotations.FlowControl flowControl) throws TooManyRequestsException {
    log.debug("flow controller. clientIp: {}, requestUri: {}, requestMethod: {}, requestTimestamp: {}", clientIp, requestUri, requestMethod, requestTimestamp);
    //根据注解中设定的值, 进行流量检查
    if (flowControl == null) {
      log.debug("request have not request flow annotation. requestUri: {}", requestUri);
      return;
    }
    verifiedRequest(clientIp, requestUri, requestMethod, requestTimestamp, flowControl);
    verifiedRequest(sessionId, requestUri, requestMethod, requestTimestamp, flowControl);
  }

  /**
   * 生成redis存储的key, 根据key获取redis存的信息, 进行验证
   *
   * @param authorizationId  authorizationId: ip or sessionId
   * @param uri              request uri
   * @param method           request method
   * @param requestTimestamp request timestamp
   * @param flowControl      流控策略
   * @throws TooManyRequestsException too many requests exception
   */
  private void verifiedRequest(String authorizationId, String uri, String method, long requestTimestamp, myzd.annotations.FlowControl flowControl) throws TooManyRequestsException {
    String key = String.format("%s-%s-%s", uri, method, authorizationId);
    log.debug("key: {}", key);
    verifiedRedisInfo(key, requestTimestamp, flowControl);
  }


  /**
   * 流控
   *
   * @param key              redis 存的key值 eg: uri-METHOD-ip
   * @param requestTimestamp 请求的时间戳
   * @param flowControl      流控策略
   * @throws TooManyRequestsException
   */
  private void verifiedRedisInfo(String key, long requestTimestamp, myzd.annotations.FlowControl flowControl) throws TooManyRequestsException {
    int secondTimes = flowControl.secondTimes();
    int minuteTimes = flowControl.minuteTimes();
    int hourTimes = flowControl.hourTimes();
    int dayTimes = flowControl.dayTimes();
    //取出数据, 若不为空, 判断请求时间间隔, 满足则请求次数+1, 若为空,则新建
    String requestFlowStr = redisTemplate.opsForValue().get(key);
    FlowControl flowContorl;
    try {
      if (StringUtils.isNoneBlank(requestFlowStr)) {
        //检查访问次数是否超出限制
        flowContorl = objectMapper.readValue(requestFlowStr, FlowControl.class);
        //若果在间隔时间内, 则访问次数+1, 若不在, 则重置次数为1
        flowContorl.setSecondTimes(requestTimestamp - flowContorl.getTimestamp() < 1000 ? flowContorl.getSecondTimes() + 1 : 1);
        flowContorl.setMinuteTimes(requestTimestamp - flowContorl.getTimestamp() < 60000 ? flowContorl.getMinuteTimes() + 1 : 1);
        flowContorl.setHourTimes(requestTimestamp - flowContorl.getTimestamp() < 3600000 ? flowContorl.getHourTimes() + 1 : 1);
        flowContorl.setDayTimes(requestTimestamp - flowContorl.getTimestamp() < 86400000 ? flowContorl.getDayTimes() + 1 : 1);
        if ((secondTimes > 0 && flowContorl.getSecondTimes() > secondTimes) ||
          (minuteTimes > 0 && flowContorl.getMinuteTimes() > minuteTimes) ||
          (hourTimes > 0 && flowContorl.getHourTimes() > hourTimes) ||
          (dayTimes > 0 && flowContorl.getDayTimes() > dayTimes)
          ) {
          log.debug("too many exception. limit: {}, {}, {}, {}; fact: {}, {}, {}, {}",
            secondTimes, minuteTimes, hourTimes, dayTimes,
            flowContorl.getSecondTimes(), flowContorl.getMinuteTimes(), flowContorl.getHourTimes(), flowContorl.getDayTimes()
          );
          throw new TooManyRequestsException("too many requests exception.");
        }
      } else {
        flowContorl = new FlowControl();
        flowContorl.setSecondTimes(1);
        flowContorl.setMinuteTimes(1);
        flowContorl.setHourTimes(1);
        flowContorl.setDayTimes(1);
      }
      flowContorl.setTimestamp(requestTimestamp);
      redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(flowContorl));
      log.debug("flowContorl: {}", flowContorl);
    } catch (IOException e) {
      log.error("Analysis request flow redis info error.", e);
    }
  }

}
