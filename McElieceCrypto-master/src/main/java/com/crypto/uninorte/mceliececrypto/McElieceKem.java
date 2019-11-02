package com.crypto.uninorte.mceliececrypto;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author pc
 */
public class McElieceKem {

    private int n; //tamaño
    private int m; //grado del polinomio del campo
    private int t; //grado del polinomio de goppa
    private int q; //cantidad de elementos en el campo
    private int k; //dimension del codigo sobre el campo
    private GF2mField field;// campo de codigos de goppa junto a sus operaciones 
    private int fieldPoly; //polinomio del campo
    private PolynomialGF2mSmallM goppaFieldPoly;//contiene los metodos para realizar operaciones del polinomio g(z)
    private int[] goppaPoly; //Coeficientes de los polinomios
    private int[] L; //conjunto de elementos aleatoreos del campo que no se repiten

    private SecureRandom secureRandom;

    public McElieceKem(int n, int m, int t) { //restricciones para el el kem de mceliece
        if (t < 2) {
            System.err.println("El parámetro t debe ser mayor o igual a 2!");
            return;
        }
        if (m * t >= n) {
            System.err.println("La condición mt < n no se cumple!");
            return;
        }
        if (n > 1 << m) {
            System.err.println("El parámetro n no cumple la condición n <= 2^m");
            return;
        }

        this.secureRandom = new SecureRandom();

        this.m = m;
        this.n = n;
        this.t = t;
        this.q = 1 << this.m;
        this.k = this.n - this.m * this.t;

        this.field = new GF2mField(this.m, this.secureRandom);

        this.fieldPoly = this.field.getPolynomial();

        this.goppaFieldPoly = new PolynomialGF2mSmallM(field, t, PolynomialGF2mSmallM.RANDOM_IRREDUCIBLE_POLYNOMIAL, this.secureRandom);

        this.goppaPoly = this.goppaFieldPoly.getCoefficients();

        this.L = generateRandomL();

    }

    public McElieceKem(int[] L, int fieldPoly, int[] goppaPoly) {
        this.L = L;
        this.fieldPoly = fieldPoly;
        this.goppaPoly = goppaPoly;

        this.secureRandom = new SecureRandom();

        this.m = PolynomialRingGF2.degree(fieldPoly);

        this.field = new GF2mField(this.m, fieldPoly, this.secureRandom);

        this.goppaFieldPoly = new PolynomialGF2mSmallM(field, goppaPoly);

        this.n = this.L.length;
        this.t = this.goppaPoly.length - 1;
        this.q = 1 << this.m;
        this.k = this.n - this.m * this.t;

        if (this.t < 2) {
            System.err.println("El parámetro t debe ser mayor o igual a 2!");
            return;
        }
        if (this.m * this.t >= this.n) {
            System.err.println("La condición mt < n no se cumple!");
            return;
        }
        if (this.n > this.q) {
            System.err.println("El parámetro n no cumple la condición n <= 2^m");
            return;
        }

    }

    public int[] generateRandomL() { //funcion para obtener los elementos del campo L[]
        boolean[] flags = new boolean[this.q];
        int[] randomL = new int[this.n];
        int count = 0;
        int roots = 0;
        while (count < this.n && count + roots < this.q) {
            int element = field.getRandomElement();
            if (flags[element]) {
                continue;
            }
            if (this.goppaFieldPoly.evaluateAt(element) == 0) {
                roots++;
            } else {
                randomL[count] = element;
                count++;
            }
            flags[element] = true;
        }
        if (count == this.n) {
            return randomL;
        } else {
            return null;
        }
    }

    public int getElementOfH(int j, int i) { //Elementos de la matriz H apartir de L y g(z)
        int element = PolynomialRingGF2.remainder(L[j], field.getPolynomial());
        return field.mult(field.exp(element, i), field.inverse(goppaFieldPoly.evaluateAt(element)));
    }

    public static void main(String[] args) {

//        McElieceKem mck = new McElieceKem(new int[]{
//            1<<2,
//            1<<3,
//            1<<4,
//            1<<5,
//            1<<6,
//            1<<7,
//            1<<8,
//            1<<9,
//            1<<10,
//            1<<11,
//            1<<12,
//            1<<13,
//        }, 0b10011, new int[]{1,11,1});
        McElieceKem mck = new McElieceKem(16, 4, 2); //n=16,m=4,t=2
        int[][] H;
        boolean good;
        do {
            good = true;
            System.out.println("m = " + mck.m);
            System.out.println("n = " + mck.n);
            System.out.println("t = " + mck.t);
            System.out.println("q = " + mck.q);
            System.out.println("k = " + mck.k);
            System.out.println("g(z) = " + Arrays.toString(mck.goppaPoly));
            System.out.println("L = " + Arrays.toString(mck.L));
            System.out.println("f(x) = " + Integer.toBinaryString(mck.fieldPoly));

            int mt = mck.m * mck.t;

            int[][] h = new int[mt][mck.n]; //generacion de la matriz de paridad H

            for (int i = 0; i < mck.t; i++) {
                for (int j = 0; j < mck.n; j++) {
                    int hij = mck.getElementOfH(j, i);
                    for (int k = 0; k < mck.m; k++) {
                        h[i * mck.m + k][j] = (hij & (1 << k)) >>> k;
                    }
                }
            }

            System.out.println("");
            System.out.println("H=");
            for (int i = 0; i < mt; i++) {
                System.out.println(Arrays.toString(h[i]));
            }
            System.out.println("");

            for (int i = 0; i < mt; i++) {
                if (h[i][i] == 0) {
                    for (int j = i + 1; j < mt; j++) {
                        if (h[j][i] == 1) {
                            for (int z = 0; z < mck.n; z++) {
                                h[i][z] = h[i][z] ^ h[j][z];
                            }
                            break;
                        }
                    }
                }

                for (int j = i + 1; j < mt; j++) {
                    if (h[j][i] == 1) {
                        for (int z = i; z < mck.n; z++) {
                            h[j][z] = h[j][z] ^ h[i][z];
                        }
                    }
                }

            }

            for (int i = mt - 1; i >= 1; i--) {
                for (int j = i - 1; j >= 0; j--) {
                    if (h[i][i] == 0) {
                        mck.L = mck.generateRandomL();
                        mck.goppaFieldPoly = new PolynomialGF2mSmallM(mck.field, mck.t, PolynomialGF2mSmallM.RANDOM_IRREDUCIBLE_POLYNOMIAL, mck.secureRandom);
                        good = false;
                        continue;
                    }
                    if (h[j][i] == 1) {
                        for (int z = i; z < mck.n; z++) {
                            h[j][z] = h[j][z] ^ h[i][z];
                        }
                    }
                }
            }

            int[][] g = new int[mck.k][mck.n]; //generacion de G matriz generadora
            for (int i = mt, f = 0; i < mck.n; i++, f++) {
                for (int j = 0; j < mt; j++) {
                    g[f][j] = h[j][i];
                }
            }
            for (int i = 0; i < mck.k; i++) {
                g[i][mt + i] = 1;
            }

            System.out.println("H^=");
            for (int i = 0; i < mt; i++) {
                System.out.println(Arrays.toString(h[i]));
            }

            System.out.println("");
            System.out.println("G=");
            for (int i = 0; i < mck.k; i++) {
                System.out.println(Arrays.toString(g[i]));
            }
            System.out.println("");
            H = h;
        } while (!good);

        System.out.println("Public Key:");//Generacion de llave publica H=(I|T)
        for (int i = 0; i < mck.m * mck.t; i++) {
            System.out.print("[");
            for (int j = mck.m * mck.t; j < (mck.n - 1); j++) {
                System.out.print(H[i][j] + ", ");
            }
            System.out.println(H[i][mck.n - 1] + "]");
        }
        int r = new Random().nextInt((int) Math.pow(2, mck.n));
        String s = Integer.toBinaryString(r); //s string random de n bits
        System.out.println("s = " + s);
        System.out.println("Private Key:");//Generacion de llave privada
        System.out.println("{" + s + "," + Arrays.toString(mck.goppaPoly) + "," + Arrays.toString(mck.L) + "}");

        r = new Random().nextInt((int) Math.pow(2, mck.n));
        String E = Integer.toBinaryString(r);
        if (E.length() < mck.n) {
            for (int i = E.length(); i < mck.n; i++) {
                E = "0" + E;
            }
        }
        String[] valorE = E.split("");
        int[] e = new int[valorE.length];
        for (int i = 0; i < valorE.length; i++) {
            e[i] = Integer.parseInt(valorE[i]);
        }

        int[] c0 = multiply(e, H);

        byte[] two = {2};
        byte[] one = {1};

        byte[] HC1 = concatenateVectors(two, getEByteVector(e));
        byte[] c1 = SHAKE(HC1);
        byte[] C = concatenateVectors(getEByteVector(c0), c1);

        byte[] HK = concatenateVectors4(one, getEByteVector(e), getEByteVector(c0), c1);
        byte[] K = SHAKE(HK);
        System.out.println("");
        System.out.println("========================");
        System.out.println("Encapsulation");
        System.out.println("========================");
        System.out.println("C en byte: ");

        for (int i = 0; i < C.length; i++) {
            System.out.print(C[i] + " ");
        }
        System.out.println("");
        System.out.println("C en hexadeciaml: ");

        Hex hex = new Hex();
        System.out.println(hex.toHexString(C));

        System.out.println("K en byte: ");
        for (int i = 0; i < K.length; i++) {
            System.out.print(K[i] + " ");
        }
        System.out.println("");
        System.out.println("K en hexadeciaml: ");
        System.out.println(hex.toHexString(K));
        System.out.println("========================");
    }

    public static String padding(String binStr, int N) {
        String aux = binStr;
        while (aux.length() < N) {
            aux = "0" + aux;
        }
        return aux;
    }

    public static byte[] getEByteVector(int[] e) {
        String bitString = "";
        //System.out.println(e.length);
        for (int i = 0; i < e.length; i++) {
            bitString += "" + e[i];
        }
        BigInteger number = new BigInteger(bitString, 2);
        String hexString = padding(number.toString(16), e.length / 8);
        return Hex.decode(hexString);
    }

    public static byte[] concatenateVectors(byte[] vec1, byte[] vec2) {
        byte[] result = new byte[vec1.length + vec2.length];
        System.arraycopy(vec1, 0, result, 0, vec1.length);
        System.arraycopy(vec2, 0, result, vec1.length, vec2.length);
        return result;
    }

    public static byte[] concatenateVectors4(byte[] vec1, byte[] vec2, byte[] vec3, byte[] vec4) {
        byte[] result = new byte[vec1.length + vec2.length + vec3.length + vec4.length];
        System.arraycopy(vec1, 0, result, 0, vec1.length);
        System.arraycopy(vec2, 0, result, vec1.length, vec2.length);
        System.arraycopy(vec3, 0, result, vec1.length + vec2.length, vec3.length);
        System.arraycopy(vec4, 0, result, vec1.length + vec2.length + vec3.length, vec4.length);
        return result;
    }

    public static byte[] SHAKE(byte[] data) {

        SHAKEDigest sd = new SHAKEDigest(256);
        sd.update(data, 0, data.length);
        byte[] result = new byte[32];
        sd.doFinal(result, 0);
        return (result);
    }

    public static int[] multiply(int[] a, int[][] b) {
        int[] c = new int[b.length];
        // se comprueba si las matrices se pueden multiplicar
        if (a.length == b[0].length) {
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < b[0].length; j++) {
                    // aquí se multiplica la matriz
                    if (a[i] == 0) {
                        c[i] = 0;
                    } else {
                        c[i] += b[i][j];
                    }
                }
                c[i] = c[i] % 2;
            }
        }
        /**
         * si no se cumple la condición se retorna una matriz vacía
         */
        return c;
    }

}
