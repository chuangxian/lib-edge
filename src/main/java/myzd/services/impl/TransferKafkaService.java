package myzd.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.IOException;
import java.util.Date;

/**
 * Created by zks on 18/07/2017.
 * 发送kafka消息的服务
 */
@Component
@Slf4j
public class TransferKafkaService {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private PrometheusEventHandler prometheusEventHandler;

  public void sendMessage(String topic, String message) throws IOException {
    log.debug("send kafka message. topic: {}, message: {}, date: {}", topic, message, new Date());
    try {
      ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(
        topic,
        objectMapper.writeValueAsString(ImmutableMap.of(
          "ts", String.valueOf(System.currentTimeMillis()),
          "message", message
        )));
      future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
        @Override
        public void onSuccess(SendResult<String, String> result) {
        }

        @Override
        public void onFailure(Throwable ex) {
          log.error("Fail to send message to message queue.", message, ex);
        }
      });
    } catch (JsonProcessingException e) {
      log.error("Unexpected parsing error when writing kafka message to message queue.", e);
    } catch (NullPointerException e) {
      log.error("send kafka message error.", e);
    }
    try {
      prometheusEventHandler.handle(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
