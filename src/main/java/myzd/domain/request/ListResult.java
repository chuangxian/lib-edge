package myzd.domain.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ListResult<T> {
	private List<T> list;
}
