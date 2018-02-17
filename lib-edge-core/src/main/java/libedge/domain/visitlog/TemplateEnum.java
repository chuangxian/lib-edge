package libedge.domain.visitlog;

/**
 * @author Created by liuqian on 2017/9/14 9:36.
 */
public interface TemplateEnum {
	String MESSAGE_SOURCE = "$message_source";
	String REMOTE_HOST = "$remote_host";
	String REQUEST_METHOD = "$request_method";
	String RESPONSE_TIME = "$response_time";
	String RESPONSE_STATUS = "$response_status";
	String RESPONSE_BODY_SIZE = "$response_body_size";
	String REQUEST_URI = "$request_uri";
	String SERVICE_NAME = "$service_name";
}
