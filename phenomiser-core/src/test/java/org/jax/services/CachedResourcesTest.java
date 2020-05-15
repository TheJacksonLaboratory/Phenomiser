package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.DiseaseParserTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

import static org.junit.Assert.*;

@Ignore
public class CachedResourcesTest {

    private static AbstractResources resources; // = new CachedResources()

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
    }

    @Test
    public void getScoreDistributions() throws Exception {
        assertNotNull(resources.getScoreDistributions());
        assertFalse(resources.getScoreDistributions().isEmpty());
        assertTrue(resources.getScoreDistributions().size() > 1);
    }

}