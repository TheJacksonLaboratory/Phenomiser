package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.ontology.algo.InformationContentComputation;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreSamplingOptions;
import org.monarchinitiative.phenol.ontology.scoredist.SimilarityScoreSampling;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ComputedResources extends AbstractResources {

    private static Logger logger = LoggerFactory.getLogger(ComputedResources.class);

    private Properties properties;

    private int numThreads = 4; //retrieve from properties

    private boolean cache = true; //cache computed score by default. overwrite from properties

    private String cachingPath = System.getProperty("user.home");

    private String folder = "Phenomiser_data";

    public ComputedResources(HpoParser hpoParser, DiseaseParser diseaseParser, Properties properties) {
        super(hpoParser, diseaseParser);
        this.properties = properties;
    }

    @Override
    public void init() {
        super.defaultInit();

        //init icMap
        logger.trace("information content map initiation started");
        icMap = new InformationContentComputation(hpo).computeInformationContent(hpoTermIdToDiseaseIds);
        logger.trace("information content map initiation success");

        //init Resnik similarity precomputation
        logger.trace("Resnik similarity precomputation started");
        final PrecomputingPairwiseResnikSimilarity pairwiseResnikSimilarity =
                new PrecomputingPairwiseResnikSimilarity(hpo, icMap, numThreads);

        resnikSimilarity = new ResnikSimilarity(pairwiseResnikSimilarity, false);

        logger.trace("Resnik similarity precomputation success");

        // score distribution
        logger.trace("score distribution computation started");
        ScoreSamplingOptions samplingOption = new ScoreSamplingOptions();
        samplingOption.setMinNumTerms(2);
        samplingOption.setMaxNumTerms(4);
        SimilarityScoreSampling sampleing = new SimilarityScoreSampling(hpo, resnikSimilarity, samplingOption);
        scoreDistributions = sampleing.performSampling(diseaseIdHashToHpoTerms);
        logger.trace("score distribution computation success");

        if (cache) {
            logger.trace("caching started");
            String path = cachingPath + File.separator + folder;
            logger.info("writing to: " + path);
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
                out.writeObject(icMap);
                out.writeObject(resnikSimilarity);
                out.writeObject(scoreDistributions);
            } catch (FileNotFoundException e) {
                logger.error("File not found: " + path);
                logger.error("caching failed");
            } catch (IOException e) {
                logger.error("IO exception");
                logger.error("caching failed");
            }


        }
    }
}
