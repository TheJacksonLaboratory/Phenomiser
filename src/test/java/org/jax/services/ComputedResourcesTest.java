package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.jax.io.HpoParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

import static org.junit.Assert.*;

@Ignore
public class ComputedResourcesTest {

    static AbstractResources resources;// = new ComputedResources()

    @BeforeClass
    public static void setUp() throws Exception {
        final String path = System.getProperty("user.home") + File.separator + "Phenomiser_data";
        final String hpoPath = DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath();
        final String phenotypeAnnotation = DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath();

        HpoParser hpoParser = new HpoParser(hpoPath);
        hpoParser.init();
        Ontology hpo = (Ontology) hpoParser.getHpo();
        HpoDiseaseAnnotationParser hpoDiseaseAnnotationParser = new HpoDiseaseAnnotationParser(phenotypeAnnotation, hpo);
        DiseaseParser diseaseParser = new DiseaseParser(hpoDiseaseAnnotationParser, hpo);
        diseaseParser.init();
        resources = new CachedResources(hpoParser, diseaseParser, path);
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