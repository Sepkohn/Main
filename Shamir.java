package Main;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;


public class Shamir implements Serializable {

    private static final Long serialVersionUID = 1L;

    Scanner scan = new Scanner(System.in);

    private BigInteger secret;
    private BigInteger prime;
    private int minParts;
    private int parts;


    private BigInteger[] xparts;
    private BigInteger[] yparts;

    private BigInteger[] randoms;

    private int choix;


    //private BigInteger gcd;

    /**
     * INPUT a, b element de Z avec a >= b
     * OUTPUT g = gcd(a, b)
     */



    public void deroulementApllication() throws IOException {

        if (secret == null) {
            deserialisation();
        }


        // QB: j'ai bouclé pour valider un choix correcte
        do {
            System.out.println("Bienvenu sur Shamir's secret, Veuillez faire votre choix :");
            System.out.println("1 - Création d'un secret");
            System.out.println("2 - Ajout de parts");
            System.out.println("3 - Suppression de parts");
            System.out.println("4 - Retrouver le secret");
            //A supprimer une fois le test effectué
            System.out.println("5 - imprimer le secret");
            System.out.println("6 - imprimer les parts");
            System.out.println("7 - Renouveller parts");
            System.out.println("0 - fermeture du programme");
            choix = scan.nextInt();
        }
        //A supprimer >4 et non 6
        while (choix < 0 || choix > 7);

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

            case (4):
                //methode pour trouver secret
                findSecret();

                break;

            //A supprimer une fois les test vérifiés
            case (5):
                System.out.println(secret);
                break;
            case (6):
                imprimeParts(0);
                break;
            //
            case (7):
                updateParts();
                break;
            //

            case (0):
                //methode d'archivage (serialization + cloture programme)
                saveSecret();

                return;
        }

        deroulementApllication();
    }

    private void setSecret() {
        if (isSecret()) {
            System.out.println("Un secret a déjà été défini !! impossible d'en créer un nouveau");
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
        int minParts = scan.nextInt();
        System.out.print("Indiquez le nombre de parts voulues : ");
        int parts = scan.nextInt();

        //test parts
        if (parts < minParts)
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur ou égal au minimum");

        setSecret(nbrebits, minParts, parts);
    }

    private void setSecret(int nbrebits, int minParts, int parts) {

        this.minParts = minParts;
        this.parts = parts;

        //Génération d'un secret et du nombre premier
        generateRandomKey(nbrebits / 8);

        //je génère une part x (x=indice pour l'instant)
        xparts = new BigInteger[parts];
        for (int i = 0; i < xparts.length; i++) {

            xparts[i] = BigInteger.valueOf(i+1);
        }

        //Je trouve le Y
        yparts = new BigInteger[parts];

        for (int i = 0; i < yparts.length; i++) {
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

        BigInteger valueX = xparts[indice];

        for (int i = 0; i <randoms.length; i++) {
            temp = temp.add(randoms[i].multiply(valueX.pow(i)));
        }

        //reduction de la part avec modulo nombre premier
        System.out.println("mon test: y vaut " + yparts[indice]);
        yparts[indice] = temp.mod(prime);
        System.out.println("mon test: y vaut après " + yparts[indice]);

    }

    private void generateRandomKey(int byteLength) throws IllegalArgumentException {



        do {

            secret = randomNumber(byteLength);
        }
        while(getByteLength()<byteLength);

        prime = secret.nextProbablePrime();

        if (prime.bitLength() > byteLength * 8) {
            generateRandomKey(byteLength);
        }

        randoms = new BigInteger[minParts];
        randoms[0]=secret;

        for(int i = 1;i<randoms.length;i++) {
            BigInteger partFonction = randomNumber(byteLength);
            if(prime.compareTo(partFonction)<1) {
                partFonction = partFonction.mod(prime);
            }
            randoms[i]=partFonction;
        }


    }

    private BigInteger randomNumber(int byteLength){

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[byteLength];
        random.nextBytes(bytes);

        return new BigInteger(1, bytes);

    }

    private void generateRandoms(int byteLength){

        randoms = new BigInteger[minParts];
        randoms[0]=secret;

        for(int i = 1;i<randoms.length;i++) {
            BigInteger partFonction = randomNumber(byteLength);
            if(prime.compareTo(partFonction)<1) {
                partFonction = partFonction.mod(prime);
            }
            randoms[i]=partFonction;
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


        BigInteger[] partx = new BigInteger[parts];
        for (int i = 0; i < parts; i++) {
            //reprendre anciennes parts de X
            if (i < ancien)
                partx[i] = xparts[i];
                //Nouvelles parts de X
            else
                partx[i] = BigInteger.valueOf(i + 1);
        }
        //transfert de tableau
        xparts = partx;

        BigInteger[] party = new BigInteger[parts];
        for (int i = 0; i < ancien; i++) {
            party[i] = yparts[i];
        }

        //transfert de tableau
        yparts = party;

        for (int i = ancien; i<parts;i++){
            trouveY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = ancien; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }

    private void updateParts() {
        if (isSecret()) {
            String reponse="";

            System.out.print("Voullez vous changer de seuil? (y-n) ");
            reponse = scan.next();

            switch (reponse) {
                case ("y"):

                    System.out.println("Quel est le nouveau seuil ? ");
                    int nouveauNombre = scan.nextInt();

                    if (nouveauNombre < 0)
                        throw new IllegalArgumentException("Veuillez entrer un nombre positif");

                    if(nouveauNombre>parts) {
                        System.out.println("Veuillez choisir un seuil inférieur au nombre de part déjà créées ou ajoutez de nouvelles parts en conséquence.");
                        System.out.println();
                        return;
                    }
                    if(nouveauNombre == 1) {
                        System.out.println("Veuillez choisir un seuil supérieur à 1.");
                        System.out.println();
                        return;
                    }

                    minParts = nouveauNombre;

                    updatePartS();
                    break;

                case ("n"):
                    updatePartS();
                    break;
            }




        }
    }

    private void updatePartS() {

        randoms = new BigInteger[minParts];
        randoms[0]=secret;

        for(int i = 1;i<randoms.length;i++) {
            BigInteger partFonction = randomNumber(getByteLength());
            if(prime.compareTo(partFonction)<1) {
                partFonction = partFonction.mod(prime);
            }
            randoms[i]=partFonction;
        }

        for (int i = 0; i < parts; i++) {
            trouveY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = 0; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }


    private boolean isSecret() {
        if(secret==null)
            return false;
        else
            return true;
    }

    private int getByteLength(){ return secret.bitLength()/8;}



    private void findSecret() {

        //le secret est la formule f(x) à x = 0

       /*
        * - Calcul du secret à partir de parts et des éventuelles metadata.
         Une erreur est affichée si la reconstruction n’est pas possible.

        */

        BigInteger[] arrayx = new BigInteger[minParts];
        BigInteger[] arrayy = new BigInteger[minParts];


        if (!isSecret()) {
            System.out.println("Aucun secret n'a été crée, Veuillez recommencer");
            return;
        }

        System.out.println("Le nombre de part pour reconstituer secret est de " + minParts);

        //je boucle sur le nombre de parts minimum nécessaire pour la reconstitution


        for (int i = 0; i < minParts; i++) {

            //j'entre la coordonnée x
            System.out.println("Entrez la part x No " + (i + 1) + " : ");
            arrayx[i] = scan.nextBigInteger();

            //j'entre la coordonnée y
            System.out.println("Entrez la part y No " + (i + 1) + " : ");
            arrayy[i] = scan.nextBigInteger();
        }

        BigInteger result = findResult(BigInteger.valueOf(0), arrayx, arrayy);

        if(result!=null)
            System.out.println("Le secret reconstitué est le suivant : " + result);

    }
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

    private BigInteger modularSubstract(BigInteger x1, BigInteger x2, BigInteger prime){

        BigInteger result = x1.subtract(x2);

        if(result.compareTo(BigInteger.ZERO)<0){
            result= result.add(prime);
        }

        return result;
    }

    private BigInteger multipleInverse(BigInteger a, BigInteger b) {

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


            //serialisation des X et Y et randoms

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
            for (int i = 0; i < randoms.length; i++) {
                if (i == randoms.length - 1) {
                    oos.writeChars(randoms[i].toString() + "TheEnd");
                } else
                    oos.writeChars(randoms[i].toString() + "FinRandoms");
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

                String[] part = result[4].split("FinPartX");
                xparts = new BigInteger[parts];

                for (int i = 0; i < part.length; i++) {
                    xparts[i] = new BigInteger(part[i]);
                }

                part = result[5].split("FinPartY");
                yparts = new BigInteger[parts];

                for (int i = 0; i < part.length; i++) {
                    yparts[i] = new BigInteger(part[i]);
                }
                part = result[6].split("FinRandoms");
                randoms = new BigInteger[part.length];

                for (int i = 0; i < part.length; i++) {
                    randoms[i] = new BigInteger(part[i]);
                }

            } else return;


        } catch (IOException e) {

            System.out.println("Aucune donnée n'a été trouvé");
        }
    }
}
