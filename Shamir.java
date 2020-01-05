package Main;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;


public class Shamir implements Serializable {

    Scanner scan = new Scanner(System.in);
    private BigInteger secret;
    private BigInteger prime;
    private int minParts;
    private int parts;
    private BigInteger partFonction;

    private int[] xparts;
    private BigInteger[] yparts;

    private int choix;


    //private BigInteger gcd;

    /**
     * INPUT a, b element de Z avec a >= b
     * OUTPUT g = gcd(a, b)
     */

    private static BigInteger multipleInverse(BigInteger a, BigInteger b) {
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

    public void deroulementApllication() throws IOException {

        if (secret == null) {
            //Déserialisation
            deserialisation();
        }


        // QB: j'ai bouclé pour valider un choix correcte
        do {
            System.out.println("Bienvenu sur Shamir's secret, Veuillez faire votre choix :");
            System.out.println("1 - Création d'un secret");
            System.out.println("2 - Ajout de parts");
            System.out.println("3 - Suppression de parts");
            System.out.println("4 - Retrouver le secret");
            System.out.println("0 - fermeture du programme");
            choix = scan.nextInt();
        } while (choix < 0 || choix > 4);

        deroulementApplication(choix);
    }

    private void deroulementApplication(int userChoice) throws IOException {

        switch (userChoice) {
            case (1):
                setSecret();
                break;
            case (2):
                ajoutPart();
                break;
            case (3):
                //Methode de suppression
                break;
            //QB: Ce que j'ai tenté de faire à la fin mais dont je ne sais pas comment est construit la fonction de lagrange
            case (4):
                //methode pour trouver secret
                findSecret();
                break;

            case (0):
                //methode d'archivage (serialization + cloture programme)
                saveSecret();

                return;
        }

        deroulementApllication();
    }

    private void setSecret() {
        if (secret != null) {
            System.out.print("Un secret a déjà été défini !! impossible d'en créer un nouveau");
            return;
        }

        System.out.print("Indiquez le nombre de bits : ");
        int nbrebits = scan.nextInt();

        //tests
        if (nbrebits < 128 || nbrebits > 4096)
            throw new IllegalArgumentException("La clé doit être entre 128 et 4096 bits");
        if (nbrebits % 8 != 0)
            throw new ArithmeticException("La clé doit être transformable en bytes (divisible par 8)");


        System.out.print("Indiquez le nombre parts minimum : ");
        this.minParts = scan.nextInt();
        System.out.print("Indiquez le nombre de parts voulues : ");
        this.parts = scan.nextInt();

        //test parts
        if (parts < minParts)
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur ou égal au minimum");

        setSecret(nbrebits, minParts, parts);
    }

    private void setSecret(int nbrebits, int minParts, int parts) {


        //Génération d'un secret et du nombre premier
        generateRandomKey(nbrebits / 8);

        //je génère une part x (x=indice pour l'instant)
        xparts = new int[parts];
        for (int i = 0; i < xparts.length; i++) {
            xparts[i] = i + 1;
        }

        //Je trouve le Y
        yparts = new BigInteger[parts];

        for (int i = 0; i < parts; i++) {
            trouveY(i);
        }

        //imprimer les parts + meta
        System.out.println("Voici les differentes parts : ");
        imprimeParts(0);
        System.out.println("les metadonnees sont les suivantes : " + prime);

    }

    private void imprimeParts(int debut) {


        for (int i = debut; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }


    }

    private void trouveY(int indice) {
        BigInteger temp = new BigInteger("0");
        BigInteger valueX = BigInteger.valueOf(xparts[indice]);


        for (int i = 0; i <= minParts; i++) {
            temp = valueX.multiply(temp);
            temp = temp.add(partFonction);
        }

        //reduction de la part avec modulo nombre premier
        yparts[indice] = temp.mod(prime);

    }

    private void generateRandomKey(int byteLength) throws IllegalArgumentException {

        SecureRandom random;
        byte[] bytes;

        do {
            random = new SecureRandom();
            bytes = new byte[byteLength];
            random.nextBytes(bytes);
        }
        while (bytes[0] != 1);


        secret = new BigInteger(bytes);

        random = new SecureRandom();
        bytes = new byte[byteLength];
        random.nextBytes(bytes);

        partFonction = new BigInteger(bytes);

        prime = secret.nextProbablePrime();

        if (prime.bitLength() > byteLength * 8) {
            generateRandomKey(byteLength);
        }

    }

    private void ajoutPart() {
        if (secret != null) {
            System.out.print("Indiquez le nombre de parts à ajouter : ");
            int nouveauNombre = scan.nextInt();

            if (nouveauNombre < 0)
                throw new IllegalArgumentException("Veuillez entrer un nombre positif");

            ajoutPart(nouveauNombre);
        }

    }

    private void ajoutPart(int nouveauNombres) {
        int ancien = parts;

        parts += nouveauNombres;


        int[] partx = new int[parts];
        for (int i = 0; i < parts; i++) {
            //reprendre anciennes parts de X
            if (i < ancien)
                partx[i] = xparts[i];

                //Nouvelles parts de X
            else
                partx[i] = i + 1;
        }
        //transfert de tableau
        xparts = partx;

        BigInteger[] party = new BigInteger[parts];
        for (int i = 0; i < ancien; i++) {
            party[i] = yparts[i];
        }

        //transfert de tableau
        yparts = party;

        //compléter nouvelles parts
        for (int i = ancien; i < parts; i++) {
            trouveY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = ancien; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }

    //Il reste encore la suppression d'une part, la reconstruction du secret et la sérialisation à faire


    private BigInteger findSecret() {
        BigInteger[] arrayx = new BigInteger[minParts];
        BigInteger[] arrayy = new BigInteger[minParts];
        BigInteger secretFinder = null;

        if (secret == null) {
            return null;
        }
        System.out.println("Le nombre de part pour reconstituer secret est de " + minParts);

        //je boucle sur le nombre de parts minimum nécessaire pour la reconstitution
        for (int i = 0; i < minParts; i++) {

            //j'entre la coordonnée x
            System.out.println("Entrez la part x No " + i + " : ");
            arrayx[i] = scan.nextBigInteger();

            //j'entre la coordonnée y
            System.out.println("Entrez la part y No " + i + " : ");
            arrayy[i] = scan.nextBigInteger();
        }

        //je dois refaire la formule de lagrange avec les données de x et y pour trouver le secret en x0l0
        for (int j = 0; j < minParts; j++) {
            secretFinder = arrayx[j];
        }

        return secretFinder;

    }

    private void saveSecret() throws IOException {

        if (isSecret()) {
            File fichier;
            fichier = new File("serialiseSecret.ser");
            fichier.createNewFile();


            FileOutputStream fos = new FileOutputStream(fichier);

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            //serialisation des nombres clés
            oos.writeChars(secret.toString() + "TheEnd");
            oos.writeChars(prime.toString() + "TheEnd");
            oos.writeChars(minParts + "TheEnd");
            oos.writeChars(parts + "TheEnd");
            oos.writeChars(partFonction.toString() + "TheEnd");

            //serialisation des X et Y

            for (int i = 0; i < xparts.length; i++) {
                if (i == xparts.length - 1) {
                    oos.writeChars(xparts[i] + "TheEnd");
                } else
                    oos.writeChars(xparts[i] + "FinPartX");
            }

            for (int i = 0; i < yparts.length; i++) {
                if (i == yparts.length - 1) {
                    oos.writeChars(yparts[i].toString() + "TheEnd");
                } else
                    oos.writeChars(yparts[i].toString() + "FinPartY");
            }


            oos.close();
            fos.close();

            System.out.println("Les données ont été sauvegardées avec succès");
        } else {
            System.out.println("Aucune donnée ne peut être sauvegardée");
        }

        System.out.println("Fermeture du programme");

    }

    private void deserialisation() {

        File fichier = new File("serialiseSecret.ser");

        // ouverture d'un flux sur un fichier
        ObjectInputStream ois;
        String serial;

        try {
            ois = new ObjectInputStream(new FileInputStream(fichier));

            serial = "";

            while (ois.available() > 0) {
                char c = ois.readChar();
                serial += c;
            }

            if (serial != null) {

                String[] result = serial.split("TheEnd");

                this.secret = new BigInteger(result[0]);
                this.prime = new BigInteger(result[1]);
                this.minParts = Integer.parseInt(result[2]);
                this.parts = Integer.parseInt(result[3]);
                this.partFonction = new BigInteger(result[4]);

                String[] part = result[5].split("FinPartX");
                xparts = new int[parts];

                for (int i = 0; i < part.length; i++) {
                    xparts[i] = Integer.parseInt(part[i]);
                }

                part = result[6].split("FinPartY");
                yparts = new BigInteger[parts];

                for (int i = 0; i < part.length; i++) {
                    yparts[i] = new BigInteger(part[i]);
                }
            } else return;


        } catch (IOException e) {

            System.out.println("Aucune donnée n'a été trouvé");
        }
    }

    private boolean isSecret() {
        return secret != null;
    }


}