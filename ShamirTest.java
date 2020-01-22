package Main;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ShamirTest{

    @Test
    void findResult() {


        BigInteger secret;
        BigInteger prime;
        BigInteger[] randoms;

        int minParts = 2;
        int parts = 2;
        int byteLength = 16;

        do {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[byteLength];
            random.nextBytes(bytes);

            secret = new BigInteger(1, bytes);

        }
        while(secret.bitLength()<byteLength*8);

        prime = secret.nextProbablePrime();

            randoms = new BigInteger[minParts];
            randoms[0]=secret;

            for(int i = 1;i<randoms.length;i++) {
                SecureRandom random = new SecureRandom();
                byte[] bytes = new byte[byteLength];
                random.nextBytes(bytes);

                BigInteger partFonction = new BigInteger(1, bytes);

                if(prime.compareTo(partFonction)<1) {
                    partFonction = partFonction.mod(prime);
                }
                randoms[i]=partFonction;
            }

        BigInteger[] xparts = new BigInteger[minParts];
        for (int i = 0; i < xparts.length; i++) {
            xparts[i] = BigInteger.valueOf(i+1);
        }

        BigInteger[] yparts = new BigInteger[minParts];

        for (int i = 0; i < yparts.length; i++) {
            BigInteger temp = new BigInteger("0");

            BigInteger valueX = xparts[i];

            for (int j = 0; j <randoms.length; j++) {
                temp = temp.add(randoms[j].multiply(valueX.pow(j)));
            }

            yparts[i] = temp.mod(prime);
        }

        MetaData data = new MetaData(minParts, parts, prime);

        //test si le secret retrouvé est juste
        assertEquals(secret,data.findResult(BigInteger.ZERO, xparts, yparts));
    }

    @Test
    void multipleInverse(){

        int byteLength = 16;
        SecureRandom random = null;

        Random rdm = new Random();
        BigInteger prime = BigInteger.probablePrime(byteLength*8, rdm);
        MetaData datas = new MetaData(prime);

        for(int i = 0; i< 10; i++)
            random = new SecureRandom();
            byte[] bytes = new byte[byteLength];
            random.nextBytes(bytes);

            BigInteger result = new BigInteger(1, bytes);

            assertEquals(result.modInverse(prime), datas.multipleInverse(result,prime));

    }





}