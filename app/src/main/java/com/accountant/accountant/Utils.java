package com.accountant.accountant;

public final class Utils {
    private Utils() {
    }

    public static int parseInputAsMonetaryAmount(String input) throws NumberFormatException {
        if (input.indexOf('.') == -1) {
            return Integer.parseInt(input) * 100;
        }

        String beforePoint = input.substring(0, input.indexOf('.'));
        String afterPoint = input.substring(input.indexOf('.') + 1);
        if (afterPoint.length() == 0) {
            return Integer.parseInt(beforePoint) * 100;
        } else if (afterPoint.length() == 1) {
            return Integer.parseInt(beforePoint) * 100 +
                    Integer.parseInt(afterPoint) * 10;
        } else if (afterPoint.length() == 2) {
            return Integer.parseInt(beforePoint) * 100 +
                    Integer.parseInt(afterPoint);
        } else {
            throw new NumberFormatException();
        }
    }
}
