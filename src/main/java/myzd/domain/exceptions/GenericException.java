package myzd.domain.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericException extends Exception {
	private String code;

	public GenericException(String code, String message) {
		super(message);
		this.setCode(code);
	}
}
