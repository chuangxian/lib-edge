package myzd.services.impl;

import lombok.extern.slf4j.Slf4j;
import myzd.annotations.FilterParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zks on 2017/9/28 11:21
 */
@Component
@Slf4j
public class FilterParamService {

  public Map<String, String[]> filterParam(Map<String, String[]> requestParam, FilterParam filterParam) {
    if (filterParam == null || !filterParam.enable()) {
      return requestParam;
    }
    Map<String, String[]> paramsMap = new HashMap<>();

    String[] includeArr = filterParam.include();
    String[] excludeArr = filterParam.exclude();

    //添加参数
    if (includeArr.length == 0) {
      paramsMap = requestParam;
    } else {
      Map<String, String[]> finalParamsMap = paramsMap;
      requestParam.forEach((String k, String[] v) -> {
        for (String includeParam :
          includeArr) {
          if (StringUtils.isNoneBlank(includeParam) && k.equals(includeParam)) {
            finalParamsMap.put(k, v);
          }
        }
      });
      paramsMap = finalParamsMap;
    }
    //过滤参数
    if (excludeArr.length == 0) {
      return paramsMap;
    } else {
      Map<String, String[]> finalParamsMap1 = paramsMap;
      paramsMap.forEach((String k, String[] v) -> {
        for (String excludeParam :
          excludeArr) {
          if (StringUtils.isNoneBlank(excludeParam) && k.equals(excludeParam)) {
            finalParamsMap1.remove(k);
          }
        }
      });
      return finalParamsMap1;
    }
  }

}
