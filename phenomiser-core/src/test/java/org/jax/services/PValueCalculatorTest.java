package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.jax.io.HpoParser;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;


import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

//@RunWith(MockitoJUnitRunner.class)
//public class PValueCalculatorTest {
//
//    //@Spy
//    private static AbstractResources resources = mock(ComputedResources.class, RETURNS_DEEP_STUBS); // = new CachedResources()
//    @Mock
//    private static SimilarityScoreCalculator similarityScoreCalculator;
//    private static IPValueCalculation pvalueCalculation;
//    private static Map<TermId, PValue> pvalues;
//
//    @BeforeClass
//    public static void setUp() throws Exception {
////        final String path = System.getProperty("user.home") + File.separator + "Phenomiser_data";
////        final String hpoPath = DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath();
////        final String phenotypeAnnotation = DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath();
////
////        HpoParser hpoParser = new HpoParser(hpoPath);
////        hpoParser.init();
////        HpoOntology hpo = (HpoOntology) hpoParser.getHpo();
////        HpoDiseaseAnnotationParser hpoDiseaseAnnotationParser = new HpoDiseaseAnnotationParser(phenotypeAnnotation, hpo);
////        DiseaseParser diseaseParser = new DiseaseParser(hpoDiseaseAnnotationParser, hpo);
////        diseaseParser.init();
////        resources = new CachedResources(hpoParser, diseaseParser, path);
////        resources.init();
////        similarityScoreCalculator = new SimilarityScoreCalculator(resources);
//
//
//
//        HpoParser hpoParser = mock(HpoParser.class);
//        hpoParser.init();
//        when(hpoParser.getHpo()).thenReturn(mock(HpoOntology.class));
//
//        HpoOntology hpoOntology = (HpoOntology) hpoParser.getHpo();
//
//        HpoDiseaseAnnotationParser hpoDiseaseAnnotationParser = mock(HpoDiseaseAnnotationParser.class);
//        DiseaseParser diseaseParser = spy(new DiseaseParser(hpoDiseaseAnnotationParser, hpoOntology));
//
//        resources.init();
//
//        MockitoAnnotations.initMocks(PValueCalculatorTest.class);
//
//
////        Map<Integer, ScoreDistribution> scoreDistributions = new HashMap<>();
////        ScoreDistribution scoreDistribution3 = mock(ScoreDistribution.class);
////        scoreDistributions.put(3, scoreDistribution3);
////        ScoreDistribution scoreDistribution4 = mock(ScoreDistribution.class);
////        scoreDistributions.put(4, scoreDistribution4);
////
////        when(resources.getScoreDistributions()).thenReturn(scoreDistributions);
//
//    }
//
//    @Test
//    public void calculatePValues() throws Exception {
////        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIdsWithExpansion().keySet().stream().limit(3).collect(Collectors.toList());
////        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
////        pvalueCalculation = new PValueCalculator(3, scores, resources);
////        pvalues = pvalueCalculation.calculatePValues();
////        assertTrue(pvalues.size() > 1);
////        pvalues.entrySet().stream().forEach(e -> {
////            System.out.println(e.getKey().getValue() + "\t" + e.getValue().p);
////        });
//
//        Map<Integer, Double> similarityScores = ImmutableMap.<Integer, Double>builder()
//                .put(1, 1.1)
//                .put(2, 5.6)
//                .put(3, 7.0).build();
//
//        pvalueCalculation = new PValueCalculator(3, similarityScores, resources);
//
//        when(resources.getScoreDistributions().containsKey(3)).thenReturn(true);
//        when(resources.getScoreDistributions().get(3)).thenReturn(notNull());
//        ScoreDistribution scoreDistribution4threeTerms = mock(ScoreDistribution.class);
//        when(resources.getScoreDistributions().get(3)).thenReturn(scoreDistribution4threeTerms);
//        when(scoreDistribution4threeTerms.getObjectScoreDistribution(1).estimatePValue(1.1)).thenReturn(0.01);
//        when(scoreDistribution4threeTerms.getObjectScoreDistribution(2).estimatePValue(2)).thenReturn(0.0001);
//        when(scoreDistribution4threeTerms.getObjectScoreDistribution(3).estimatePValue(7.0)).thenReturn(0.0000001);
//        TermId disease1 = TermId.of("DISEASE:01");
//        TermId disease2 = TermId.of("DISEASE:02");
//        TermId disease3 = TermId.of("DISEASE:03");
//        when(resources.getDiseaseIndexToDisease().get(1)).thenReturn(disease1);
//        when(resources.getDiseaseIndexToDisease().get(2)).thenReturn(disease2);
//        when(resources.getDiseaseIndexToDisease().get(3)).thenReturn(disease3);
//
//
//        verify(resources).init();
//
//        Map<TermId, PValue> pvalues = pvalueCalculation.calculatePValues();
//        assertEquals(3, pvalues.size());
//
//        assertEquals(0.0001, pvalues.get(disease2).p, 0.0000001);
//
//
//
//    }
//
//    @Test
//    @Ignore
//    public void adjustPValues() {
//        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIdsWithExpansion().keySet().stream().limit(3).collect(Collectors.toList());
//        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
//        pvalueCalculation = new PValueCalculator(3, scores, resources);
//        BenjaminiHochberg benjaminiHochberg = new BenjaminiHochberg();
//        pvalues = benjaminiHochberg.adjustPValues(pvalueCalculation);
//        assertTrue(pvalues.size() > 1);
//        pvalues.entrySet().stream().forEach(e -> {
//            System.out.println(e.getKey().getValue() + "\t" + e.getValue().p + "\t" + e.getValue().p_adjusted);
//        });
//    }
//
//}

//The following code shows a test with concrete instantiation
@Ignore
public class PValueCalculatorTest {

//    private static AbstractResources resources; // = new CachedResources()
//    private static SimilarityScoreCalculator similarityScoreCalculator;
//    private static IPValueCalculation pvalueCalculation;
//    private static Map<TermId, PValue> pvalues;
//
//    @BeforeClass
//    public static void setUp() throws Exception {
//        final String path = System.getProperty("user.home") + File.separator + "Phenomiser_data";
//        final String hpoPath = DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath();
//        final String phenotypeAnnotation = DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath();
//
//        HpoParser hpoParser = new HpoParser(hpoPath);
//        hpoParser.init();
//        Ontology hpo = (Ontology) hpoParser.getHpo();
//        HpoDiseaseAnnotationParser hpoDiseaseAnnotationParser = new HpoDiseaseAnnotationParser(phenotypeAnnotation, hpo);
//        DiseaseParser diseaseParser = new DiseaseParser(hpoDiseaseAnnotationParser, hpo);
//        diseaseParser.init();
//        resources = new CachedResources(hpoParser, diseaseParser, path);
//        resources.init();
//        similarityScoreCalculator = new SimilarityScoreCalculator(resources);
//
//    }
//
//    @Test
//    public void calculatePValues() throws Exception {
//        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIdsWithExpansion().keySet().stream().limit(3).collect(Collectors.toList());
//        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
//        pvalueCalculation = new PValueCalculator(3, scores, resources);
//        pvalues = pvalueCalculation.calculatePValues();
//        assertTrue(pvalues.size() > 1);
//        pvalues.entrySet().stream().forEach(e -> {
//            System.out.println(e.getKey().getValue() + "\t" + e.getValue().getRawPValue());
//        });
//    }
//
//    @Test
//    public void adjustPValues() {
//        List<TermId> randomTermList = resources.getHpoTermIdToDiseaseIdsWithExpansion().keySet().stream().limit(3).collect(Collectors.toList());
//        Map<Integer, Double> scores = similarityScoreCalculator.compute(randomTermList, Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA));
//        pvalueCalculation = new PValueCalculator(3, scores, resources);
//        BenjaminiHochberg benjaminiHochberg = new BenjaminiHochberg();
//        pvalues = benjaminiHochberg.adjustPValues(pvalueCalculation);
//        assertTrue(pvalues.size() > 1);
//        pvalues.entrySet().stream().forEach(e -> {
//            System.out.println(e.getKey().getValue() + "\t" + e.getValue().getRawPValue() + "\t" + e.getValue().getAdjustedPValue());
//        });
//    }

}