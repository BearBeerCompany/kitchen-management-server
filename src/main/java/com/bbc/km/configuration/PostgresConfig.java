package com.bbc.km.configuration;

import com.impossibl.postgres.jdbc.PGDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class PostgresConfig {

    public static final String DATASOURCE = "postgresDataSource";

    @Bean(DATASOURCE)
    @ConfigurationProperties(prefix = "spring.data.postgres")
    public DataSource dataSource() {
        return new PGDataSource();
    }
}
