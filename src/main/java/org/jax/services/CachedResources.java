package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.ontology.algo.InformationContentComputation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CachedResources extends AbstractResources{

    private static Logger logger = LoggerFactory.getLogger(CachedResources.class);

    private String cachingPath;

    public CachedResources(HpoParser hpoParser, DiseaseParser diseaseParser, String cachePath) {
        super(hpoParser, diseaseParser);
        this.cachingPath = cachePath;
    }

    @Override
    public void init() {
        super.defaultInit();

        String icMapPath = cachingPath + File.separator + "icMap.binary";
        String resnikSimilarityPath = cachingPath + File.separator + "resnikSimilarity.binary";
        String scoreDistributionsPath = cachingPath + File.separator + "scoreDistributions.binary";

//        //init icMap
//        logger.trace("information content map initiation started");
//        icMap = new InformationContentComputation(hpo).computeInformationContent(hpoTermIdToDiseaseIds);
//        logger.trace("information content map initiation success");
//
//        //init Resnik similarity precomputation
//        logger.trace("Resnik similarity precomputation started");
//        final PrecomputingPairwiseResnikSimilarity pairwiseResnikSimilarity =
//                new PrecomputingPairwiseResnikSimilarity(hpo, icMap, numThreads);
//
//        resnikSimilarity = new ResnikSimilarity(pairwiseResnikSimilarity, false);
//
//        logger.trace("Resnik similarity precomputation success");

        //deserialize ic map
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(icMapPath))) {
            logger.trace("deserialize information content map started");
            icMap = (Map<TermId, Double>) in.readObject();
            logger.trace("deserialize information content map success");
        } catch (FileNotFoundException e) {
            logger.trace("deserialize information content map failed");
            logger.error("file not found" + icMapPath);
            logger.trace("information content map initiation started");
            icMap = new InformationContentComputation(hpo).computeInformationContent(hpoTermIdToDiseaseIds);
            logger.trace("information content map initiation success");
        } catch (IOException e) {
            logger.trace("deserialize information content map failed");
            logger.error("io exception occurred");
        } catch (ClassNotFoundException e) {
            logger.trace("deserialize information content map failed");
            logger.error("class not found");
        }

        //deserialize resniksimilarity
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(resnikSimilarityPath))) {
            logger.trace("deserialize ResnikSimilarity started");
            resnikSimilarity = (ResnikSimilarity) in.readObject();
            logger.trace("deserialize ResnikSimilarity success");
        } catch (FileNotFoundException e) {
            logger.trace("deserialize ResnikSimilarity failed");
            logger.error("file not found" + resnikSimilarityPath);
        } catch (IOException e) {
            logger.trace("deserialize ResnikSimilarity failed");
            logger.error("io exception occurred");
        } catch (ClassNotFoundException e) {
            logger.trace("deserialize ResnikSimilarity failed");
            logger.error("class not found");
        }

        //deserialize similarity score distributions
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(scoreDistributionsPath))) {
            logger.trace("deserialize scoreDistributions started");
            scoreDistributions = (Map<Integer, ScoreDistribution>) in.readObject();
            logger.trace("deserialize scoreDistributions success");
        } catch (FileNotFoundException e) {
            logger.trace("deserialize scoreDistributions failed");
            logger.error("file not found" + scoreDistributionsPath);
        } catch (IOException e) {
            logger.trace("deserialize scoreDistributions failed");
            logger.error("io exception occurred");
        } catch (ClassNotFoundException e) {
            logger.trace("deserialize scoreDistributions failed");
            logger.error("class not found");
        }

        //if the above one does not work, meaning we only have individual ScoreDistribution, we read in one by one
        if (scoreDistributions == null || scoreDistributions.isEmpty()) {
            scoreDistributions = new HashMap<>();
            try {
                Files.list(Paths.get(cachingPath))
                        .filter(path -> path.getFileName().toString().matches("^[0-9]{1,2}_term.scoreDistribution.binary"))
                        .filter(path -> {
                            int numHPO = Integer.parseInt(path.getFileName().toString().split("_")[0]);
                            return (numHPO <= 10);
                        })
                        .forEach(path -> {
                            int numHPO = Integer.parseInt(path.getFileName().toString().split("_")[0]);
                            try (ObjectInputStream in =
                                          new ObjectInputStream(new FileInputStream(path.toFile()))) {
                        ScoreDistribution scoreDistribution = (ScoreDistribution) in.readObject();
                        scoreDistributions.put(numHPO, scoreDistribution);
                    } catch (Exception e) {
                                e.printStackTrace();
                        logger.error("error when trying to deserialize " + path.getFileName().toString());
                    }
                });
            } catch (IOException e) {
                logger.error("io exception when trying to find individual score distributions");
            }
        }

    }
}
