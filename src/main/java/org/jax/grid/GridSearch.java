package org.jax.grid;

import org.jax.services.AbstractResources;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a demonstration of the likelihood ratio algorithm that uses simulated cases to assess the performance of the
 * algorithm.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class GridSearch {
   // private static final Logger logger = LogManager.getLogger();
    private AbstractResources resources;
    /** SHould we exchange the terms with their parents to simulate "imprecise" data entry? */
    private final boolean useImprecision;

    /** Number of HPO terms to use for each simulated case. */
    private  int n_terms_per_case;
    /** Number of "noise" (unrelated) HPO terms to use for each simulated case. */
    private  int n_noise_terms;
    /** Number of cases to simulate. */
    private  int n_cases_to_simulate;

    private Random r;


    /**
     * Perform a grid search with the indicated number of simulated cases. We will simulate from
     * one to ten HPO terms with from zero to four "random" (noise) terms, and write the results to
     * a file that can be input by R.
     * @param n_cases Number of cases to simulate
     * @param imprecision if true, use "imprecision" to change HPO terms to a parent term
     */
    public GridSearch(AbstractResources resources, int n_cases, int n_diseaseTerm, int n_noiseTerm, boolean imprecision) {
        this.resources = resources;
        this.n_cases_to_simulate=n_cases;
        this.n_terms_per_case = n_diseaseTerm;
        this.n_noise_terms = n_noiseTerm;
        this.useImprecision=imprecision;
        this.r = new Random();
    }

    public double[][] run() {
        double[][] rankmatrix = new double[n_terms_per_case][n_noise_terms];
        String outfilename=String.format("grid_%d_cases.R", n_cases_to_simulate);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfilename))) {
            for (int i = 2; i < n_terms_per_case; i++) {
                for (int j = 0; j < n_noise_terms; j++) {
                    PhenotypeOnlyHpoCaseSimulator simulator = new PhenotypeOnlyHpoCaseSimulator(resources, n_cases_to_simulate, i+1 , j, useImprecision);
                    simulator.simulateCases();
                    rankmatrix[i][j] = simulator.getProportionAtRank1();
                    System.out.println(String.format("terms: %d; noise terms: %d; percentage at rank 1: %.2f\n",
                        i,
                        j,
                        100.00 * rankmatrix[i][j]));
                }
            }
            // output a file that we will input as an R data frame.
            // see the read-the-docs documentation for how to create a graphic in R with this
            writer.write("library(plot3D)\n");
            writer.write("mat <- matrix(\n");

        List<Double> values = new ArrayList<>();
        for (int j = 0; j < n_terms_per_case; j++) {
            for (int i = 0; i < n_noise_terms; i++) {
               values.add(rankmatrix[i][j]);
            }
        }

        String valuestring = values.stream().map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(valuestring);
        writer.write("c(" + valuestring + "),\n");
        writer.write("nrow=5,\nncol=10,\nbyrow=TRUE)\n");
        writer.write("hist3D(z = mat, scale = FALSE, expand = 0.5, bty = \"g\", phi = 20,\n" +
                "      col = \"#0072B2\", border = \"black\", shade = 0.2, ltheta = 99,\n" +
                "      space = 0.3, ticktype = \"detailed\", d = 2)");

        }
        catch (IOException e) {
            try {
                throw new Exception();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }


        return rankmatrix;
    }

    /*public void write(double[][] rankmatrix, Writer writer) throws Exception{
        for (int i = 0; i < rankmatrix.length; i++){
            for (int j = 0; j < rankmatrix[i].length; j++){
                writer.write(Double.toString(rankmatrix[i][j]));
                writer.write("\t");
            }
            writer.write("\n");
        }
//        writer.write("library(plot3D)\n");
//        writer.write("mat <- matrix(\n");
//
//        List<Double> values=new ArrayList<>();
//        for (int j = 0; j < randomtermnumber.length; j++) {
//            for (int i = 0; i < termnumber.length; i++) {
//                values.add(Z[i][j]);
//            }
//        }
//        String valuestring=values.stream().map(String::valueOf).collect(Collectors.joining(","));
//        writer.write("c(" + valuestring +"),\n");
//        writer.write("nrow=5,\nncol=10,\nbyrow=TRUE)\n");
//        writer.write("hist3D(z = mat, scale = FALSE, expand = 0.5, bty = \"g\", phi = 20,\n" +
//                "      col = \"#0072B2\", border = \"black\", shade = 0.2, ltheta = 99,\n" +
//                "      space = 0.3, ticktype = \"detailed\", d = 2)");
   // }

    /**
     * Perform a grid search over varying numbers of terms and random terms
     * both with and without moving the terms to parent terms (imprecision).

     */
    //public void gridsearch()  {

//        int[] termnumber = {1,2,3,4,5,6,7,8,9,10};
//        int[] randomtermnumber = {0,1,2,3,4};
//        String outfilename= String.format("grid_%d_cases_%s.R",
//                n_cases_to_simulate_per_run,
//                useImprecision?"imprecise":"precise"
//                );
//        double[][] Z;
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfilename))) {
//            Z = new double[termnumber.length][randomtermnumber.length];
//
//            for (int i = 0; i < termnumber.length; i++) {
//                for (int j = 0; j < randomtermnumber.length; j++) {
//                    PhenotypeOnlyHpoCaseSimulator simulator = new PhenotypeOnlyHpoCaseSimulator(resources, n_cases_to_simulate, n_terms_per_case, n_noise_terms, useImprecision);
//                    simulator.simulateCases();
//                    Z[i][j] = simulator.getProportionAtRank1();//simulator.getProportionAtRank1();
//                    System.out.println(String.format("terms: %d; noise terms: %d; percentage at rank 1: %.2f\n",
//                            termnumber[i],
//                            randomtermnumber[j],
//                            100.00 * Z[i][j]));
//                }
//            }
//            // output a file that we will input as an R data frame.
//            // see the read-the-docs documentation for how to create a graphic in R with this
//            writer.write("library(plot3D)\n");
//            writer.write("mat <- matrix(\n");
//
//            List<Double> values=new ArrayList<>();
//            for (int j = 0; j < randomtermnumber.length; j++) {
//                for (int i = 0; i < termnumber.length; i++) {
//                    values.add(Z[i][j]);
//                }
//            }
//            String valuestring=values.stream().map(String::valueOf).collect(Collectors.joining(","));
//            writer.write("c(" + valuestring +"),\n");
//            writer.write("nrow=5,\nncol=10,\nbyrow=TRUE)\n");
//            writer.write("hist3D(z = mat, scale = FALSE, expand = 0.5, bty = \"g\", phi = 20,\n" +
//                    "      col = \"#0072B2\", border = \"black\", shade = 0.2, ltheta = 99,\n" +
//                    "      space = 0.3, ticktype = \"detailed\", d = 2)");
//        } catch (IOException e) {
//            //throw new Exception("I/O error: " + e.getMessage());
//        }

 //   }
}
