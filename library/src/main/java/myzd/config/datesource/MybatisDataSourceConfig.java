package myzd.config.datesource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author yrw
 * 2/9/2018
 */
@Configuration
@MapperScan(basePackages = {"myzd.authmapper"}, sqlSessionFactoryRef = "mybatisSqlSessionFactory")
public class MybatisDataSourceConfig {

	@Bean(name="authDataSource")
	@ConfigurationProperties(prefix = "auth.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(value = "mybatisSqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory() {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource());

		try {
			return sqlSessionFactoryBean.getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
