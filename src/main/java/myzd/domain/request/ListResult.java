package myzd.domain.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yrw
 * @since 2/10/2018
 */
@Data
@NoArgsConstructor
public class ListResult<T> {
	private List<T> list;
}
