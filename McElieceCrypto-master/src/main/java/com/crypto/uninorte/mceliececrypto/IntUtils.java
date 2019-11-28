package com.crypto.uninorte.mceliececrypto;

public final class IntUtils {

    /**
     * Default constructor (private).
     */
    private IntUtils() {
        // empty
    }

    /**
     * Return a clone of the given int array. No null checks are performed.
     *
     * @param array the array to clone
     * @return the clone of the given array
     */
    public static int[] clone(int[] array) {
        int[] result = new int[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    public static boolean equals(int[] left, int[] right) {
        if (left.length != right.length) {
            return false;
        }
        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            result &= left[i] == right[i];
        }
        return result;
    }
}
