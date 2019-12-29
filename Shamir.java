package Main;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;

public class Shamir {


    Scanner scan = new Scanner(System.in);



    private BigInteger secret;
    private BigInteger prime;
    private int minParts;
    private int parts;
    private int[] xparts;
    private BigInteger[] yparts;

    public BigInteger getPartFonction() {
        return partFonction;
    }

    private BigInteger partFonction;
    private BigInteger gcd;

    public BigInteger getSecret() {
        return secret;
    }

    public void setSecret() {
        if(secret!=null)
        {
            System.out.print("Un secret a déjà été défini");
            return;
        }

        System.out.print("Indiquez le nombre de bits : ");
        int nbrebits = scan.nextInt();

        //tests
        if (nbrebits < 128 || nbrebits > 4096)
            throw new IllegalArgumentException("La clé doit être entre 128 et 4096 bits");
        if (nbrebits%8!=0)
            throw new ArithmeticException("La clé doit être transformable en bytes (divisible par 8)");


        System.out.print("Indiquez le nombre parts minimum : ");
        this.minParts = scan.nextInt();
        System.out.print("Indiquez le nombre de parts voulues : ");
        this.parts = scan.nextInt();

        //test parts
        if (parts<minParts)
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur ou égal au minimum");

        setSecret(nbrebits, minParts, parts);
    }

    private void setSecret(int nbrebits, int minParts, int parts) {


        //Génération d'un secret et du nombre premier
        generateRandomKey(nbrebits/8);

        //je génère une part x
        xparts = new int[parts];
        for(int i = 0;i<xparts.length;i++ ) {
            xparts[i]=i+1;
        }

        //Je trouve le Y
        yparts = new BigInteger[parts];
        for(int i = 0;i<parts;i++)
        {
            trouveY(i);
        }

        //imprimer les parts + meta
        System.out.println("Voici les differentes parts : ");
        for(int i = 0;i<parts;i++)
        {
            System.out.println("No : " + (i+1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }
        System.out.println("les metadonnees sont les suivantes : " + prime);


    }


    private void trouveY(int indice)
    {
        BigInteger temp = new BigInteger("0");
        BigInteger valueX = BigInteger.valueOf(xparts[indice]);

        for(int i = 0;i<=minParts;i++)
        {
            temp=valueX.multiply(temp);
            temp=temp.add(partFonction);
        }
        yparts[indice]=temp.mod(prime);
    }
    /**
     *	INPUT a, b element de Z avec a >= b
     *	OUTPUT g = gcd(a, b)
     */

    public static BigInteger multipleInverse(BigInteger a, BigInteger b)
    {
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

            q.add(i, r.get(i-1).divide(r.get(i)));

            r.add(i+1, r.get(i-1).subtract(r.get(i).multiply(q.get(i))));

            x.add(i+1, x.get(i-1).subtract(x.get(i).multiply(q.get(i))));
            y.add(i+1, y.get(i-1).subtract(y.get(i).multiply(q.get(i))));

        }
        while (r.get(i+1).compareTo(BigInteger.ZERO) > 0) ;

        BigInteger multInverse = y.get(i);

        while (multInverse.compareTo(BigInteger.ZERO) < 0) {
            multInverse = multInverse.add(a);
        }

        return multInverse;
    }

    public void generateRandomKey(int byteLength) throws IllegalArgumentException {

        SecureRandom random;
        byte bytes[];

        do
            {
                random = new SecureRandom();
                bytes = new byte[byteLength]; // 128 bits are converted to 16 bytes;
                random.nextBytes(bytes);
            }
        while(bytes[0]!=1);



        secret = new BigInteger(bytes);

        random = new SecureRandom();
        bytes = new byte[byteLength];
        random.nextBytes(bytes);

        partFonction = new BigInteger(bytes);

        prime = secret.nextProbablePrime();

        if(prime.bitLength()>byteLength*8)
        {
            generateRandomKey(byteLength);
        }

    }

    public void ajoutPart()
    {
        System.out.print("Indiquez le nombre de parts à ajouter : ");
        int nouveauNombre = scan.nextInt();

        if(nouveauNombre<0)
            throw new IllegalArgumentException("Veuillez entrer un nombre positif");


        ajoutPart(nouveauNombre);
    }

    private void ajoutPart(int nouveauNombres)
    {
        int ancien = parts;

        parts+=nouveauNombres;


        int[] partx = new int[parts];
        for(int i = 0; i<parts;i++)
        {
            //reprendre anciennes parts de X
            if(i<ancien)
                partx[i]=xparts[i];

            //Nouvelles parts de X
            else
               partx[i]=i+1;
        }
        //transfert de tableau
        xparts=partx;

        BigInteger[] party = new BigInteger[parts];
        for(int i = 0; i<parts;i++)
        {
            //reprendre anciennes parts de Y
            if(i<ancien)
                party[i]=yparts[i];

            //Nouvelles parts de Y
            else
                trouveY(i);
        }
        //transfert de tableau
        yparts=party;

        System.out.println("Voici les nouvelles parts : ");
        for(int i = ancien; i<parts; i++)
        {
            System.out.println("No : " + (i+1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }


}
