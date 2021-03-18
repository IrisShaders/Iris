/*
 * The MIT License
 *
 * Copyright (c) 2017-2021 JOML
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.coderbot.iris.vendored.joml;

import java.text.NumberFormat;

/**
 * Internal class to detect features of the runtime.
 * 
 * @author Kai Burjack
 */
public final class Runtime {

//#ifndef __GWT__
    public static final boolean HAS_floatToRawIntBits = hasFloatToRawIntBits();
    public static final boolean HAS_doubleToRawLongBits = hasDoubleToRawLongBits();
    public static final boolean HAS_Long_rotateLeft = hasLongRotateLeft();

//#ifdef __HAS_MATH_FMA__
    public static final boolean HAS_Math_fma = Options.USE_MATH_FMA && hasMathFma();

    private static boolean hasMathFma() {
        try {
            java.lang.Math.class.getDeclaredMethod("fma", new Class[] { float.class, float.class, float.class });
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


    private Runtime() {
    }

//#ifndef __GWT__
    private static boolean hasFloatToRawIntBits() {
        try {
            Float.class.getDeclaredMethod("floatToRawIntBits", new Class[] { float.class });
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasDoubleToRawLongBits() {
        try {
            Double.class.getDeclaredMethod("doubleToRawLongBits", new Class[] { double.class });
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasLongRotateLeft() {
        try {
            Long.class.getDeclaredMethod("rotateLeft", new Class[] { long.class, int.class });
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


    public static int floatToIntBits(float flt) {
//#ifndef __GWT__
        if (HAS_floatToRawIntBits)
            return floatToIntBits1_3(flt);

        return floatToIntBits1_2(flt);
    }
//#ifndef __GWT__
    private static int floatToIntBits1_3(float flt) {
        return Float.floatToRawIntBits(flt);
    }

    private static int floatToIntBits1_2(float flt) {
        return Float.floatToIntBits(flt);
    }

    public static long doubleToLongBits(double dbl) {
//#ifndef __GWT__
        if (HAS_doubleToRawLongBits)
            return doubleToLongBits1_3(dbl);

        return doubleToLongBits1_2(dbl);
    }
//#ifndef __GWT__
    private static long doubleToLongBits1_3(double dbl) {
        return Double.doubleToRawLongBits(dbl);
    }

    private static long doubleToLongBits1_2(double dbl) {
        return Double.doubleToLongBits(dbl);
    }

    public static String formatNumbers(String str) {
        StringBuffer res = new StringBuffer();
        int eIndex = Integer.MIN_VALUE;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == 'E') {
                eIndex = i;
            } else if (c == ' ' && eIndex == i - 1) {
                // workaround Java 1.4 DecimalFormat bug
                res.append('+');
                continue;
            } else if (Character.isDigit(c) && eIndex == i - 1) {
                res.append('+');
            }
            res.append(c);
        }
        return res.toString();
    }

    public static String format(double number, NumberFormat format) {
        if (Double.isNaN(number)) {
            return padLeft(format, " NaN");
        } else if (Double.isInfinite(number)) {
            return padLeft(format, number > 0.0 ? " +Inf" : " -Inf");
        }
        return format.format(number);
    }

    private static String padLeft(NumberFormat format, String str) {
        int len = format.format(0.0).length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len - str.length() + 1; i++) {
            sb.append(" ");
        }
        return sb.append(str).toString();
    }

    public static boolean equals(float a, float b, float delta) {
        return Float.floatToIntBits(a) == Float.floatToIntBits(b) || java.lang.Math.abs(a - b) <= delta;
    }

    public static boolean equals(double a, double b, double delta) {
        return Double.doubleToLongBits(a) == Double.doubleToLongBits(b) || java.lang.Math.abs(a - b) <= delta;
    }

}
