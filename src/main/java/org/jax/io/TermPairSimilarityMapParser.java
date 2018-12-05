package org.jax.io;

import org.jax.model.TermPair;
import org.jax.model.TermPairSimilarityMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TermPairSimilarityMapParser {

    private String path;

    private TermPairSimilarityMap termPairSimilarityMap;

    public TermPairSimilarityMapParser(String path) {

        this.path = path;

    }

    public TermPairSimilarityMap parse() throws IOException {
        TermPairSimilarityMap termPairSimilarityMap = new TermPairSimilarityMap();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        String[] elements;
        TermPair termPair;
        double score;
        while((line = reader.readLine()) != null) {
            elements = line.split(",");
            if (elements.length != 3) {
                continue;
            }
            termPair = new TermPair(elements[0], elements[1]);
            score = Double.valueOf(elements[2]);
            termPairSimilarityMap.add(termPair, score);
        }
        return termPairSimilarityMap;

    }
}
