package piotrholda.portfoliomanager.infrastructure;

import java.math.MathContext;
import java.math.RoundingMode;

public class Math {
    public static final MathContext MATH_CONTEXT = new MathContext(12, RoundingMode.HALF_UP);
    public static final MathContext OUTPUT_CONTEXT = new MathContext(2, RoundingMode.HALF_UP);
}
