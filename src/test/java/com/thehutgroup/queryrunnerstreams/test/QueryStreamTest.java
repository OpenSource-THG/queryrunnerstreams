package com.thehutgroup.queryrunnerstreams.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.thehutgroup.queryrunnerstreams.QueryStream;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryStreamTest {

  private final QueryRunner queryRunner;

  public QueryStreamTest() {
    queryRunner = getQueryRunner();
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
  @DisplayName("Test that a Stream can be made from a ResultSet")
  void testThatValuesCanBeExtracted() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    doReturn("value").when(rs).getObject("column");
    doReturn(7).when(rs).getRow();
    doReturn(true).when(rs).next();

    Optional<String> firstColumnValue = QueryStream.of(rs)
        .map(row -> row.get("column", String.class))
        .findFirst();

    assertThat(firstColumnValue.get(), is("value"));
  }

  @Test
  @DisplayName("Test that we can query from a database")
  void testQueryingFromRealDB() throws SQLException {

    String authUrl = UUID.randomUUID().toString();

    insertRows(queryRunner, authUrl);

    String result = queryRunner.query(
        "SELECT * FROM Social_Login_Provider WHERE Auth_URL = ? ORDER BY Code DESC",
        rs -> QueryStream.of(rs)
            .map(row -> row.get("Code", String.class))
            .collect(Collectors.joining(",")),
        authUrl);

    assertThat(result, is("twitter,facebook"));
  }

  @Test
  @DisplayName("Test that the mapper functionality works as expected")
  void testQueryingWithMapFromRealDB() throws SQLException {

    String authUrl = UUID.randomUUID().toString();

    insertRows(queryRunner, authUrl);

    String result = queryRunner.query(
        "SELECT * FROM Social_Login_Provider WHERE Auth_URL = ? ORDER BY Code DESC",
        rs -> QueryStream
            .map(rs, row -> row.get("Code", String.class))
            .collect(Collectors.joining(",")),
        authUrl);

    assertThat(result, is("twitter,facebook"));
  }

  @Test
  @DisplayName("Test that the to list functionality works as expected")
  void testQueryingWithToListFromRealDB() throws SQLException {

    String authUrl = UUID.randomUUID().toString();

    insertRows(queryRunner, authUrl);

    List<String> result = queryRunner.query(
        "SELECT * FROM Social_Login_Provider WHERE Auth_URL = ? ORDER BY Code DESC",
        rs -> QueryStream
            .toList(rs, row -> row.get("Code", String.class)),
        authUrl);

    assertThat(result, is(Arrays.asList("twitter", "facebook")));
  }

  private QueryRunner getQueryRunner() {
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
    //config.setMetricRegistry(Objects.requireNonNull(themisMetricRegistry.getMetricRegistry()));
    return new QueryRunner(new HikariDataSource(config));
  }

  private void insertRows(QueryRunner queryRunner, String authUrl) throws SQLException {
    queryRunner.execute("INSERT INTO Social_Login_Provider "
        + "  (Provider_Id, Code, Name, Auth_URL, Method) "
        + "VALUES "
        + "  (1, 'facebook', 'facebook', ?, 'oauth2'),"
        + "  (2, 'twitter', 'twitter', ?, 'twitter')", authUrl, authUrl);
  }
}
