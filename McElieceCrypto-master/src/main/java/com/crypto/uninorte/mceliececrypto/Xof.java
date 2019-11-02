package com.crypto.uninorte.mceliececrypto;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


public interface Xof
    extends ExtendedDigest
{
    /**
     * Output the results of the final calculation for this digest to outLen number of bytes.
     *
     * @param out output array to write the output bytes to.
     * @param outOff offset to start writing the bytes at.
     * @param outLen the number of output bytes requested.
     * @return the number of bytes written
     */
    int doFinal(byte[] out, int outOff, int outLen);

    /**
     * Start outputting the results of the final calculation for this digest. Unlike doFinal, this method
     * will continue producing output until the Xof is explicitly reset, or signals otherwise.
     *
     * @param out output array to write the output bytes to.
     * @param outOff offset to start writing the bytes at.
     * @param outLen the number of output bytes requested.
     * @return the number of bytes written
     */
    int doOutput(byte[] out, int outOff, int outLen);
}