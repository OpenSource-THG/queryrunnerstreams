package com.thehutgroup.queryrunnerstreams;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thehutgroup.queryrunnerstreams.NamedParameterParser.SqlAndParamsList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressFBWarnings("CLI")
class NamedParameterParserTest {

  private NamedParameterParser parser;

  @BeforeEach
  void setup() {
    parser = new NamedParameterParser();
  }

  @Test
  @DisplayName("Check standard operation of Named Parameter Parser")
  void basicUsage() throws SQLException {
    String sql = "SELECT * FROM Table WHERE Column = :value";
    Map<String, Object> params = Collections.singletonMap("value", 7);

    SqlAndParamsList result = parser.parseNamedParameters(sql, params);

    assertThat(result.getSql(), is("SELECT * FROM Table WHERE Column = ?"));
    assertThat(result.getParams().length, is(1));
    assertThat(result.getParams()[0], is(7));
  }

  @Test
  @DisplayName("Check repeated use of Named Parameter Parser")
  void repeatedUsage() throws SQLException {
    String sql = "SELECT * FROM Table WHERE ColA = :value1 AND ColB = :value2 AND ColC = :value1";

    Map<String, Object> params = new HashMap<>();
    params.put("value1", 7);
    params.put("value2", 5);

    SqlAndParamsList result = parser.parseNamedParameters(sql, params);

    assertThat(result.getSql(),
        is("SELECT * FROM Table WHERE ColA = ? AND ColB = ? AND ColC = ?"));
    assertThat(result.getParams().length, is(3));
    assertThat(result.getParams()[0], is(7));
    assertThat(result.getParams()[1], is(5));
    assertThat(result.getParams()[2], is(7));
  }

  @Test
  @DisplayName("Check unused parameters are fine")
  void unusedParams() throws SQLException {
    String sql = "SELECT * FROM Table WHERE ColA = :value1 AND ColB = :value2 AND ColC = :value1";

    Map<String, Object> params = new HashMap<>();
    params.put("value1", 7);
    params.put("value2", 5);
    params.put("value3", 9);
    params.put("value4", 9);

    SqlAndParamsList result = parser.parseNamedParameters(sql, params);

    assertThat(result.getSql(),
        is("SELECT * FROM Table WHERE ColA = ? AND ColB = ? AND ColC = ?"));
    assertThat(result.getParams().length, is(3));
    assertThat(result.getParams()[0], is(7));
    assertThat(result.getParams()[1], is(5));
    assertThat(result.getParams()[2], is(7));
  }

  @Test
  @DisplayName("Check null parameters are fine")
  void nullParams() throws SQLException {
    String sql = "SELECT * FROM Table WHERE ColA = :value1 AND ColB = :value2 AND ColC = :value1";

    Map<String, Object> params = new HashMap<>();
    params.put("value1", null);
    params.put("value2", 5);

    SqlAndParamsList result = parser.parseNamedParameters(sql, params);

    assertThat(result.getSql(),
        is("SELECT * FROM Table WHERE ColA = ? AND ColB = ? AND ColC = ?"));
    assertThat(result.getParams().length, is(3));
    assertThat(result.getParams()[0], is(nullValue()));
    assertThat(result.getParams()[1], is(5));
    assertThat(result.getParams()[2], is(nullValue()));
  }

  @Test
  @DisplayName("Check that a missing parameter throws an exception")
  void missingParam() {
    String sql = "SELECT * FROM Table WHERE ColA = :value1 AND ColB = :value2 AND ColC = :value1";

    Map<String, Object> params = new HashMap<>();
    params.put("value1", 7);

    SQLException exception = null;

    try {
      parser.parseNamedParameters(sql, params);
    } catch (SQLException ex) {
      exception = ex;
    }

    assertThat(exception, is(not(nullValue())));
  }

  @Test
  @DisplayName("Check batch operation of Named Parameter Parser")
  void batchUsage() throws SQLException {
    String sql = "SELECT * FROM Table WHERE ColA = :val1 AND ColB IN (:val2[]) AND ColC = :val3";

    Map<String, Object> params = new HashMap<>();
    params.put("val1", 5);
    params.put("val2", Arrays.asList(6, 7, 8));
    params.put("val3", 9);

    SqlAndParamsList result = parser.parseNamedParameters(sql, params);

    assertThat(result.getSql(),
        is("SELECT * FROM Table WHERE ColA = ? AND ColB IN (?, ?, ?) AND ColC = ?"));
    assertThat(result.getParams().length, is(5));
    assertThat(result.getParams()[0], is(5));
    assertThat(result.getParams()[1], is(6));
    assertThat(result.getParams()[2], is(7));
    assertThat(result.getParams()[3], is(8));
    assertThat(result.getParams()[4], is(9));
  }

  @Test
  @DisplayName("Check that passing in something that is not a collection fails as a batch param")
  void batchUsageWithoutCollection() {
    String sql = "SELECT * FROM Table WHERE ColA = :val1 AND ColB IN (:val2[]) AND ColC = :val3";

    Map<String, Object> params = new HashMap<>();
    params.put("val1", 5);
    params.put("val2", 8);
    params.put("val3", 9);

    assertThrows(SQLException.class, () -> parser.parseNamedParameters(sql, params));

    //Even an array should throw for now.
    params.put("val2", new int[] { 6, 7, 8 });
    assertThrows(SQLException.class, () -> parser.parseNamedParameters(sql, params));
  }

}
