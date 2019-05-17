package com.thehutgroup.queryrunnerstreams;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NamedParameterQueryRunnerTest {

  NamedParameterQueryRunner queryRunner;

  NamedParameterQueryRunnerTest() {
    queryRunner = getTestQueryRunner();
  }

  @BeforeEach
  void setup() throws SQLException {
    queryRunner.execute("CREATE TABLE Social_Login_Provider "
        + "(Provider_Id INT PRIMARY KEY, Code VARCHAR(32), Name VARCHAR(32), "
        + " Auth_URL VARCHAR(255), Method VARCHAR(8))");
  }

  @AfterEach
  void cleanup() throws SQLException {
    queryRunner.execute("DROP TABLE Social_Login_Provider");
  }

  @Test
  @DisplayName("Test that we can query from a database")
  void testQueryingFromRealDB() throws SQLException {

    String authUrl = UUID.randomUUID().toString();

    insertRows(authUrl);

    String result = queryRunner
        .stream(
            "SELECT * FROM Social_Login_Provider WHERE Auth_URL = :authUrl ORDER BY Code DESC",
            Collections.singletonMap("authUrl", authUrl))
        .map(row -> row.get("Code", String.class))
        .collect(Collectors.joining(","));

    assertThat(result, is("twitter,facebook"));
  }

  private NamedParameterQueryRunner getTestQueryRunner() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("sa");
    config.setDriverClassName("org.h2.Driver");
    config.setConnectionTimeout(500L);
    config.setIdleTimeout(120000L);
    config.setMaxLifetime(600000L);
    config.setMinimumIdle(5);
    config.setMaximumPoolSize(50);
    config.setValidationTimeout(250L);
    config.setLeakDetectionThreshold(30000L);
    config.setPoolName("hikari-db-pool");
    config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
    config.setReadOnly(false);
    return new NamedParameterQueryRunner(new HikariDataSource(config));
  }

  private void insertRows(String authUrl) throws SQLException {
    queryRunner.execute("INSERT INTO Social_Login_Provider "
        + "  (Provider_Id, Code, Name, Auth_URL, Method) "
        + "VALUES "
        + "  (1, 'facebook', 'facebook', :authUrl, 'oauth2'),"
        + "  (2, 'twitter', 'twitter', :authUrl, 'twitter')",
        Collections.singletonMap("authUrl", authUrl));
  }
}
