package co.com.pragma.r2dbc.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MySQLConnectionPool {
    public static final int INITIAL_SIZE = 12;
    public static final int MAX_SIZE = 15;
    public static final int MAX_IDLE_TIME = 30;

	@Bean
	public ConnectionPool getConnectionConfig(MysqlConnectionProperties properties) {
		ConnectionFactory dbConfiguration =
                MySqlConnectionFactory.from(
                        MySqlConnectionConfiguration.builder()
                                .host(properties.host())
                                .user(properties.username())
                                .port(properties.port())
                                .password(properties.password())
                                .database(properties.database())
                                .createDatabaseIfNotExist(true)
                                .connectTimeout(Duration.ofSeconds(3))
                                .build()
                );

        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder()
                .connectionFactory(dbConfiguration)
                .name("api-mysql-connection-pool")
                .initialSize(INITIAL_SIZE)
                .maxSize(MAX_SIZE)
                .maxIdleTime(Duration.ofMinutes(MAX_IDLE_TIME))
                .validationQuery("SELECT 1")
                .build();

		return new ConnectionPool(poolConfiguration);
	}
}