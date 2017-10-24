package myzd.utils;

import lombok.extern.slf4j.Slf4j;
import myzd.domain.exceptions.GenericException;
import myzd.domain.request.ResultWrapper;
import retrofit2.Response;

/**
 * Created by zks on 2017/9/5.
 * RetrofitUtils
 */
@Slf4j
public class RetrofitUtils {
  private final static Integer RESPONSE_OK_CODE = 1000000;

  public static <T> T getResponseBody(Response<ResultWrapper<T>> response) throws GenericException {
    ResultWrapper<T> resultWrapper = response.body();
    if (response.isSuccessful() && resultWrapper != null) {
      if (resultWrapper.getCode() == RESPONSE_OK_CODE) {
        return resultWrapper.getData();
      }
      log.error("Services has some errors. resultWrapper: {}", resultWrapper);
      throw new GenericException(String.valueOf(resultWrapper.getCode()), resultWrapper.getMessage());
    }
    log.error("Request is not success. code: {}, message: {}, response: {}", response.code(), response.message(), response);
    throw new GenericException("1910001", String.format("请求出错, %s - %s", String.valueOf(response.code()), response.message()));
  }


}
