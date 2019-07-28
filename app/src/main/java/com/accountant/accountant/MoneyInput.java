package com.accountant.accountant;

public class MoneyInput {
    private byte[] integral;
    private int integralCount;
    private byte[] decimal;
    private int decimalCount;
    private boolean switchedToDecimal;

    public MoneyInput() {
        integral = new byte[5];
        integralCount = 0;
        decimal = new byte[2];
        decimalCount = 0;
        switchedToDecimal = false;
    }

    public void append(int digit) {
        if (digit < 0 || digit > 9) {
            throw new IllegalArgumentException("Parameter digit must be a single digit");
        }

        if (!switchedToDecimal) {
            if (integralCount < integral.length) {
                integral[integralCount] = (byte)digit;
                integralCount++;
            }
        } else {
            if (decimalCount < decimal.length) {
                decimal[decimalCount] = (byte) digit;
                decimalCount++;
            }
        }
    }

    public void appendDot() {
        switchedToDecimal = true;
    }

}
