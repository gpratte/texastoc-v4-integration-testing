package com.texastoc.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Only run when the mysql spring profile is not present
 */
@Configuration
public class DatabaseConfig {

  @Value("${mysql.url:jdbc:mysql://localhost/toc?useTimezone=true&serverTimezone=America/Chicago}")
  private String mysqlUrl;
  @Value("${mysql.username:tocuser}")
  private String mysqlUsername;
  @Value("${mysql.password:tocpass}")
  private String mysqlPassword;

  @ConditionalOnProperty(prefix = "db", name = "h2", havingValue = "true")
  @Bean
  public DataSource h2DataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("org.h2.Driver");
    dataSourceBuilder.url("jdbc:h2:mem:testdb");
    dataSourceBuilder.username("sa");
    dataSourceBuilder.password("");
    return dataSourceBuilder.build();
  }

  @ConditionalOnProperty(prefix = "db", name = "mysql", havingValue = "true")
  @Bean
  public DataSource mysqlDataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.url(mysqlUrl);
    dataSourceBuilder.username(mysqlUsername);
    dataSourceBuilder.password(mysqlPassword);
    return dataSourceBuilder.build();
  }
}
