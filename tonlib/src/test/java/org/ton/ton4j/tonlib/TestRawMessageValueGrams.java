package org.ton.ton4j.tonlib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ton.ton4j.tonlib.types.RawMessage;

@RunWith(JUnit4.class)
public class TestRawMessageValueGrams {

  @Test
  public void testValueGramsAccessors() {
    RawMessage message = RawMessage.builder().value("1234567890").build();

    assertThat(message.getValueGrams(9)).isEqualTo("1.234567890");
    assertThat(message.getValueToncoins(9)).isEqualTo("1.234567890");
  }
}
