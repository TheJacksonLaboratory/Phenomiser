package org.jax.model;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class Item2PValueAndSimilarityTest {
    private List<Item2PValueAndSimilarity<TermId>> items;

    @Before
    public void setUp() throws Exception {

        items = new ArrayList<>();
        Item2PValueAndSimilarity<TermId> item1 = new Item2PValueAndSimilarity<TermId>(TermId.of("OMIM:1"), 0.0, 3.05);
        Item2PValueAndSimilarity<TermId> item2 = new Item2PValueAndSimilarity<TermId>(TermId.of("OMIM:2"), 0.0, 4.05);
        Item2PValueAndSimilarity<TermId> item3 = new Item2PValueAndSimilarity<TermId>(TermId.of("OMIM:3"), 0.1, 4.05);
        Item2PValueAndSimilarity<TermId> item4 = new Item2PValueAndSimilarity<TermId>(TermId.of("OMIM:4"), 0.15, 6.05);

        items.addAll(Arrays.asList(item1, item2, item3, item4));
    }

    @Test
    public void testSort() throws Exception{
        assertNotNull(items);
        assertFalse(items.isEmpty());
        Collections.shuffle(items);
        Collections.sort(items);
        items.forEach(i -> System.out.println(i.getItem().getValue()));
    }

}