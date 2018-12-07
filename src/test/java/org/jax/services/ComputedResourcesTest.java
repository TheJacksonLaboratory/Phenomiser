package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.jax.io.HpoParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;

import static org.junit.Assert.*;

public class ComputedResourcesTest {

    static AbstractResources resources;// = new ComputedResources()

    @BeforeClass
    public static void setUp() throws Exception {
        HpoParser hpoParser = new HpoParser(DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath());
        hpoParser.init();
        DiseaseParser diseaseParser = new DiseaseParser(DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath(), (HpoOntology) hpoParser.getHpo());
        diseaseParser.init();
        resources = new ComputedResources(hpoParser, diseaseParser, null, true);
        resources.init();
    }

    @Test
    public void getHpo() {
        assertNotNull(resources.getHpo());
    }


    @Test
    public void getICMap() {
        assertNotNull(resources.getIcMap());
        assertTrue(!resources.getIcMap().isEmpty());
        assertTrue(resources.getIcMap().size() > 100);
    }

    @Test
    public void getResnikSimilarity() {
        assertNotNull(resources.getResnikSimilarity());
    }

    @Test
    public void getScoreDistributions() {
        assertNotNull(resources.getScoreDistributions());
        assertTrue(! resources.getScoreDistributions().isEmpty());
        assertTrue(resources.getScoreDistributions().size() > 1);
    }


}