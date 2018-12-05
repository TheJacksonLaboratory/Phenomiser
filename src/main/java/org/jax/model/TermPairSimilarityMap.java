package org.jax.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Use this class to represent a model to save the pairwise similarity scores.
 */
public class TermPairSimilarityMap {

    //To find the similarity score between two terms
    private Map<TermPair, Double> pairwiseMap;

    public TermPairSimilarityMap() {
        this.pairwiseMap = new HashMap<>();
    }

    public void add(TermPair pair, double simScore) {
        pairwiseMap.put(pair, simScore);
    }

    public void get(TermPair pair) {
        pairwiseMap.get(pair);
    }

    public boolean isExisted(TermPair pair) {
        return pairwiseMap.containsKey(pair);
    }

    public Map<TermPair, Double> getPairwiseMap() {
        return pairwiseMap;
    }

    public static void serialize(TermPairSimilarityMap termPairSimilarityMap, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        termPairSimilarityMap.getPairwiseMap().entrySet().stream().forEach(e -> {
            try {
                writer.write(e.getKey().toString());
                writer.write(",");
                writer.write(e.getValue().toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        writer.close();
    }



}
