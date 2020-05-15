package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Ignore
public class SimilarityScoreCalculatorTest {
    private static AbstractResources resources; // = new CachedResources()
    private static SimilarityScoreCalculator similarityScoreCalculator;
    @BeforeClass
    public static void setUp() throws Exception {

        final String path = System.getProperty("user.home") + File.separator + "Phenomiser_data";
        final String hpoPath = DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath();
        final String phenotypeAnnotation = DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath();

        Ontology hpo = OntologyLoader.loadOntology(new File(hpoPath));
        DiseaseParser diseaseParser = new DiseaseParser(phenotypeAnnotation, hpo);
        diseaseParser.init();
        resources = new CachedResources(hpo, diseaseParser, path);
        resources.init();
        similarityScoreCalculator = new SimilarityScoreCalculator(resources);
    }

    @Test
    public void computeB() {
        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIdsWithExpansion().keySet().stream().limit(3).collect(Collectors.toList());
        Map<TermId, Double> scores = similarityScoreCalculator.computeB(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
        System.out.println(scores.size());
    }

}