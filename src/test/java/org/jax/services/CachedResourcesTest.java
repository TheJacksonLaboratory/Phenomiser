package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.jax.io.HpoParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;

import java.io.File;

import static org.junit.Assert.*;

@Ignore
public class CachedResourcesTest {

    private static AbstractResources resources; // = new CachedResources()

    @BeforeClass
    public static void setUp() throws Exception {
        final String path = System.getProperty("user.home") + File.separator + "Phenomiser_data";

        HpoParser hpoParser = new HpoParser(DiseaseParserTest.class.getClassLoader().getResource("hp.obo").getPath());
        hpoParser.init();
        DiseaseParser diseaseParser = new DiseaseParser(DiseaseParserTest.class.getClassLoader().getResource("phenotype.hpoa").getPath(), (HpoOntology) hpoParser.getHpo());
        diseaseParser.init();
        resources = new CachedResources(hpoParser, diseaseParser, path);
        resources.init();
    }

    @Test
    public void getScoreDistributions() throws Exception {
        assertNotNull(resources.getScoreDistributions());
        assertTrue(! resources.getScoreDistributions().isEmpty());
        assertTrue(resources.getScoreDistributions().size() > 1);

    }

}