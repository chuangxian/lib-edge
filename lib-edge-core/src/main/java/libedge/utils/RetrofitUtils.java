package libedge.utils;

import libedge.domain.exceptions.GenericException;
import libedge.domain.request.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

/**
 * @author Created by zks on 2017/9/5.
 * RetrofitUtils
 */
@Slf4j
public class RetrofitUtils {
  private final static String RESPONSE_OK_CODE = "1000000";

  public static <T> T getResponseBody(Response<ResultWrapper<T>> response) throws GenericException {
    ResultWrapper<T> resultWrapper = response.body();
    if (response.isSuccessful() && resultWrapper != null) {
      if (RESPONSE_OK_CODE.equals(resultWrapper.getCode())) {
        return resultWrapper.getData();
      }
      log.error("Services has some errors. resultWrapper: {}", resultWrapper);
      throw new GenericException(resultWrapper.getCode(), resultWrapper.getMessage());
    }
    log.error("Request is not success. code: {}, message: {}, response: {}", response.code(), response.message(), response);
    throw new GenericException("1910001", String.format("请求出错, %s - %s", String.valueOf(response.code()), response.message()));
  }


}
