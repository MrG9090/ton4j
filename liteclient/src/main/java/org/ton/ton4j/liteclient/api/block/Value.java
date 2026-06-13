package org.ton.ton4j.liteclient.api.block;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Builder
@ToString
public class Value implements Serializable {
    BigDecimal grams;
    @Getter
    private List<Currency> otherCurrencies;

    public BigDecimal getGrams() {
        return grams;
    }

    public void setGrams(BigDecimal grams) {
        this.grams = grams;
    }

    /**
     * @deprecated use {@link #getGrams()} instead.
     */
    @Deprecated
    public BigDecimal getToncoins() {
        return getGrams();
    }

    /**
     * @deprecated use {@link #setGrams(BigDecimal)} instead.
     */
    @Deprecated
    public void setToncoins(BigDecimal grams) {
        setGrams(grams);
    }

    public static class ValueBuilder {
        /**
         * @deprecated use {@link #grams(BigDecimal)} instead.
         */
        @Deprecated
        public ValueBuilder toncoins(BigDecimal grams) {
            this.grams = grams;
            return this;
        }
    }
}
