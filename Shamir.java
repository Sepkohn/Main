package Main;

import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;


public class Shamir implements Serializable {

    private static final Long serialVersionUID = 1L;

    Scanner scan = new Scanner(System.in);

    private BigInteger secret;


    private BigInteger[] xparts;
    private BigInteger[] yparts;
    private BigInteger[] randoms;
    MetaData datas;


    /**
     * @author Valentin Haenggeli
     * @author Quentin Beeckmans
     * <h1>Donnee</h1>
     * <p>Écrire un logiciel permettant le partage d’un secret (Shamir's secret sharing étudié pendant le cours).
     * Le secret est une chaîne de bits aléatoires (on peut supposer que ce nombre est un multiple de 8).
     * Ce logiciel est par exemple utilisé pour protéger des clés cryptographiques (de 128 à 4096 bits). </p>
     * */


    /**
     * Début de l'application.
     * Vérification automatique si un fichier serialisé contenant les parts existe et si oui, le déserialise.
     * Proposition des choix.
     * @throws IOException
     */
    public void appMethods() throws IOException {

        if (secret == null) {
            deserialisation();

        }


        int choice;

        do {
            System.out.println("Bienvenu sur Shamir's secret, Veuillez faire votre choix :");
            System.out.println("1 - Création d'un secret");
            System.out.println("2 - Ajout de parts");
            System.out.println("3 - Renouveller parts");
            System.out.println("4 - Retrouver le secret");
            //A supprimer une fois le test effectué
            System.out.println("5 - imprimer le secret");
            System.out.println("6 - imprimer les parts");
            System.out.println("0 - fermeture du programme");
            choice = scan.nextInt();
        }
        //A supprimer >4 et non 6
        while (choice < 0 || choice > 6);

        appMethods(choice);
    }

    /**
     * Envoie en méthode le(s) choix de l'utilisateur
     * @param userChoice récupère le choix de l'utilisateur
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
                //Methode de suppression
                updateParts();
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
                displayParts(0);
                break;

            case (0):
                saveSecret();
                return;
        }

        appMethods();
    }

    /**
     * Méthode de création de secret.
     * @throws IllegalArgumentException -- "La clé doit être entre 128 et 4096 bits" et "Le nombre de parts doit être supérieur ou égal au minimum"
     * @throws ArithmeticException -- "La clé doit être transformable en bytes (divisible par 8)"
     *
     */
    private void setSecret() {
        if (isSecret()) {
            System.out.println("Un secret a déjà été défini !! impossible d'en créer un nouveau");
            return;
        }

        System.out.print("Indiquez le nombre de bits : ");
        int bitLength = scan.nextInt();

        //tests
        if (bitLength < 128 || bitLength > 4096)
            throw new IllegalArgumentException("La clé doit être entre 128 et 4096 bits");
        if (bitLength % 8 != 0)
            throw new ArithmeticException("La clé doit être transformable en bytes (divisible par 8)");


        System.out.print("Indiquez le nombre parts minimum : ");
        int minParts = scan.nextInt();
        System.out.print("Indiquez le nombre de parts voulues : ");
        int parts = scan.nextInt();

        if (parts < minParts)
            throw new IllegalArgumentException("Le nombre de parts doit être supérieur ou égal au minimum");

        setSecret(bitLength, minParts, parts);
    }

    /**
     * La création du secret et de ses parts en passant les paramètres demandés
     * @param bitLength
     * @param minParts
     * @param parts
     */
    private void setSecret(int bitLength, int minParts, int parts) {

        datas = new MetaData(minParts, parts);

        generateRandomKey(bitLength / 8);



        xparts = new BigInteger[parts];
        for (int i = 0; i < xparts.length; i++) {

            xparts[i] = BigInteger.valueOf(i+1);
        }

        yparts = new BigInteger[parts];

        for (int i = 0; i < yparts.length; i++) {
            findFirstY(i);
        }

        //imprimer les parts + meta
        System.out.println("Voici les differentes parts : ");
        displayParts(0);

    }

    /**
     * Méthode permetant d'afficher les parts de notre secret
     * @param beginning
     */
    private void displayParts(int beginning) {

        if(!isSecret())
            return;

        for (int i = beginning; i < datas.getParts(); i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }

    /**
     * La méthode trouveY génère l'ordonnée Y pour la génération de la part en BigInteger pour chaque paramètre indice.
     * @param index -- est la valeur de l'ordonnée X
     */
    private void findY(int index) {
        BigInteger temp = new BigInteger("0");

        BigInteger valueX = xparts[index];

        yparts[index] = datas.findResult(valueX, xparts, yparts);


    }
    private void findFirstY(int index) {
        BigInteger temp = new BigInteger("0");

        BigInteger valueX = xparts[index];

        if(randoms==null){
            randoms = datas.generateRandoms(getByteLength());
            randoms[0]=secret;
        }

        for (int i = 0; i <randoms.length; i++) {
            temp = temp.add(randoms[i].multiply(valueX.pow(i)));
        }

        yparts[index] = temp.mod(datas.getPrime());

    }



    /**
     * Génération d'un secret et du nombre premier
     * @param byteLength -- nombre de bits demandé par l'utilisateur divisé par 8
     * @throws IllegalArgumentException
     */
    private void generateRandomKey(int byteLength) throws IllegalArgumentException {
        do {
            secret = datas.randomNumber(byteLength);
        }
        while(secret.bitLength()<byteLength*8);

        BigInteger prime = secret.nextProbablePrime();

        datas = new MetaData(datas.getMinParts(), datas.getParts(), prime);

        if (prime.bitLength() > byteLength * 8) {
            generateRandomKey(byteLength);
        }

        randoms = datas.generateRandoms(getByteLength());
        randoms[0]=secret;

    }






    /**
     * Méthode d'initialisation d'ajout de parts du secret
     */
    private void AddPart() {
        if (secret != null) {
            System.out.print("Indiquez le nombre de parts à ajouter : ");
            int newNumber = scan.nextInt();

            if (newNumber < 0)
                throw new IllegalArgumentException("Veuillez entrer un nombre positif");

            AddPart(newNumber);
        }

    }

    /**
     * Méthode d'ajout de parts du secret avec en paramètre le nombre demandé par l'utilisateur
     * @param newNumber le nombre de parts à ajouter
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
        //transfert de tableau
        xparts = partx;

        BigInteger[] party = new BigInteger[parts];
        for (int i = 0; i < oldNumber; i++) {
            party[i] = yparts[i];
        }

        //transfert de tableau
        yparts = party;

        for (int i = oldNumber; i<parts;i++){
            findY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = oldNumber; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

        datas=new MetaData(datas.getMinParts(), parts, datas.getPrime());

    }
    /**
     * Methode d'initialisation de renouvellement des parts déja créées
     * et propose de modifier le nombre de parts minimum pour la reconstruction du secret.
     */
    private void updateParts() {
        if (isSecret()) {

            String response="";

            do {
                System.out.print("Voullez vous changer de seuil? (y-n) ");
                response = scan.next();
            }while(!response.equals("y") && !response.equals("n"));

            switch (response) {

                case ("y"):
                    System.out.println("Quel est le nouveau seuil ? ");
                    int newNumber = scan.nextInt();

                    if (newNumber < 0)
                        throw new IllegalArgumentException("Veuillez entrer un nombre positif");

                    if(newNumber>datas.getParts()) {
                        System.out.println("Veuillez choisir un seuil inférieur au nombre de part déjà créées ou ajoutez de nouvelles parts en conséquence.");
                        System.out.println();
                        return;
                    }
                    if(newNumber == 1) {
                        System.out.println("Veuillez choisir un seuil supérieur à 1.");
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
    }
    /**
     * Methode de mise à jour des parts
     */
    private void updatePartS() {

        int parts = datas.getParts();

        randoms = datas.generateRandoms(getByteLength());
        randoms[0]=secret;

        for (int i = 0; i < parts; i++) {
            findFirstY(i);
        }

        System.out.println("Voici les nouvelles parts : ");
        for (int i = 0; i < parts; i++) {
            System.out.println("No : " + (i + 1) + " // X = " + xparts[i] + " et Y = " + yparts[i]);
        }

    }

    /**
     * Methode de vérification si le secret existe ou pas
     * @return vrai si le secret existe
     */
    private boolean isSecret() {
        if(secret==null)
            return false;
        else
            return true;
    }

    private int getByteLength(){
        return secret.bitLength()/8;
    }

    /**
     * Methode pour retrouver notre secret en fonction des différentes parts nécessaire pour composer la fonction de Lagrange
     */
    private void findSecret() {

        if(!isSecret())
            return;

        int minParts = datas.getMinParts();

        BigInteger[] arrayx = new BigInteger[minParts];
        BigInteger[] arrayy = new BigInteger[minParts];


        if (!isSecret()) {
            System.out.println("Aucun secret n'a été crée, veuillez recommencer");
            return;
        }

        System.out.println("Le nombre de part pour reconstituer secret est de " + minParts);


        for (int i = 0; i < minParts; i++) {

            System.out.println("Entrez la part x No " + (i + 1) + " : ");
            arrayx[i] = scan.nextBigInteger();

            System.out.println("Entrez la part y No " + (i + 1) + " : ");
            arrayy[i] = scan.nextBigInteger();
        }

        BigInteger result = datas.findResult(BigInteger.valueOf(0), arrayx, arrayy);

        if(result!=null)
            System.out.println("Le secret reconstitué est le suivant : " + result);
        else
            System.out.println("Une erreur s'est produite pendant la reconstrucion du secret, veuillez entrer des coordonnées valides");
    }


    /**
     * Methode de serialisation de notre secret, de la prime, du nombre de parts du secret et le nombre maximum de parts du secret
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

            System.out.println("Les données ont été sauvegardées avec succès");
        } else {
            System.out.println("Aucune donnée ne peut être sauvegardée");
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
                int minParts=Integer.parseInt(result[1]);
                int parts = Integer.parseInt(result[2]);
                BigInteger prime = new BigInteger(result[3]);
                datas = new MetaData(minParts,parts,prime);

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
            System.out.println("Aucune donnée n'a été trouvé");
        }
    }
}