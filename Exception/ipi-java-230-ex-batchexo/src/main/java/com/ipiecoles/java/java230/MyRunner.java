package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;
    private static final String REGEX_GRADE_TECHNICIEN = "[1-5]{1}$";

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) throws Exception {
        Stream<String> stream;
        stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        int i;
        i=0;
        for (String ligne : stream.collect(Collectors.toList())){
            i++;
            try {
                processLine(ligne);
            }catch (BatchException e){
                    System.out.println("Ligne " + i +" : " + e.getMessage() + " =>" + ligne);
                }
            }

        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        String[] tab = ligne.split(",");
        if(!ligne.matches("^[MCT]{1}.*")){
            throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
        }
        if (!tab[0].matches(REGEX_MATRICULE)){
            throw new BatchException("la chaîne "+tab[0]+" ne respecte pas l'expression régulière ^[MTC][0-9]{5}$" + ligne.charAt(0));
        }
        if (ligne.matches("^[M]{1}.*")){
            if (!(tab.length == NB_CHAMPS_MANAGER)) {
                throw new BatchException("La ligne manager ne contient pas " +NB_CHAMPS_MANAGER+ " éléments mais " + tab.length);
            }
        }
        else if(ligne.matches("^[C]{1}.*")){
            if (!(tab.length == NB_CHAMPS_COMMERCIAL)) {
                throw new BatchException("La ligne commercial ne contient pas " +NB_CHAMPS_COMMERCIAL+ " éléments " + tab.length);
            }
            try {
                Double.parseDouble(tab[5]);
            } catch (Exception e) {
                throw new BatchException( "Le chiffre d'affaire du commercial est incorrect :" + tab[5]);
            }
            try {
                Double.parseDouble(tab[6]);
            } catch (Exception e) {
                throw new BatchException( "La performance du commercial est incorrecte : " + tab[6]);
            }

        }
        else if(ligne.matches("^[T]{1}.*")){
            if (!(tab.length == NB_CHAMPS_TECHNICIEN)) {
                throw new BatchException("La ligne technicien ne contient pas " +NB_CHAMPS_TECHNICIEN+ " éléments " + tab.length);
            }
            try {
                Integer.parseInt(tab[5]);
            } catch (Exception e) {
                throw new BatchException( "Le grade du technicien est incorrect :" + tab[5]);
            }
            if (!tab[5].matches(REGEX_GRADE_TECHNICIEN)){
                throw new BatchException("Le grade doit être compris entre 1 et 5 : " +tab[5]+ ", technicien : " +ligne.charAt(0));
            }
            if (!tab[6].matches(REGEX_MATRICULE_MANAGER)){
                throw new BatchException("la chaîne "+tab[6]+" ne respecte pas l'expression régulière ^M[0-9]{5}$");
            }

            Employe e =  employeRepository.findByMatricule(tab[6]);
            if (e==null){
                throw new BatchException("Le manager de matricule "+tab[6]+" n'a pas été trouvé dans le fichier ou en base de données");
            }




        }

        try {
            LocalDate d = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(tab[3]);

        } catch (Exception e) {
            throw new BatchException(tab[3] + " ne respecte pas le format de date dd/MM/yyyy");
        }

        try {
            Double.parseDouble(tab[4]);
        } catch (Exception e) {
            throw new BatchException(tab[4] + " n'est pas un nombre valide pour un salaire");
        }



    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO
    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO
    }

}
