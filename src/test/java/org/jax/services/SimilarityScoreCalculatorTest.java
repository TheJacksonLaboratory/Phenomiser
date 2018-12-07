package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.jax.io.HpoParser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimilarityScoreCalculatorTest {
    private static AbstractResources resources; // = new CachedResources()
    private static SimilarityScoreCalculator similarityScoreCalculator;
    @BeforeClass
    public static void setUp() throws Exception {

        final String path = System.getProperty("user.home") + File.separator + "Phenomiser_data";

        HpoParser hpoParser = new HpoParser(DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath());
        hpoParser.init();
        DiseaseParser diseaseParser = new DiseaseParser(DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath(), (HpoOntology) hpoParser.getHpo());
        diseaseParser.init();
        resources = new CachedResources(hpoParser, diseaseParser, path);
        resources.init();
        similarityScoreCalculator = new SimilarityScoreCalculator(resources);
    }

    @Test
    public void compute() throws Exception {
        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIds().keySet().stream().limit(3).collect(Collectors.toList());
        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
        System.out.println(scores.size());
        //scores.entrySet().stream().forEach(e -> System.out.println(e.getKey() + "\t" + e.getValue()));

    }

}