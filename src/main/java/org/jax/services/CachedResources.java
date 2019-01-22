package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.ontology.algo.InformationContentComputation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

public class CachedResources extends AbstractResources{

    private static Logger logger = LoggerFactory.getLogger(CachedResources.class);

    private String cachingPath;

    private int numThreads = 4;

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

    }
}
