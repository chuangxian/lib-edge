package libedge.domain.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @param <T>
 * @author yrw
 */
@Data
@NoArgsConstructor
public class ListResult<T> {
	private List<T> list;
}
