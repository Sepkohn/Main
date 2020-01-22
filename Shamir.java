package main;

import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;


public class Shamir implements Serializable {

    private static final long serialVersionUID = 1L;

    Scanner scan = new Scanner(System.in);
    MetaData datas;
    private BigInteger secret;
    private BigInteger[] xparts;
    private BigInteger[] yparts;
    private BigInteger[] randoms;


    /**
     * @author Valentin Haenggeli
     * @author Quentin Beeckmans
     * <h1>Donnee</h1>
     * <p>Ecrire un logiciel permettant le partage d'un secret (Shamir's secret sharing etudie pendant le cours).
     * Le secret est une chaine de bits aleatoires (on peut supposer que ce nombre est un multiple de 8).
     * Ce logiciel est par exemple utilise pour proteger des cles cryptographiques (de 128 a 4096 bits). </p>
     * */

    /**
     * D�but de l'application.
     * V�rification automatique si un fichier serialise contenant les parts existe et si oui, le deserialise.
     * Proposition des choix.
     *
     * @throws IOException
     */
    public void appMethods() throws IOException {

        if (secret == null) {
            deserialisation();
        }

        int choice;

        do {

            System.out.println("Bienvenu sur Shamir's secret, Veuillez faire votre choix :");
            System.out.println("1 - Generer un secret");
            System.out.println("2 - Generer de nouvelles parts");
            System.out.println("3 - Renouveller les parts");
            System.out.println("4 - Retrouver le secret");
            System.out.println("0 - Fermeture du programme");
            choice = scan.nextInt();

        } while (choice < 0 || choice > 4);

        appMethods(choice);
    }

    /**
     * Envoie en methode le(s) choix de l'utilisateur
     *
     * @param userChoice recupere le choix de l'utilisateur
     * @throws IOException
     */
    private void appMethods(int userChoice) throws IOException {

        switch (userChoice) {
            case (1):
                setSecret();
                break;
            case (2):
                AddPart();
                break;
            case (3):
                updateParts();
                break;
            case (4):
                findSecret();
                break;
            case (0):
                saveSecret();
                return;
        }

        appMethods();
    }

    /**
     * Methode de creation de secret.
     *
     * @throws IllegalArgumentException -- "La cle doit etre entre 128 et 4096 bits" et "Le nombre de parts doit etre superieur ou egal au minimum"
     * @throws ArithmeticException      -- "La cle doit etre transformable en bytes (divisible par 8)"
     */
    private void setSecret() {
        if (isSecret()) {
            System.out.println("Un secret a deja ete defini !! impossible d'en creer un nouveau");
            return;
        }

        System.out.print("Indiquez le nombre de bits : ");
        int bitLength = scan.nextInt();


        if (bitLength < 128 || bitLength > 4096)
            throw new IllegalArgumentException("La cle doit etre entre 128 et 4096 bits");
        if (bitLength % 8 != 0)
            throw new ArithmeticException("La cle doit etre transformable en bytes (divisible par 8)");


        System.out.print("Indiquez le nombre parts minimum : ");
        int minParts = scan.nextInt();
        System.out.print("Indiquez le nombre de parts voulues : ");
        int parts = scan.nextInt();

        if (parts < minParts)
            throw new IllegalArgumentException("Le nombre de parts doit etre superieur ou egal au minimum");

        setSecret(bitLength, minParts, parts);
    }

    /**
     * La creation du secret et de ses parts en passant les parametres demandes
     *
     * @param bitLength = nombre de bits que comportera le secret
     * @param minParts  = le nombre de parts minimum servant a la reconstruction du sercret
     * @param parts     = le nomnre de parts que l'on souhaite creer dans l'immediat
     */
    private void setSecret(int bitLength, int minParts, int parts) {

        datas = new MetaData(minParts, parts);

        generateRandomKey(bitLength / 8);


        xparts = new BigInteger[parts];
        for (int i = 0; i < xparts.length; i++) {

            xparts[i] = BigInteger.valueOf(i + 1);
        }

        yparts = new BigInteger[parts];

        for (int i = 0; i < yparts.length; i++) {
            findFirstY(i);
        }

        System.out.println("Voici les differentes parts : ");
        displayParts(0);

    }

    /**
     * Methode permetant d'afficher les parts de notre secret
     *
     * @param beginning = index de debut de la table
     */
    private void displayParts(int beginning) {

        if (!isSecret())
            return;


        for (int i = beginning; i < datas.getParts(); i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }

    /**
     * La methode FindY genere l'ordonnee Y pour la generation de la part en BigInteger pour chaque parametre indice en fonctions des premieres parts.
     *
     * @param index -- est la valeur de l'ordonnee X
     */
    private void findY(int index) {

        BigInteger valueX = xparts[index];

        yparts[index] = datas.findResult(valueX, xparts, yparts);


    }

    /**
     * La methode FindY genere l'ordonnee Y pour la generation de la part en BigInteger pour chaque parametre indice en fonction des coefficients crees.
     *
     * @param index -- est la valeur de l'ordonnee X
     */
    private void findFirstY(int index) {
        BigInteger temp = new BigInteger("0");

        BigInteger valueX = xparts[index];

        if (randoms == null) {
            randoms = datas.generateRandoms(getByteLength());
            randoms[0] = secret;
        }

        for (int i = 0; i < randoms.length; i++) {
            temp = temp.add(randoms[i].multiply(valueX.pow(i)));
        }

        yparts[index] = temp.mod(datas.getPrime());
    }


    /**
     * Generation d'un secret et des metadatas
     *
     * @param byteLength -- nombre de bits demande par l'utilisateur divise par 8
     * @throws IllegalArgumentException
     */
    private void generateRandomKey(int byteLength) throws IllegalArgumentException {
        do {
            secret = datas.randomNumber(byteLength);
        }
        while (secret.bitLength() < byteLength * 8);

        BigInteger prime = secret.nextProbablePrime();

        datas = new MetaData(datas.getMinParts(), datas.getParts(), prime);

        if (prime.bitLength() > byteLength * 8) {
            generateRandomKey(byteLength);
        }

        randoms = datas.generateRandoms(getByteLength());
        randoms[0] = secret;

    }


    /**
     * Methode d'initialisation d'ajout de parts du secret
     */
    private void AddPart() {
        if (secret != null) {
            System.out.print("Indiquez le nombre de parts a ajouter : ");
            int newNumber = scan.nextInt();

            if (newNumber < 0)
                throw new IllegalArgumentException("Veuillez entrer un nombre positif");

            AddPart(newNumber);
        }

    }

    /**
     * Methode d'ajout de parts du secret avec en parametre le nombre demande par l'utilisateur
     *
     * @param newNumber le nombre de parts a ajouter
     *                  genere les coordonnees X et Y pour les nouvelles parts
     */
    private void AddPart(int newNumber) {
        int parts = datas.getParts();
        int oldNumber = parts;

        parts += newNumber;


        BigInteger[] partx = new BigInteger[parts];
        for (int i = 0; i < parts; i++) {
            if (i < oldNumber)
                partx[i] = xparts[i];
            else
                partx[i] = BigInteger.valueOf(i + 1);
        }
        xparts = partx;

        BigInteger[] party = new BigInteger[parts];
        for (int i = 0; i < oldNumber; i++) {
            party[i] = yparts[i];
        }

        yparts = party;

        for (int i = oldNumber; i < parts; i++) {
            findY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = oldNumber; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

        datas = new MetaData(datas.getMinParts(), parts, datas.getPrime());

    }

    /**
     * Methode d'initialisation de renouvellement des parts deja creees
     * Propose aussi de modifier le nombre de parts minimum pour la reconstruction du secret.
     *
     * @throws IllegalArgumentException il faut un nombre positif
     */
    private void updateParts() {
        if (!isSecret())
            return;

        String response = "";

        do {
            System.out.print("Voullez vous changer de seuil? (y-n) ");
            response = scan.next();
        } while (!response.equals("y") && !response.equals("n"));

        switch (response) {

            case ("y"):
                System.out.println("Quel est le nouveau seuil ? ");
                int newNumber = scan.nextInt();

                if (newNumber < 0)
                    throw new IllegalArgumentException("Veuillez entrer un nombre positif");

                if (newNumber > datas.getParts()) {
                    System.out.println("Veuillez choisir un seuil inferieur au nombre de part deja creees ou ajoutez de nouvelles parts en consequence.");
                    System.out.println();
                    return;
                }
                if (newNumber == 1) {
                    System.out.println("Veuillez choisir un seuil superieur a 1.");
                    System.out.println();
                    return;
                }
                datas = new MetaData(newNumber, datas.getParts(), datas.getPrime());

                updatePartS();
                break;

            case ("n"):
                updatePartS();
                break;
        }

    }

    /**
     * Methode de mise a jour des parts
     */
    private void updatePartS() {

        int parts = datas.getParts();

        randoms = datas.generateRandoms(getByteLength());
        randoms[0] = secret;

        for (int i = 0; i < parts; i++) {
            findFirstY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = 0; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }

    /**
     * Methode de verification si le secret existe ou pas
     *
     * @return false et un message d'erreur sinon
     */
    private boolean isSecret() {
        if (secret == null) {
            System.out.println("Aucun secret n'a ete definit, veuillez commencer par le generer");

            return false;
        } else
            return true;
    }

    private int getByteLength() {
        return secret.bitLength() / 8;
    }

    /**
     * Methode pour retrouver notre secret en fonction des differentes parts necessaire pour composer la fonction de Lagrange
     */
    private void findSecret() {

        if (!isSecret())
            return;

        int minParts = datas.getMinParts();

        BigInteger[] arrayx = new BigInteger[minParts];
        BigInteger[] arrayy = new BigInteger[minParts];


        System.out.println("Le nombre de part pour reconstituer secret est de " + minParts);


        for (int i = 0; i < minParts; i++) {

            System.out.println("Entrez la part x No " + (i + 1) + " : ");
            arrayx[i] = scan.nextBigInteger();

            System.out.println("Entrez la part y No " + (i + 1) + " : ");
            arrayy[i] = scan.nextBigInteger();
        }

        BigInteger result = datas.findResult(BigInteger.valueOf(0), arrayx, arrayy);

        if (result != null)
            System.out.println("Le secret reconstitue est le suivant : " + result);
        else
            System.out.println("Une erreur s'est produite pendant la reconstrucion du secret, veuillez entrer des coordonnees valides");
    }


    /**
     * Methode de serialisation de notre secret, de la prime, du nombre de parts du secret et le nombre maximum de parts du secret
     *
     * @throws IOException
     */
    private void saveSecret() throws IOException {

        if (isSecret()) {
            File file;
            file = new File("serialiseSecret.ser");
            file.createNewFile();


            FileOutputStream fos = new FileOutputStream(file);

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeChars(secret.toString() + "TheEnd");
            oos.writeChars(datas.getMinParts() + "TheEnd");
            oos.writeChars(datas.getParts() + "TheEnd");
            oos.writeChars(datas.getPrime().toString() + "TheEnd");

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

            System.out.println("Les donnees ont ete sauvegardees avec succes");
        } else {
            System.out.println("Aucune donnee ne peut etre sauvegardee");
        }

        System.out.println("Fermeture du programme");

    }

    /**
     * Methode de deserialisation si il existe de notre fichier serialiseSecret.ser
     */
    private void deserialisation() {

        File file = new File("serialiseSecret.ser");

        ObjectInputStream ois;
        String serial;

        try {
            ois = new ObjectInputStream(new FileInputStream(file));

            serial = "";

            while (ois.available() > 0) {
                char c = ois.readChar();
                serial += c;
            }

            if (serial != null) {

                String[] result = serial.split("TheEnd");

                this.secret = new BigInteger(result[0]);
                int minParts = Integer.parseInt(result[1]);
                int parts = Integer.parseInt(result[2]);
                BigInteger prime = new BigInteger(result[3]);
                datas = new MetaData(minParts, parts, prime);

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

            } else return;

        } catch (IOException e) {
            System.out.println("Aucune donnee n'a ete trouvee");
        }
    }
}