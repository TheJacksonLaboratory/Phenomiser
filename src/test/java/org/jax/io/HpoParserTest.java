package org.jax.io;

import org.junit.*;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.monarchinitiative.phenol.ontology.data.Ontology;


import static org.junit.Assert.*;

@Ignore
/**
 * There is no need to test this class. This is just an example to test out the mockito library.
 */
public class HpoParserTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    static HpoParser parser;

    @Mock
    Ontology hpoOntology;

    @Before
    public void setUp() throws Exception {
        when(parser.getHpo()).thenReturn(hpoOntology);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(parser.getHpo(), hpoOntology);
    }

}