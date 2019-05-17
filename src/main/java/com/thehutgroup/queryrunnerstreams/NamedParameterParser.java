package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NamedParameterParser {

  private static final Pattern pattern = Pattern.compile(":[a-zA-Z0-9\\-]+");

  SqlAndParamsList parseNamedParameters(String sql, Map<String, Object> params)
      throws SQLException {

    List<Object> paramList = new ArrayList<>();

    Matcher matcher = pattern.matcher(sql);
    while (matcher.find()) {
      String key = matcher.group().substring(1);
      Object value = params.get(key);
      if (value == null) {
        throw new SQLException("Parameter :" + key + " could not be matched");
      }
      paramList.add(value);
    }

    return new SqlAndParamsList(matcher.replaceAll("?"), paramList.toArray());
  }

  static class SqlAndParamsList {
    private final String sql;
    private final Object[] params;

    SqlAndParamsList(String sql, Object... params) {
      this.sql = sql;
      this.params = params;
    }

    String getSql() {
      return sql;
    }

    Object[] getParams() {
      return params;
    }
  }

}
