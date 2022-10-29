/*
 * The MIT License
 *
 * Copyright (c) 2016-2021 JOML
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
//#ifndef __GWT__
import java.util.Locale;


/**
 * Utility class for reading system properties.
 * 
 * @author Kai Burjack
 */
public final class Options {

    /**
     * Whether certain debugging checks should be made, such as that only direct NIO Buffers are used when Unsafe is active,
     * and a proxy should be created on calls to readOnlyView().
     */
    public static final boolean DEBUG = hasOption(System.getProperty("joml.debug", "false"));

    /**
     * Whether fast approximations of some java.lang.Math operations should be used.
     */
    public static final boolean FASTMATH = hasOption(System.getProperty("joml.fastmath", "false"));

    /**
     * When {@link #FASTMATH} is <code>true</code>, whether to use a lookup table for sin/cos.
     */
    public static final boolean SIN_LOOKUP = hasOption(System.getProperty("joml.sinLookup", "false"));

    /**
     * When {@link #SIN_LOOKUP} is <code>true</code>, this determines the table size.
     */
    public static final int SIN_LOOKUP_BITS = Integer.parseInt(System.getProperty("joml.sinLookup.bits", "14"));

//#ifdef __HAS_MATH_FMA__
    /**
     * Whether to try using java.lang.Math.fma() in most matrix/vector/quaternion operations if it is available.
     * If the CPU does <i>not</i> support it, it will be a lot slower than `a*b+c` and potentially generate a lot of memory allocations
     * for the emulation with `java.util.BigDecimal`, though.
     */
    public static final boolean USE_MATH_FMA = hasOption(System.getProperty("joml.useMathFma", "false"));

	/**
	 * Whether to use a {@link NumberFormat} producing scientific notation output when formatting matrix,
	 * vector and quaternion components to strings.
	 */
	public static final boolean useNumberFormat = hasOption(System.getProperty("joml.format", "true"));

    /**
     * Determines the number of decimal digits produced in the formatted numbers.
     */
    public static final int numberFormatDecimals = Integer.parseInt(System.getProperty("joml.format.decimals", "3"));

    /**
     * The {@link NumberFormat} used to format all numbers throughout all JOML classes.
     */
    public static final NumberFormat NUMBER_FORMAT = decimalFormat();

    private Options() {
    }

    private static NumberFormat decimalFormat() {
        NumberFormat df;

        if (useNumberFormat) {
            char[] prec = new char[numberFormatDecimals];
            Arrays.fill(prec, '0');
            df = new DecimalFormat(" 0." + new String(prec) + "E0;-");
        } else {
            df = NumberFormat.getNumberInstance(Locale.ENGLISH);
            df.setGroupingUsed(false);
        }

        return df;
    }

    private static boolean hasOption(String v) {
        if (v == null)
            return false;
        if (v.trim().length() == 0)
            return true;
        return Boolean.valueOf(v).booleanValue();
    }

}
