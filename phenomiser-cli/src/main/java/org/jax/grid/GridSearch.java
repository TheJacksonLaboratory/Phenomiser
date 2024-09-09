package org.jax.grid;

import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.utils.DiseaseDB;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * This is a demonstration of the Phenomiser algorithm that uses simulated cases to assess the performance of the
 * algorithm.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:aaron.zhang@jax.org">Aaron Zhang</a>
 */
public class GridSearch {
   // private static final Logger logger = LogManager.getLogger();
    private AbstractResources resources;
    /** SHould we exchange the terms with their parents to simulate "imprecise" data entry? */
    private final boolean useImprecision;

    private List<DiseaseDB> diseaseDB;
    /** Number of HPO terms to use for each simulated case. */
    private  int n_terms_per_case;
    /** Number of "noise" (unrelated) HPO terms to use for each simulated case. */
    private  int n_noise_terms;
    /** Number of cases to simulate. */
    private  int n_cases_to_simulate;

    private Random r;


    /**
     * Perform a grid search with the indicated number of simulated cases. We will simulate from
     * one to ten HPO terms with from zero to four "random" (noise) terms, and write the results to the console
     * or a file that can be input by R.
     * @param resources Resources for the simulation
     * @param diseaseDB Limit the simulation to particular disease databases, such as OMIM or ORPHA
     * @param n_cases Number of cases to simulate
     * @param n_diseaseTerm Number of disease terms
     * @param n_noiseTerm Number of noise terms
     * @param imprecision if true, use "imprecision" to change HPO terms to a parent term
     * @param random Can be used to provide a random number generator with specified seed number
     */
    public GridSearch(AbstractResources resources, List<DiseaseDB> diseaseDB, int n_cases, int n_diseaseTerm, int n_noiseTerm, boolean imprecision, @Nullable Random random) {
        this.resources = resources;
        this.diseaseDB = diseaseDB;
        this.n_cases_to_simulate=n_cases;
        this.n_terms_per_case = n_diseaseTerm;
        this.n_noise_terms = n_noiseTerm;
        this.useImprecision=imprecision;
 System.out.println("a non null random is passed" + (random != null));
        this.r = (random == null) ? new Random() : random;
    }

    public double[][] run() {
        double[][] rankmatrix = new double[n_terms_per_case + 1][n_noise_terms + 1];
//        for (int i = 1; i <= n_terms_per_case; i++) {
//            for (int j = 0; j <= n_noise_terms; j++) {
//                PhenotypeOnlyHpoCaseSimulator simulator = new PhenotypeOnlyHpoCaseSimulator(resources, diseaseDB, n_cases_to_simulate, i, j, useImprecision);
//                simulator.simulateCases();
//                rankmatrix[i][j] = simulator.getProportionAtRank1();
//            }
//        }

        for (int t = 1; t <= n_terms_per_case + n_noise_terms; t++){
            //load required score distribution.
            ((CachedResources) resources).cleanAndLoadScoreDistribution(t);
            for (int i = 1; i <= n_terms_per_case; i++){ //signal
                int j = t - i;
                if ((j >= 0) && (j <= n_noise_terms)){
                    PhenotypeOnlyHpoCaseSimulator simulator = new
                            PhenotypeOnlyHpoCaseSimulator(resources,
                            diseaseDB, n_cases_to_simulate, i, j,
                            useImprecision, this.r);
                    simulator.simulateCases();
                    rankmatrix[i][j] = simulator.getProportionAtRank1();
                }
            }
        }
        return rankmatrix;
    }


    public static void write(double[][] rankmatrix, Writer writer) throws IOException {
        writer.write("\t");
        for (int i = 0; i < rankmatrix[0].length; i++) {
            writer.write(i + "\t");
        }
        writer.write("\n");
        for (int i = 1; i < rankmatrix.length; i++){
            writer.write(i + "\t");
            for (int j = 0; j < rankmatrix[i].length; j++){
                writer.write(Double.toString(rankmatrix[i][j]));
                writer.write("\t");
            }
            writer.write("\n");
        }
        writer.close();
    }

}
