package com.bbc.km.configuration;

import com.impossibl.postgres.jdbc.PGDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class PostgresConfig {

    public static final String DATASOURCE = "postgresDataSource";
    public static final String DATASOURCE_ACK = "postgresDataSourceAck";

    @Bean(DATASOURCE)
    @ConfigurationProperties(prefix = "spring.data.postgres")
    public DataSource dataSource() {
        return new PGDataSource();
    }

    @Primary
    @Bean(DATASOURCE_ACK)
    @ConfigurationProperties(prefix = "spring.data.postgres-ack")
    public DataSource postgresDataSource() {
        return new HikariDataSource();
    }
}
