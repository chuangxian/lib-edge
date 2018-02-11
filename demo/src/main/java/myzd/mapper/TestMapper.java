package myzd.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author yrw
 * @since 2/10/2018
 */
@Mapper
@Repository
public interface TestMapper {

	/**
	 * select一条数据
	 * @return String
	 */
	@Select("select username from user where id = 1")
	String selectMethod();
}
