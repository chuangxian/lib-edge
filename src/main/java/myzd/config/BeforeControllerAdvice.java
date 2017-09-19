package myzd.config;

import lombok.extern.slf4j.Slf4j;
import myzd.annotations.FilterParam;
import myzd.domain.TransferAuditor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mike He on 4/28/2017.
 */
@Component
@Aspect
@Slf4j
public class BeforeControllerAdvice {

  @Autowired
  private TransferAuditor transferAuditor;

  /**
   * 对配置参数进行过滤，并把auditor中params(是一个map)更新
   *
   * @param joinPoint joinPoint
   */
  private void filter(JoinPoint joinPoint) {
    Method action = ((MethodSignature) joinPoint.getSignature()).getMethod();
    FilterParam filterParam = action.getDeclaredAnnotation(FilterParam.class);
    if (filterParam == null || !filterParam.switchButton()) {
      return;
    }
    String[] includeArr = filterParam.include().split(",");
    for (String s : includeArr) {
      log.debug("include: {}", s);
    }
    String[] excludeArr = filterParam.exclude().split(",");
    for (String s : excludeArr) {
      log.debug("exclude: {}", s);
    }
    Map<String, String[]> paramsMap = new HashMap<>();
    transferAuditor.getParams().forEach((String k, String[] v) -> {
      boolean flag = true;
      if (includeArr[0].equals("") && excludeArr[0].equals("")) {  //预防filter开关打开时，include和exclude都没有配置
      } else if (!includeArr[0].equals("")) {
        for (String str : includeArr) {
          if (k.equals(str.replaceAll(" ", ""))) {
            paramsMap.put(k, v);
            break;
          }
        }
      } else {
        for (String str : excludeArr) {
          if (k.equals(str.replaceAll(" ", ""))) {
            flag = false;
            break;
          }
        }
        if (flag) {
          paramsMap.put(k, v);
        }
      }
    });
    transferAuditor.setParams(paramsMap);
  }

  @Before("execution(public * myzd.api.controllers.*.*(..))")
  public void filtBeforeHandling(JoinPoint joinPoint) throws Exception {
    Map<String, String[]> paramsMap = transferAuditor.getParams();
    log.debug("before filter request params:");
    paramsMap.forEach((key, value) -> log.debug("key: {}, value: {}", key, value));
    filter(joinPoint);  //filter
    log.debug("after filter request params:");
    transferAuditor.getParams().forEach((key, value) -> log.debug("key: {}, value: {}", key, value));
  }

}
