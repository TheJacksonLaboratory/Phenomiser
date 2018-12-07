package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.jax.io.HpoParser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.IPValueCalculation;
import org.monarchinitiative.phenol.stats.PValue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PValueCalculatorTest {

    private static AbstractResources resources; // = new CachedResources()
    private static SimilarityScoreCalculator similarityScoreCalculator;
    private static IPValueCalculation pvalueCalculation;
    private static Map<TermId, PValue> pvalues;

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
    public void calculatePValues() throws Exception {
        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIds().keySet().stream().limit(3).collect(Collectors.toList());
        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
        pvalueCalculation = new PValueCalculator(3, scores, resources);
        pvalues = pvalueCalculation.calculatePValues();
        assertTrue(pvalues.size() > 1);
        pvalues.entrySet().stream().forEach(e -> {
            System.out.println(e.getKey().getValue() + "\t" + e.getValue().p);
        });
    }

    @Test
    public void adjustPValues() {
        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIds().keySet().stream().limit(3).collect(Collectors.toList());
        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
        pvalueCalculation = new PValueCalculator(3, scores, resources);
        BenjaminiHochberg benjaminiHochberg = new BenjaminiHochberg();
        pvalues = benjaminiHochberg.adjustPValues(pvalueCalculation);
        assertTrue(pvalues.size() > 1);
        pvalues.entrySet().stream().forEach(e -> {
            System.out.println(e.getKey().getValue() + "\t" + e.getValue().p + "\t" + e.getValue().p_adjusted);
        });
    }

}