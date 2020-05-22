package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.utils.ObservableMap;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.algo.InformationContentComputation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ComputedResources extends AbstractResources {

    private static Logger logger = LoggerFactory.getLogger(ComputedResources.class);

    private Properties properties;

    private final int numThreads; //retrieve from properties
    /** cache computed score by default. Value will be extracted from properties. */
    private final boolean cache;

    private String cachingPath = System.getProperty("user.home") + File.separator + "Phenomiser_data";

    private final boolean debug;

    private int sampleMin = 1;
    private int sampleMax = 10;


    /**
     * note: the init() method must be called before injecting hpoParser and diseaseParser
     * @param diseaseParser
     * @param properties pass in settings to overwrite default settings
     * @param debug if true, only precompute the similarity score distributions between 3 HPO terms and 100 diseases.
     */
    public ComputedResources(DiseaseParser diseaseParser, @Nullable Properties properties, boolean debug) {
        super(diseaseParser);
        this.properties = properties;
        try {
            this.numThreads = Integer.parseInt(this.properties.getProperty("numThreads", "4"));
            this.cache = Boolean.parseBoolean(this.properties.getProperty("cache", "true"));
            this.cachingPath = this.properties.getProperty("cachingPath", cachingPath);
            this.sampleMin = Integer.parseInt(this.properties.getProperty("sampleMin", "1"));
            this.sampleMax = Integer.parseInt(this.properties.getProperty("sampleMax", "10"));
            if (this.sampleMin > this.sampleMax) {
                throw new PhenolRuntimeException("sampling min > sampling max");
            }
        } catch (Exception e) {
            throw new PhenolRuntimeException("Error encountered while setting properties.");
        }
        this.debug = debug;

        super.scoreDistributions = new ObservableMap<>(new BiConsumer<Integer, ScoreDistribution>() {
            @Override
            public void accept(Integer numHPO, ScoreDistribution scoreDistribution) {
                if(!cache) {
                    return;
                }
                try {
                    createIfNotExists(cachingPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("caching failed: folder cannot be created.");
                    return;
                }
                String name = String.format("%d_term.scoreDistribution.binary", numHPO);
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cachingPath + File.separator + name))) {
                    out.writeObject(scoreDistribution);
                    logger.trace("caching score distributions success");
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("IO exception");
                    logger.error("caching score distributions failed");
                }
            }
        });
    }

    @Override
    public void init() {
        //init icMap
        logger.trace("information content map initiation started");
        icMap = new InformationContentComputation(hpo).computeInformationContent(hpoTermIdToDiseaseIdsWithExpansion);
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
        samplingOption.setMinNumTerms(sampleMin);
        samplingOption.setMaxNumTerms(sampleMax);

        SimilarityScoreSampling scoreSampling;
        if (this.debug) {

            // The following reduces the disease map to a smaller number of examples

            List<TermId> keysToRetain =  diseaseIdToHpoTermIdsNoExpansion.keySet().stream()
                    .limit(50).collect(Collectors.toList());
            Map<TermId, Collection<TermId>> subset = new HashMap<>();
            for (TermId t : keysToRetain) {
                subset.put(t, diseaseIdToHpoTermIdsNoExpansion.get(t));
            }
            scoreSampling = new SimilarityScoreSampling(hpo, resnikSimilarity, samplingOption,subset);
            scoreDistributions.putAll(scoreSampling.performSampling());
        } else {
            for (int i = samplingOption.getMinNumTerms(); i <= samplingOption.getMaxNumTerms(); i++) {
                ScoreSamplingOptions newoption = new ScoreSamplingOptions();
                newoption.setNumThreads(numThreads);
                newoption.setMinNumTerms(i);
                newoption.setMaxNumTerms(i);
                scoreSampling = new SimilarityScoreSampling(hpo, resnikSimilarity, newoption,diseaseIdToHpoTermIdsNoExpansion);
                scoreDistributions.putAll(scoreSampling.performSampling());
            }
        }

        logger.trace("score distribution computation success");

        if (cache) {
            logger.trace("caching started");
            try {
                createIfNotExists(cachingPath);
            } catch (Exception e) {
                logger.error("caching failed: folder cannot be created.");
                return;
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
                e.printStackTrace();
                logger.error("caching score distributions failed");
            }
        }
    }

    private void createIfNotExists(String path) throws IOException {
        if (!Files.exists(Paths.get(path))){
            Files.createDirectories(Paths.get(path));
        }
    }
}
