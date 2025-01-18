package com.quentity.entity.field;

import lombok.Builder;

public abstract class Spice {
    enum NumberType {
        INTEGER,
        DECIMAL,
        BIG_DECIMAL
    }

    NumberType numberType;

    public abstract Number getMax();

    public abstract Number getStep();

    public abstract Number getMin();


    private static class NumberSpice<N extends Number> extends Spice {
        private final N min;
        private final N max;
        private final N step;

        public NumberSpice(NumberType numberType, N min, N max, N step) {
            this.numberType = numberType;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        public Number getMax() {
            return max == null ? 0 : max;
        }

        public Number getStep() {
            return step == null ? 0 : step;
        }

        public Number getMin() {
            return min == null ? 0 : min;
        }
    }

    public static class IntegerSpice extends NumberSpice<Integer> {
        @Builder
        public IntegerSpice(Integer min, Integer max, Integer step) {
            super(NumberType.INTEGER, min, max, step);
        }
    }

    public static class DecimalSpice extends NumberSpice<Double> {
        @Builder
        public DecimalSpice(Double min, Double max, Double step) {
            super(NumberType.DECIMAL, min, max, step);
        }
    }

    public static class BigDecimalSpice extends Spice {
        @Builder
        public BigDecimalSpice() {
            this.numberType = NumberType.BIG_DECIMAL;
        }

        @Override
        public Number getMax() {
            return null;
        }

        @Override
        public Number getStep() {
            return null;
        }

        @Override
        public Number getMin() {
            return null;
        }
    }
}
