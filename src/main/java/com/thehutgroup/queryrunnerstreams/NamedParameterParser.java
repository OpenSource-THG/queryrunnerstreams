package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NamedParameterParser {

  private static final Pattern PATTERN = Pattern.compile("(:[a-zA-Z0-9\\-]+)(\\[\\])?");

  SqlAndParamsList parseNamedParameters(final String sql, final Map<String, Object> params)
      throws SQLException {

    final StringBuffer resultSql = new StringBuffer();
    final List<Object> paramList = new ArrayList<>();

    final Matcher matcher = PATTERN.matcher(sql);
    while (matcher.find()) {
      final String key = matcher.group(1).substring(1);
      final boolean batch = matcher.group(2) != null;
      final Object value = params.get(key);
      if (value == null) {
        throw new SQLException("Parameter :" + key + " could not be matched");
      }

      if (batch) {
        if (!(value instanceof Collection<?>)) {
          throw new SQLException("Value of :" + key + " is not a collection");
        }

        final Collection<?> collection = (Collection) value;
        final int count = collection.size();
        paramList.addAll(collection);

        matcher.appendReplacement(resultSql, String.join(", ", Collections.nCopies(count, "?")));
      } else {
        matcher.appendReplacement(resultSql, "?");
        paramList.add(value);
      }
    }

    matcher.appendTail(resultSql);

    return new SqlAndParamsList(resultSql.toString(), paramList.toArray());
  }

  static class SqlAndParamsList {
    private final String sql;
    private final Object[] params;

    SqlAndParamsList(final String sql, final Object... params) {
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
