package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.ontology.algo.InformationContentComputation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreSamplingOptions;
import org.monarchinitiative.phenol.ontology.scoredist.SimilarityScoreSampling;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ComputedResources extends AbstractResources {

    private static Logger logger = LoggerFactory.getLogger(ComputedResources.class);

    private Properties properties;

    private int numThreads = 4; //retrieve from properties

    private boolean cache = true; //cache computed score by default. overwrite from properties

    private String cachingPath = System.getProperty("user.home") + File.separator + "Phenomiser_data";

    private boolean debug = false;

    private int sampleMin = 1;
    private int sampleMax = 10;

    /**
     * note: the init() method must be called before injecting hpoParser and diseaseParser
     * @param hpoParser
     * @param diseaseParser
     * @param properties pass in settings to overwrite default settings
     * @param debug
     */
    public ComputedResources(HpoParser hpoParser, DiseaseParser diseaseParser, @Nullable Properties properties, @Nullable boolean debug) {
        super(hpoParser, diseaseParser);
        this.properties = properties;
        try {
            this.numThreads = Integer.parseInt(this.properties.getProperty("numThreads", "4"));
            this.cache = Boolean.parseBoolean(this.properties.getProperty("cache", "true"));
            this.cachingPath = this.properties.getProperty("cachingPath", cachingPath);
            this.sampleMin = Integer.parseInt(this.properties.getProperty("sampleMin", "1"));
            this.sampleMax = Integer.parseInt(this.properties.getProperty("sampleMax", "10"));
            if (this.sampleMin > this.sampleMax) {
                System.err.print("sampling min > sampling max");
                System.exit(1);
            }
        } catch (Exception e) {
            logger.error("not all properties are applied.");
        }
        this.debug = debug;
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
        samplingOption.setNumThreads(numThreads);
        if (this.debug) {
            samplingOption.setMinNumTerms(3);
            samplingOption.setMaxNumTerms(3);
        } else {
            samplingOption.setMinNumTerms(sampleMin);
            samplingOption.setMaxNumTerms(sampleMax);
        }

        SimilarityScoreSampling sampleing = new SimilarityScoreSampling(hpo, resnikSimilarity, samplingOption);
        if (this.debug) {

            Map<Integer, List<TermId>> subset = diseaseIndexToHpoTerms.entrySet().stream()
                    .limit(100).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            scoreDistributions = sampleing.performSampling(subset);
        } else {
            scoreDistributions = sampleing.performSampling(diseaseIndexToHpoTerms);
        }

        logger.trace("score distribution computation success");

        if (cache) {
            logger.trace("caching started");
            if (!Files.exists(Paths.get(cachingPath))){
                try {
                    Files.createDirectories(Paths.get(cachingPath));
                } catch (Exception e) {
                    logger.error("caching failed: folder cannot be created.");
                    return;
                }

            }
            logger.info("writing to: " + cachingPath);
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cachingPath + File.separator + "icMap.binary"))) {
                out.writeObject(icMap);
                logger.trace("caching information content success");
            } catch (IOException e) {
                logger.error("IO exception");
                logger.error("caching icMap failed");
            }

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cachingPath + File.separator + "resnikSimilarity.binary"))) {
                out.writeObject(resnikSimilarity);
                logger.trace("caching resnikSimilarity success");
            } catch (IOException e) {
                logger.error("IO exception");
                logger.error("caching resnikSimilarity failed");
            }

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cachingPath + File.separator + "scoreDistributions.binary"))) {
                out.writeObject(scoreDistributions);
                logger.trace("caching score distributions success");
            } catch (IOException e) {
                logger.error("IO exception");
                logger.error("caching score distributions failed");
            }


        }
    }
}
