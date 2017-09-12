package myzd.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zks on 2017/9/6.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestFlow {
  private int secondTimes;
  private int minuteTimes;
  private int hourTimes;
  private int dayTimes;
  private long timestamp;
}