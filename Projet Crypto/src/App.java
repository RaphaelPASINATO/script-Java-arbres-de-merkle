import java.nio.file.*;
import java.util.Scanner; 
import java.io.IOException;


public class App {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Saisie du chemin du fichier");
        System.out.println("par exemple C:\\"+"\\Users\\"+"\\raphael\\"+"\\Desktop\\"+"\\projet crypto\\"+"\\grosFichier.txt");
        System.out.print("Chemin : ");
        String file = sc.nextLine();
        //découpage du fichier en octets
        byte[] tab = {0};
        //String file = "C:\\Users\\raphael\\Desktop\\projet crypto\\grosFichier.txt";
        try {
            byte[] temp = Files.readAllBytes(Paths.get(file));
            tab = temp;

         
      
        } catch (IOException err) {
            System.out.println(err.toString());
        }
      
        int longueurListe = tab.length;

        //calcul du nombre de blocs nécessaire
        int nbBlocs = longueurListe / 512;
        if (longueurListe % 512 > 0){
            nbBlocs++;
        }

        int[][] blocs = new int[nbBlocs][512];

        //remplissage des blocs
        int cptBloc = 0;
        int cptByte = 0;
        for (int nbByte = 0; nbByte < longueurListe;nbByte++) {
            cptBloc = nbByte /512;
            cptByte = nbByte % 512;
            blocs[cptBloc][cptByte] = tab[nbByte];

        }
        //System.out.println(cptByte);
        //bourrage du dernier bloc si possible
        boolean premierBourrage = true;
        if (cptByte <511) {
            for(int nbBourrage = cptByte+1;nbBourrage < 512; nbBourrage++ ) {
                if (premierBourrage == true){
                    blocs[cptBloc][nbBourrage] = 1;
                    premierBourrage = false;
                }
                else {
                    blocs[cptBloc][nbBourrage] = 0;
                }
            }
        }

        //calcule de la racine de l’arbre de Merkle

        //calul des empreintes des premiers blocs
        int[][] empreintesPremiersBlocs = new int[nbBlocs][5];
        for (int i = 0; i < nbBlocs;i++) {
            empreintesPremiersBlocs[i] = TTH(blocs[i]);
        }

        
        int nbEmpreintes = empreintesPremiersBlocs.length;        
        boolean premierParcours = true;
        int[][] empreinte  = new int[1][5];
        //boucle qui calcule les empreintes de la deuxième couche jusqu'à la derniere empreinte
        while (nbEmpreintes > 1) {
            if (nbEmpreintes % 2 == 1){
                nbEmpreintes = nbEmpreintes + 1;
            }
            nbEmpreintes = nbEmpreintes / 2;
            if (premierParcours == true) {
                empreinte  = new int[nbEmpreintes][5];
                empreinte  = calculEmpreinte(empreintesPremiersBlocs, nbEmpreintes);
            }
            else {
                int[][] empreinteTemp = calculEmpreinte(empreinte , nbEmpreintes);
                empreinte  = new int[nbEmpreintes][5];
                empreinte  = empreinteTemp;
            }
          
        }

        //cas ou il y a un bloc dans le découpage du fichier
        if (empreintesPremiersBlocs.length == 1){
            empreinte = empreintesPremiersBlocs;
        }


        //affichage de l'empreinte finale
        System.out.print("Empreinte du fichier :  ");
        for (int i = 0; i <5; i ++){
            System.out.print(empreinte [0][i] + " ");
        }
    }

    public static int[] TTH(int[] tableau)  {
        //calcul du nombre de blocs
        int longueurTableau = tableau.length;
        int nbBlocs = longueurTableau / 25;
        if (longueurTableau % 25 > 0){
            nbBlocs++;
        }

        //creation des blocs
        int[][][] blocs = new int[nbBlocs][5][5];
        int cptTableau = 0;
        boolean finTableau = true;
        for (int nbB = 0; nbB < nbBlocs;nbB++) {
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5;j++) {
                    if ( cptTableau < tableau.length) {
                        blocs[nbB][i][j] = tableau[cptTableau];
                    }
                    else {
                        if (finTableau == true) {
                            blocs[nbB][i][j] = 32;
                            finTableau = false;
                        }
                        else {
                            blocs[nbB][i][j] = 0;
                        }
                        
                    }
                    cptTableau++;
                }
            }
        }


        //calcul des empreintes 
        int[][] empreinte = new int[nbBlocs][5];
        int somme = 0;
        for (int nbB = 0; nbB < nbBlocs;nbB++) {
            for(int j = 0; j < 5; j++) {
                somme =0;
                for(int i = 0; i < 5;i++) {
                    somme = somme + blocs[nbB][i][j];
                   
                    
                }
                somme = somme % 64;
                empreinte[nbB][j] = somme;
            }
        }
        //décalage des lignes pour l'intégralité des blocs
        int[][][] nouveauxBlocs = new int[nbBlocs][5][5];
        int cptLigne = 0;
        int tbCible = 0;
        for (int nbB = 0; nbB < nbBlocs;nbB++) {
            cptLigne = 0;
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5;j++) {
                    tbCible = (j + cptLigne) % 5;
                    nouveauxBlocs[nbB][i][tbCible] = blocs[nbB][i][j];
                }
                cptLigne++;
            }
        }

        //calcul des empreintes des nouveaux blocs et ajouts des empreintes aux  empreintes courantes
        somme = 0;
        for (int nbB = 0; nbB < nbBlocs;nbB++) {
            for(int j = 0; j < 5; j++) {
                somme =0;
                for(int i = 0; i < 5;i++) {
                    somme = somme + nouveauxBlocs[nbB][i][j];
                   
                    
                }
                empreinte[nbB][j] = (empreinte[nbB][j]+ somme) % 64;
            }
        }

       //calcul de l'empreinte finale 
        int[] empreinteFinale = new int[5];
        for (int nbB = 0; nbB < nbBlocs;nbB++) {
            for(int i = 0; i < 5;i++) {
                empreinteFinale[i] =  ( empreinte[nbB][i] + empreinteFinale[i] ) %64;
            }
        }


        return empreinteFinale;
    }

    //fonction qui retourne des empreintes pour les tableaux données en paramètre
    public static int[][] calculEmpreinte(int[][] tableau, int nbEmpreintes)  { 
        int[][] empreinte = new int[nbEmpreintes][5];
        int cptTableau = 0;
        for (int i = 0; i < nbEmpreintes; i++) {
            if (i == nbEmpreintes-1 && tableau.length % 2 == 1) {
                int[] tableauTemp = new int[5];
                for (int j = 0; j < 5;j++) {
                    tableauTemp[j] = tableau[tableau.length-1][j];
                }
                empreinte[i] = TTH(tableauTemp);
            }
            else {
                int[] tableauTemp = new int[10];
                int cpt =0;
                for (int j = 0; j < 5;j++){
                    tableauTemp[cpt] = tableau[cptTableau][j];
                    cpt++;
                }
                cptTableau++;
                for (int j = 0; j < 5;j++){
                    tableauTemp[cpt] = tableau[cptTableau][j];
                    cpt++;
                }
                cptTableau++;
                empreinte[i] = TTH(tableauTemp);
            }
        }


        return empreinte;
    }

 
}
