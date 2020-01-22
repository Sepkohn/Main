package Main;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

public class MetaData {


    public MetaData(int minParts, int parts, BigInteger prime) {
        this.minParts = minParts;
        this.parts = parts;
        this.prime = prime;
    }

    public MetaData(int minParts, int parts) {
        this.minParts = minParts;
        this.parts = parts;
    }
    public MetaData(BigInteger prime) {
       this.prime = prime;
    }

    public BigInteger getPrime() {
        return prime;
    }


    public int getMinParts() {
        return minParts;
    }

    public void setMinParts(int minParts) {
        this.minParts = minParts;
    }

    public int getParts() {
        return parts;
    }


    private BigInteger prime;

    private int minParts;
    private int parts;


    /**
     * Methode de calcul
     * @param a
     * @param arrayx
     * @param arrayy
     * @return
     */
    public BigInteger findResult(BigInteger a, BigInteger[] arrayx, BigInteger[] arrayy  ){

        BigInteger result = new BigInteger("0");


        for (int j = 0; j < minParts; j++) {

            BigInteger rv = BigInteger.ONE;

            for (int k = 0; k < minParts; k++) {
                if (k != j) {

                    BigInteger denominator = modularSubstract(arrayx[j], arrayx[k], prime);

                    BigInteger multInv = multipleInverse(denominator, prime);

                    BigInteger numerator = modularSubstract(a, arrayx[k], prime);

                    rv = rv.multiply(numerator).multiply(multInv);
                }

            }
            result = result.add(rv.multiply(arrayy[j])).mod(prime);

        }

        return result;
    }


    protected BigInteger multipleInverse(BigInteger a, BigInteger b) {

        if(a.compareTo(b)<0){
            BigInteger temp = a;
            a = b;
            b= temp;
        }

        ArrayList<BigInteger> r = new ArrayList<>();
        ArrayList<BigInteger> q = new ArrayList<>();
        ArrayList<BigInteger> x = new ArrayList<>();
        ArrayList<BigInteger> y = new ArrayList<>();

        r.add(a);
        r.add(b);

        x.add(BigInteger.valueOf(1));
        x.add(BigInteger.ZERO);

        q.add(BigInteger.ZERO);
        y.add(BigInteger.ZERO);
        y.add(BigInteger.valueOf(1));

        int i = 0;

        do {
            i++;

            q.add(i, r.get(i - 1).divide(r.get(i)));
            r.add(i + 1, r.get(i - 1).subtract(r.get(i).multiply(q.get(i))));
            x.add(i + 1, x.get(i - 1).subtract(x.get(i).multiply(q.get(i))));
            y.add(i + 1, y.get(i - 1).subtract(y.get(i).multiply(q.get(i))));

        }
        while (r.get(i + 1).compareTo(BigInteger.ZERO) > 0);

        BigInteger multInverse = y.get(i);

        while (multInverse.compareTo(BigInteger.ZERO) < 0) {
            multInverse = multInverse.add(a);
        }

        return multInverse;
    }

    private BigInteger modularSubstract(BigInteger x1, BigInteger x2, BigInteger prime){

        BigInteger result = x1.subtract(x2);

        if(result.compareTo(BigInteger.ZERO)<0){
            result= result.add(prime);
        }

        return result;
    }

    /**
     * Génération de coefficients pour générer la fonction polynomiale
     * @param byteLength -- nombre de bits demandé par l'utilisateur divisé par 8
     * @throws IllegalArgumentException
     */

    public BigInteger[] generateRandoms(int byteLength){

        BigInteger[] randoms = new BigInteger[minParts];

        for(int i = 1;i<randoms.length;i++) {
            BigInteger partFonction = randomNumber(byteLength);
            if(prime.compareTo(partFonction)<1) {
                partFonction = partFonction.mod(prime);
            }
            randoms[i]=partFonction;
        }
        return randoms;

    }

    public BigInteger randomNumber(int byteLength){

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[byteLength];
        random.nextBytes(bytes);

        return new BigInteger(1, bytes);

    }
}
