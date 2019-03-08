package org.jax.services;

import org.jax.model.SearchResult;
import org.junit.Test;
import org.monarchinitiative.phenol.ontology.data.TermId;

import static junit.framework.TestCase.assertTrue;

public class SearchResultTest {



     @Test
    public void test1() {
         TermId tid1 = TermId.of("test:1");
         TermId tid2 = TermId.of("test:2");

         SearchResult sr1 = new SearchResult(tid1,0.001,3.2);
         SearchResult sr2 = new SearchResult(tid1,0.0005,3.2);

         assertTrue(sr1.compareTo(sr2)<0);


     }


}
