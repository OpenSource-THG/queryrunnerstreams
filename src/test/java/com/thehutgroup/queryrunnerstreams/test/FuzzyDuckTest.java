package com.thehutgroup.queryrunnerstreams.test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

public class QueryrunnerstreamsTest {

  @Test
  public void test() {
    assertThat(true, is(not(false)));
  }

}
