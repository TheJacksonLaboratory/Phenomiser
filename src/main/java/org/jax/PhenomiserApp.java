package org.jax;

import org.apache.commons.cli.*;
import org.jax.services.PValueCalculator;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.monarchinitiative.phenol.stats.PValue;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class PhenomiserApp {

    private static Logger logger = LoggerFactory.getLogger(PhenomiserApp.class);
    /**
     * Number of threads to use.
     */
    private final int numThreads = 4;

    /**
     * Path to hp.obo file to read.
     */
    private String pathHpObo;

    /**
     * Path to {@code phenotype.hpoa}
     */
    private String pathPhenotypeHpoa;

    private static HpoOntology hpo;

    private static Map<TermId, HpoDisease> diseaseMap;

    private static Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds;

    private static Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds;

    private static Map<TermId, Double> icMap;

    private static PrecomputingPairwiseResnikSimilarity precomputingPairwiseResnikSimilarity;

    private static ResnikSimilarity resnikSimilarity;

    private static Map<Integer, List<TermId>> diseaseIdHashToHpoTerms;

    private static Map<Integer, TermId> diseaseIdHashToDisease;

    private static Map<Integer, ScoreDistribution> scoreDistributions;


    public static void init(String args) {

        //hpo = getHpo();


    }

    public static void query(List<TermId> queryTerms, List<DiseaseDB> dbs) {

        if (queryTerms == null || dbs == null || queryTerms.isEmpty() || dbs.isEmpty()) {
            return;
        }

        //a user might just want to select "OMIM", "OPHANET" or "MONDO" diseases
        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();
        logger.info(filter);

        //for each disease, calculate the similarity score with query terms
        Map<Integer, Double> similarityScores = new HashMap<>();
        diseaseIdHashToHpoTerms.entrySet().stream()
                .filter(e -> diseaseIdHashToDisease.get(e.getKey()).getPrefix().contains(filter))
                .forEach(e -> similarityScores.put(e.getKey(), resnikSimilarity.computeScore(queryTerms, e.getValue())));

        //estimate p values for each disease

        PValueCalculator pValueCalculator = new PValueCalculator(scoreDistributions, similarityScores, diseaseIdHashToDisease, queryTerms);

        //p value multi test correction

        BenjaminiHochberg bhFDR = new BenjaminiHochberg();
        Map<TermId, PValue> adjusted = bhFDR.adjustPValues(pValueCalculator);

        //TODO: write out adjusted
        System.out.println(adjusted.size());

        //done


    }

    public static void main( String[] args ) {

        //set up command line options
        Options options = OptionsFactory.getInstance();

        HelpFormatter formatter = new HelpFormatter();

        //parse and handle command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                formatter.printHelp("Phenomiser", options);
            }

            if (commandLine.hasOption("db")) {
                System.out.println("db: " + options.getOption("db"));
            }

            if (commandLine.hasOption("hpo")) {
                System.out.println("load hpo");
            }

            if (commandLine.hasOption("da")) {
                System.out.println("load disease annotations");
            }

            if (commandLine.hasOption("q")) {
                query(null, null);
                System.out.println("start querying");
            }

            //exit if requested
            if (commandLine.hasOption("exit")) {
                System.out.println("exiting");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
