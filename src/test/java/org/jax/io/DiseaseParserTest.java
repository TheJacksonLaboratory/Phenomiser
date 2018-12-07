package org.jax.io;

import org.junit.*;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;

import static org.junit.Assert.*;

public class DiseaseParserTest {

    private static HpoParser hpoParser;
    private static DiseaseParser diseaseParser;

    @BeforeClass
    public static void setUp() throws Exception {
        hpoParser = new HpoParser(DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath());
        hpoParser.init();
        diseaseParser = new DiseaseParser(DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath(), (HpoOntology) hpoParser.getHpo());
        diseaseParser.init();

    }

    @AfterClass
    public static void tearDown() throws Exception {
    }

    @Test
    public void getDiseaseMap() throws Exception {
        assertNotNull(diseaseParser.getDiseaseMap());
        assertTrue(! diseaseParser.getDiseaseMap().isEmpty());
        assertTrue(diseaseParser.getDiseaseMap().size() > 1000);
    }

    @Test
    public void getDiseaseIdToHpoTermIds() throws Exception {
        assertNotNull(diseaseParser.getDiseaseIdToHpoTermIds());
        assertTrue(! diseaseParser.getDiseaseIdToHpoTermIds().isEmpty());
        assertTrue(diseaseParser.getDiseaseIdToHpoTermIds().size() > 1000);
    }

    @Test
    public void getHpoTermIdToDiseaseIds() throws Exception {
        assertNotNull(diseaseParser.getHpoTermIdToDiseaseIds());
        assertTrue(! diseaseParser.getHpoTermIdToDiseaseIds().isEmpty());
        assertTrue(diseaseParser.getHpoTermIdToDiseaseIds().size() > 1000);
    }

    @Test
    public void getDiseaseIndexToDisease() throws Exception {
        assertNotNull(diseaseParser.getDiseaseIndexToDisease());
        assertTrue(! diseaseParser.getDiseaseIndexToDisease().isEmpty());
        assertTrue(diseaseParser.getDiseaseIndexToDisease().size() > 1000);
    }

    @Test
    public void getDiseaseIndexToHpoTerm() throws Exception {
        assertNotNull(diseaseParser.getDiseaseIndexToHpoTerms());
        assertTrue(! diseaseParser.getDiseaseIndexToHpoTerms().isEmpty());
        assertTrue(diseaseParser.getDiseaseIndexToHpoTerms().size() > 1000);
    }

}