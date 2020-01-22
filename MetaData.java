package main;

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

    public int getParts() {
        return parts;
    }


    private BigInteger prime;

    private int minParts;
    private int parts;


    /**
     * Methode de calcul pour trouver le secret ou les nouvelles parts de y
     * @param a - valeur a 0 pour retrouver le secret ou valeur du x pour trouver la part y
     * @param arrayx - tableau constitué des parts X
     * @param arrayy - tableau constitué des parts X
     * @return le resultat du secret ou de la nouvelle part y
     */
    public BigInteger findResult(BigInteger a, BigInteger[] arrayx, BigInteger[] arrayy  ){

        BigInteger result = new BigInteger("0");


        for(int j = 0; j < minParts; j++) {

            BigInteger rv = BigInteger.ONE;

            for(int k = 0; k < minParts; k++) {
                if(k != j) {

                    BigInteger denominator = modularSubstract(arrayx[j], arrayx[k]);

                    BigInteger multInv = multipleInverse(denominator, prime);

                    BigInteger numerator = modularSubstract(a, arrayx[k]);

                    rv = rv.multiply(numerator).multiply(multInv);
                }

            }
            result = result.add(rv.multiply(arrayy[j])).mod(prime);

        }

        return result;
    }

    /**
     * Methode pour calculer les inverses multiplicatifs
     * @param a = valeur du dénominateur
     * @param b = nombre premier
     * @return l'inverse multiplicatif de a
     */

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

    /**
     * Methode pour calculer une soustraction modulaire
     * @param x1 = valeur du 1er nombre
     * @param x2 = valeur du 2e nombre
     * @return la resultat de x1-x2 avec si besoin  modulo (prime)
     */

    private BigInteger modularSubstract(BigInteger x1, BigInteger x2){

        BigInteger result = x1.subtract(x2);

        if(result.compareTo(BigInteger.ZERO)<0){
            result= result.add(prime);
        }

        return result;
    }

    /**
     * Generation de coefficients pour generer la fonction polynomiale
     * @param byteLength -- nombre de bits demande par l'utilisateur divise par 8
     * @throws IllegalArgumentException
     * @return une nombre aleatoire modulo(prime)
     */

    public BigInteger[] generateRandoms(int byteLength){

        BigInteger[] randoms = new BigInteger[minParts];

        for(int i = 1;i<randoms.length;i++) {
            BigInteger rdmNumber = randomNumber(byteLength);
            if(prime.compareTo(rdmNumber)<1) {
                rdmNumber = rdmNumber.mod(prime);
            }
            randoms[i]=rdmNumber;
        }
        return randoms;

    }
    /**
     * Generation d'un nombre aleatoire d'un nombre de Byte donne
     * @param byteLength -- nombre de bits demande par l'utilisateur divise par 8
     * @throws IllegalArgumentException
     * @return un nombre aleatoire
     */

    public BigInteger randomNumber(int byteLength){

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[byteLength];
        random.nextBytes(bytes);

        return new BigInteger(1, bytes);

    }
}
