package org.jax.services;

import org.jax.io.DiseaseParser;
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

    private final Integer n_terms_in_query;

    /**
     * Use this constructor if we know we are analyzing a query with a specific number of query terms.
     * Then there is no need to load all of the cached score distribution files.
     * @param diseaseParser
     * @param cachePath
     * @param n_terms
     */
    public CachedResources(DiseaseParser diseaseParser,
                           String cachePath, Integer n_terms) {
        super(diseaseParser);
        this.cachingPath = cachePath;
        this.n_terms_in_query = n_terms;
    }

    public CachedResources(DiseaseParser diseaseParser, String cachePath) {
        super(diseaseParser);
        this.cachingPath = cachePath;
        this.n_terms_in_query = null;
    }

    public void cleanAndLoadScoreDistribution(int i){
        // treat any number above 10 as 10 because we only compute score distributions for <=10 terms
        i = Math.min(i, 10);
        //if desired score distribution already exist, return
        if (this.scoreDistributions.containsKey(i)) {
            return;
        }
        //!!!important!!!
        //empty current score distribution to reduce memory requirement.
        this.scoreDistributions.clear();

        String cachep = String.format("%s%s%d_term.scoreDistribution.binary",
                cachingPath , File.separator, i);
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(cachep))) {
            ScoreDistribution scoreDistribution = (ScoreDistribution) in.readObject();
            scoreDistributions.put(i, scoreDistribution);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error when trying to deserialize " + cachep);
        }
        logger.trace("Done deserializing {}", cachep);
    }

    @Override
    public void init() {
        String icMapPath = cachingPath + File.separator + "icMap.binary";
        String resnikSimilarityPath = cachingPath + File.separator + "resnikSimilarity.binary";
        String scoreDistributionsPath = cachingPath + File.separator + "scoreDistributions.binary";

        //deserialize ic map
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(icMapPath))) {
            logger.trace("deserialize information content map started");
            icMap = (Map<TermId, Double>) in.readObject();
            logger.trace("deserialize information content map success");
        } catch (FileNotFoundException e) {
            logger.trace("deserialize information content map failed");
            logger.error("file not found" + icMapPath);
            logger.trace("information content map initiation started");
            icMap = new InformationContentComputation(hpo).computeInformationContent(hpoTermIdToDiseaseIdsWithExpansion);
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
            logger.warn("file not found" + scoreDistributionsPath);
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
            if (n_terms_in_query!=null) {
                logger.trace("Running query with {} terms", n_terms_in_query);
                String cachep = String.format("%s%s%d_term.scoreDistribution.binary", cachingPath , File.separator,n_terms_in_query);
                try (ObjectInputStream in =
                             new ObjectInputStream(new FileInputStream(cachep))) {
                    ScoreDistribution scoreDistribution = (ScoreDistribution) in.readObject();
                    scoreDistributions.put(n_terms_in_query, scoreDistribution);
                } catch (Exception e) {
                    //We warn user but do not fail so that reader can call
                    // cleanAndLoadScoreDistribution method to control
                    // deserialization
                    //e.printStackTrace();
                    logger.warn("error when trying to deserialize " +
                            cachep);
                }
                logger.trace("Done deserializing {}", cachep);
            } else {
                try {
                    Files.list(Paths.get(cachingPath))
                            .filter(path -> path.getFileName().toString().matches("^[0-9]{1,2}_term.scoreDistribution.binary"))
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
}
