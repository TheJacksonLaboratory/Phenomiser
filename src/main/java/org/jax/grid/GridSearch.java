package org.jax.grid;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.lr2pg.exception.Lr2pgException;
import org.monarchinitiative.lr2pg.hpo.PhenotypeOnlyHpoCaseSimulator;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getParentTerms;

/**
 * This is a demonstration of the likelihood ratio algorithm that uses simulated cases to assess the performance of the
 * algorithm.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class GridSearch {
   // private static final Logger logger = LogManager.getLogger();
    /** key: a disease id such as OMIM:654321; value: coirresponding {@link HpoDisease} object. */
    private final Map<TermId, HpoDisease> diseaseMap;
    /** Reference to HPO ontology object. */
    private final Ontology ontology;
    /** Number of cases to be simulated for any given parameter combination */
    private final int n_cases_to_simulate_per_run;
    /** SHould we exchange the terms with their parents to simulate "imprecise" data entry? */
    private final boolean useImprecision;

    /** Number of HPO terms to use for each simulated case. */
    private  int n_terms_per_case;
    /** Number of "noise" (unrelated) HPO terms to use for each simulated case. */
    private  int n_noise_terms;
    /** Number of cases to simulate. */
    private  int n_cases_to_simulate;

    private boolean addTermImprecision=false;

    /** A list of all HPO term ids in the Phenotypic abnormality subontology. */
    private  ImmutableList<TermId> phenotypeterms;


    /**
     * Perform a grid search with the indicated number of simulated cases. We will simulate from
     * one to ten HPO terms with from zero to four "random" (noise) terms, and write the results to
     * a file that can be input by R.
     * @param ontology Reference to the HPO ontology
     * @param diseaseMap Map of HPO Disease models
     * @param n_cases Number of cases to simulate
     * @param imprecision if true, use "imprecision" to change HPO terms to a parent term
     */
    public GridSearch(Ontology ontology, Map<TermId, HpoDisease> diseaseMap, int n_cases, boolean imprecision) {
        this.ontology=ontology;
        this.diseaseMap=diseaseMap;
        this.n_cases_to_simulate_per_run=n_cases;
        this.useImprecision=imprecision;
    }

    /** This array will hold the TermIds from the disease map in order -- this will allow us to
     * get random indices for the simulations. */
    private TermId[] termIndices;

    public TermId getNextRandomDisease(Random r) {
        int i = r.nextInt(diseaseMap.size());
        TermId tid = termIndices[i];
        HpoDisease disease = diseaseMap.get(tid);
        while (disease.getPhenotypicAbnormalities().size() < this.n_terms_per_case) {
            i = r.nextInt(diseaseMap.size());
            tid = termIndices[i];
            disease = diseaseMap.get(tid);
        }
        return tid;
    }


    /**
     * This is a term that was observed in the simulated patient (note that it should not be a HpoTermId, which
     * contains metadata about the term in a disease entity, such as overall frequency. Instead, we are simulating an
     * individual patient and this is a definite observation.
     * @return a random term from the phenotype subontology.
     */
    private TermId getRandomPhenotypeTerm() {
        int n=phenotypeterms.size();
        int r = (int)Math.floor(n*Math.random());
        return phenotypeterms.get(r);
    }

    /** @return a random parent of term tid. */
    private TermId getRandomParentTerm(TermId tid) {
        Set<TermId> parents = getParentTerms(ontology,tid,false);
        int r = (int)Math.floor(parents.size()*Math.random());
        int i=0;
        return (TermId)parents.toArray()[r];
    }


    /**
     * This creates a simulated, phenotype-only case based on our annotations for the disease
     * @param disease Disease for which we will simulate the case
     * @return HpoCase object with a randomized selection of phenotypes from the disease
     */
    private List<TermId> getRandomTermsFromDisease(HpoDisease disease) {
        int n_terms=Math.min(disease.getNumberOfPhenotypeAnnotations(),n_terms_per_case);
        int n_random=Math.min(n_terms, n_noise_terms);// do not take more random than real terms.
       // logger.trace("Creating simulated case with n_terms="+n_terms + ", n_random="+n_random);
        // the creation of a new ArrayList is needed because disease returns an immutable list.
        List<HpoAnnotation> abnormalities = new ArrayList<>(disease.getPhenotypicAbnormalities());
        ImmutableList.Builder<TermId> termIdBuilder = new ImmutableList.Builder<>();
        Collections.shuffle(abnormalities); // randomize order of phenotypes
        // take the first n_random terms of the randomized list
        abnormalities.stream().limit(n_terms).forEach(a-> termIdBuilder.add(a.getTermId()));
        // now add n_random "noise" terms to the list of abnormalities of our case.
        for(int i=0;i<n_random;i++){
            TermId t = getRandomPhenotypeTerm();
            if (addTermImprecision) {
                t = getRandomParentTerm(t);
            }
            termIdBuilder.add(t);
        }
        return termIdBuilder.build();
    }


    public double simulateCase() {
        for (int i=0;i<n_cases_to_simulate;++i) {
            Random r = new Random();
            int idx = r.nextInt(diseaseMap.size());
        }
            TermId diseaseToSimulate = getNextRandomDisease(r);//termIndices[randomIndices[i]];
    }



    /**
     * Perform a grid search over varying numbers of terms and random terms
     * both with and without moving the terms to parent terms (imprecision).

     */
    public void gridsearch()  {

        int[] termnumber = {1,2,3,4,5,6,7,8,9,10};
        int[] randomtermnumber = {0,1,2,3,4};
        String outfilename= String.format("grid_%d_cases_%s.R",
                n_cases_to_simulate_per_run,
                useImprecision?"imprecise":"precise"
                );
        double[][] Z;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfilename))) {
            Z = new double[termnumber.length][randomtermnumber.length];

            for (int i = 0; i < termnumber.length; i++) {
                for (int j = 0; j < randomtermnumber.length; j++) {

                    Z[i][j] = 42.0;//simulator.getProportionAtRank1();
                    System.out.println(String.format("terms: %d; noise terms: %d; percentage at rank 1: %.2f\n",
                            termnumber[i],
                            randomtermnumber[j],
                            100.00 * Z[i][j]));
                }
            }
            // output a file that we will input as an R data frame.
            // see the read-the-docs documentation for how to create a graphic in R with this
            writer.write("library(plot3D)\n");
            writer.write("mat <- matrix(\n");

            List<Double> values=new ArrayList<>();
            for (int j = 0; j < randomtermnumber.length; j++) {
                for (int i = 0; i < termnumber.length; i++) {
                    values.add(Z[i][j]);
                }
            }
            String valuestring=values.stream().map(String::valueOf).collect(Collectors.joining(","));
            writer.write("c(" + valuestring +"),\n");
            writer.write("nrow=5,\nncol=10,\nbyrow=TRUE)\n");
            writer.write("hist3D(z = mat, scale = FALSE, expand = 0.5, bty = \"g\", phi = 20,\n" +
                    "      col = \"#0072B2\", border = \"black\", shade = 0.2, ltheta = 99,\n" +
                    "      space = 0.3, ticktype = \"detailed\", d = 2)");
        } catch (IOException e) {
            //throw new Exception("I/O error: " + e.getMessage());
        }

    }
}
