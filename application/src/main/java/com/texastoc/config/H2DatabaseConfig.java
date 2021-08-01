package com.texastoc.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Only run when the mysql spring profile is not present
 */
@Configuration
@ConditionalOnProperty(prefix = "use", name = "h2", havingValue = "true")
public class H2DatabaseConfig {

  public static boolean initialized = false;

  @Bean
  public DataSource dataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("org.h2.Driver");
    dataSourceBuilder.url("jdbc:h2:mem:testdb");
    dataSourceBuilder.username("sa");
    dataSourceBuilder.password("");
    return dataSourceBuilder.build();
  }

  @Bean
  CommandLineRunner init(JdbcTemplate jdbcTemplate) {
    return args -> {
      InputStream resource = new ClassPathResource(
          "create_toc_schema.sql").getInputStream();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(resource))) {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          if (StringUtils.isBlank(line)) {
            continue;
          }
          if (line.startsWith("#")) {
            continue;
          }

          sb.append(" " + line);

          if (line.endsWith(";")) {
            jdbcTemplate.execute(sb.toString());
            sb = new StringBuilder();
          }
        }
      }

      resource = new ClassPathResource(
          "seed_toc.sql").getInputStream();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(resource))) {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          if (StringUtils.isBlank(line)) {
            continue;
          }
          if (line.startsWith("#")) {
            continue;
          }

          sb.append(" " + line);

          if (line.endsWith(";")) {
            jdbcTemplate.execute(sb.toString());
            sb = new StringBuilder();
          }
        }
      }
      initialized = true;
    };
  }
}
