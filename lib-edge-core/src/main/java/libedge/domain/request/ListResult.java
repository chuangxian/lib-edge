package libedge.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @param <T>
 * @author yrw
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListResult<T> {
	private List<T> list;
}
