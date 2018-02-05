package myzd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface AuthMapper {
	public List<String> getAuthorizaion(Map<String, String> condition);

}
