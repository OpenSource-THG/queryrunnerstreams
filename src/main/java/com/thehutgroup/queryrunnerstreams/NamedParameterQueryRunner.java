package com.thehutgroup.queryrunnerstreams;

import com.thehutgroup.queryrunnerstreams.NamedParameterParser.SqlAndParamsList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

  @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE") //Intentionally throwing the parent
  public <T> List<T> queryForList(
      final String sql, final SafeSQLFunction<SqlRow, T> rowMapper, Object... params)
      throws SQLException {

    ResultSetHandler<List<T>> rsh = rs -> {
      List<T> list = new ArrayList<>();

      while (rs.next()) {
        try {
          list.add(rowMapper.apply(new SqlRow(rs)));
        } catch (RuntimeSQLException ex) {
          throw ex.getParent();
        }
      }

      return list;
    };

    return query(sql, rsh, params);
  }

  public <T> List<T> queryForList(
      String sql, SafeSQLFunction<SqlRow, T> rowMapper, Map<String, Object> params)
      throws SQLException {

    SqlAndParamsList simple = parser.parseNamedParameters(sql, params);

    return queryForList(simple.getSql(), rowMapper, simple.getParams());
  }

  @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE") //Intentionally throwing the parent
  public <T> T queryForObject(final String sql, final Class<T> clazz, Object... params)
      throws SQLException {

    ResultSetHandler<T> rsh = rs -> {
      if (rs.next()) {
        try {
          return new SqlRow(rs).get(1, clazz); //Wrap in SqlRow for better type handling.
        } catch (RuntimeSQLException ex) {
          throw ex.getParent();
        }
      }

      throw new EmptyResultDataAccessException("Object could not be found");
    };

    try {
      return query(sql, rsh, params);
    } catch (SQLException ex) {
      //This will only be true if no errors occurred before the EmptyResultDataAccessException
      if (ex.getNextException() instanceof EmptyResultDataAccessException) {
        throw ex.getNextException();
      } else {
        throw ex;
      }
    }
  }

  public <T> T queryForObject(final String sql, final Class<T> clazz, Map<String, Object> params)
      throws SQLException {

    SqlAndParamsList simple = parser.parseNamedParameters(sql, params);

    return queryForObject(simple.getSql(), clazz, simple.getParams());
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
