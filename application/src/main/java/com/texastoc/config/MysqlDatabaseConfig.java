package com.texastoc.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Only run when the mysql spring profile is present
 */
@Configuration
@ConditionalOnProperty(prefix = "use", name = "mysql", havingValue = "true")
public class MysqlDatabaseConfig {

  @Value("${mysql.url:jdbc:mysql://localhost/toc?useTimezone=true&serverTimezone=America/Chicago}")
  private String url;
  @Value("${mysql.username:tocuser}")
  private String username;
  @Value("${mysql.password:tocpass}")
  private String password;

  @Bean
  public DataSource dataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.url(url);
    dataSourceBuilder.username(username);
    dataSourceBuilder.password(password);
    return dataSourceBuilder.build();
  }
}
