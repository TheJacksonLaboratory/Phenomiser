package org.jax.io;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermIds;

import static org.mockito.Mockito.*;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class DiseaseParserTest {

    private DiseaseParser diseaseParser;

    @Mock
    HpoDiseaseAnnotationParser hpodiseaseAnnotationParser;

     Map<TermId, HpoDisease> diseaseMap = new HashMap<>();

    Map<TermId, HpoDisease> diseaseMapSpy = spy(diseaseMap);
    @Mock
     HpoDisease disease1;
    @Mock
     HpoDisease disease2;
    @Mock
    Ontology hpo;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(DiseaseParserTest.class);

        TermId id1 = TermId.of("OMIM:001");
        TermId id2 = TermId.of("OMIM:002");
        diseaseMap.put(id1, disease1);
        diseaseMap.put(id2, disease2);
        Set<TermId> hpoTerms4disease1 = new HashSet<>();
        Set<TermId> hpoTerms4disease2 = new HashSet<>();

        TermId ancesTerm1 = TermId.of("HP:100");
        TermId ancesTerm2 = TermId.of("HP:101");
        Set<TermId> inclAncestorTermIds1 = new HashSet<>(Arrays.asList(ancesTerm1, ancesTerm2));

        TermId ancesTerm3 = TermId.of("HP:102");
        TermId ancesTerm4 = TermId.of("HP:103");
        TermId ancesTerm5 = TermId.of("HP:104");
        Set<TermId> inclAncestorTermIds2 = new HashSet<>(Arrays.asList(ancesTerm3, ancesTerm4, ancesTerm5));

        when(hpodiseaseAnnotationParser.parse()).thenReturn(diseaseMap);
        when(TermIds.augmentWithAncestors(hpo, hpoTerms4disease1, true)).thenReturn(inclAncestorTermIds1);
        when(TermIds.augmentWithAncestors(hpo, hpoTerms4disease2, true)).thenReturn(inclAncestorTermIds2);
        diseaseParser = new DiseaseParser(hpodiseaseAnnotationParser, hpo);
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void getDiseaseMap() {
        assertNotNull(diseaseParser.getDiseaseMap());
        assertFalse(diseaseParser.getDiseaseMap().isEmpty());
        assertEquals(diseaseParser.getDiseaseMap().size(), 2);
    }

    @Test
    public void getDiseaseIdToHpoTermIds() {
        assertNotNull(diseaseParser.getDiseaseIdToHpoTermIdsWithExpansion());
        assertFalse(diseaseParser.getDiseaseIdToHpoTermIdsWithExpansion().isEmpty());
        assertEquals(diseaseParser.getDiseaseIdToHpoTermIdsWithExpansion().size(), 2);
    }

    @Test
    public void getHpoTermIdToDiseaseIds() {
        assertNotNull(diseaseParser.getHpoTermIdToDiseaseIdsWithExpansion());
        assertFalse(diseaseParser.getHpoTermIdToDiseaseIdsWithExpansion().isEmpty());
        assertEquals(diseaseParser.getHpoTermIdToDiseaseIdsWithExpansion().size(), 2);
    }

    @Test
    public void getDiseaseIndexToDisease() {
        assertNotNull(diseaseParser.getDiseaseIndexToDisease());
        assertFalse(diseaseParser.getDiseaseIndexToDisease().isEmpty());
        assertEquals(diseaseParser.getDiseaseIndexToDisease().size(), 2);
    }

    @Test
    public void getDiseaseIndexToHpoTerm() {
        assertNotNull(diseaseParser.getDiseaseIndexToHpoTermsWithExpansion());
        assertFalse(diseaseParser.getDiseaseIndexToHpoTermsWithExpansion().isEmpty());
        assertEquals(diseaseParser.getDiseaseIndexToHpoTermsWithExpansion().size(), 2);
    }

}