package com.crypto.uninorte.mceliececrypto;



public final class IntUtils
{

    /**
     * Default constructor (private).
     */
    private IntUtils()
    {
        // empty
    }


    /**
     * Return a clone of the given int array. No null checks are performed.
     *
     * @param array the array to clone
     * @return the clone of the given array
     */
    public static int[] clone(int[] array)
    {
        int[] result = new int[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

}
