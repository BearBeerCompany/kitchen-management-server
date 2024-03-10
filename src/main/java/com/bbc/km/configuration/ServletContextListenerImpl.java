package com.bbc.km.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

import static com.bbc.km.configuration.PostgresConfig.DATASOURCE;

@Component
public class ServletContextListenerImpl implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextListenerImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PGConnection pgConnection;

    private boolean isChannelOpen = false;

    public ServletContextListenerImpl(@Autowired @Qualifier(DATASOURCE) DataSource dataSource) throws SQLException {
        pgConnection = (PGConnection) dataSource.getConnection();

        pgConnection.addNotificationListener(new PGNotificationListener() {
            @Override
            public void notification(int processId, String channelName, String payload) {
                LOGGER.info("Received from channel {} message with payload {}", channelName, payload);
                final JsonNode json;
                try {
                    json = OBJECT_MAPPER.readTree(payload);
                    System.out.println(json);
                    //TODO: map the json node to a PKMI notification event and sent through ws with simpMessagingTemplate
                } catch (JsonProcessingException e) {
                    LOGGER.error("Failed json processing for ingested payload!", e);
                }
            }
        });
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Statement statement = pgConnection.createStatement();
            statement.execute("LISTEN plate_orders");
            statement.close();
            isChannelOpen = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (isChannelOpen)
            try {
                Statement statement = pgConnection.createStatement();
                statement.execute("UNLISTEN plate_orders");
                statement.close();
                isChannelOpen = false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

    }
}
