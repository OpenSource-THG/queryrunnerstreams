package com.thehutgroup.queryrunnerstreams;

import com.thehutgroup.queryrunnerstreams.NamedParameterParser.SqlAndParamsList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

public class NamedParameterQueryRunner extends QueryRunner {

  private final NamedParameterParser parser;

  public NamedParameterQueryRunner(DataSource ds) {
    super(ds);
    parser = new NamedParameterParser();
  }

  public <T> T query(String sql, ResultSetHandler<T> rsh, Map<String, Object> params)
      throws SQLException {

    SqlAndParamsList simple = parser.parseNamedParameters(sql, params);

    return query(simple.getSql(), rsh, simple.getParams());
  }

  public int update(String sql, Map<String, Object> params)
      throws SQLException {

    SqlAndParamsList simple = parser.parseNamedParameters(sql, params);

    return update(simple.getSql(), simple.getParams());
  }

  public int execute(String sql, Map<String, Object> params)
      throws SQLException {

    SqlAndParamsList simple = parser.parseNamedParameters(sql, params);

    return execute(simple.getSql(), simple.getParams());
  }

  public Stream<SqlRow> stream(String sql, Map<String, Object> params) throws SQLException {

    SqlAndParamsList simple = parser.parseNamedParameters(sql, params);

    return stream(simple.getSql(), simple.getParams());
  }

  public Stream<SqlRow> stream(String sql, Object... params) throws SQLException {
    final Connection conn = prepareConnection();

    if (conn == null) {
      throw new SQLException("Null connection");
    }

    if (sql == null) {
      throw new SQLException("Null SQL statement");
    }

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = prepareStatement(conn, sql);
      fillStatement(stmt, params);

      rs = wrap(stmt.executeQuery());

    } catch (SQLException ex) {
      try {
        rethrow(ex, sql, params);
      } finally {
        close(rs, stmt, conn);
      }
    }

    final Statement fstmt = stmt;
    final ResultSet frs = rs;

    return QueryStream.of(rs, () -> close(frs, fstmt, conn));
  }

  private void close(ResultSet rs, Statement stmt, Connection conn) {
    try {
      try {
        close(rs);
      } finally {
        close(stmt);
        close(conn);
      }
    } catch (SQLException ex) {
      throw new RuntimeSQLException(ex);
    }
  }
}
