package org.jax.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;

import static org.junit.Assert.*;

@Ignore
public class HpoParserTest {
    static HpoParser parser;
    @Before
    public void setUp() throws Exception {
        parser = new HpoParser(HpoParserTest.class.getClassLoader().getResource("hp.obo").getPath());
        parser.init();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParse() throws Exception {
        HpoOntology hpoOntology = (HpoOntology) parser.getHpo();
        assertNotNull(hpoOntology);
        assertNotNull(parser.termIdMap());
        assertTrue(parser.termIdMap().size() > 1000);
    }

}