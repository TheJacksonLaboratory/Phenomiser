package org.jax.dichotomy;

import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class DichotomousPairTest {

    private static List<TermId> termIds;

    @BeforeClass
    public static void setup() {
        termIds = Stream.of("HP:01", "HP:02", "HP:03", "HP:04").map(TermId::of).collect(Collectors.toList());

    }
    @Test (expected = IllegalArgumentException.class)
    public void testConstructor() throws Exception {
        DichotomousPair pair1 = new DichotomousPair(termIds.get(0), termIds.get(0));
    }

    @Test
    public void equals1() throws Exception {

        DichotomousPair pair1 = new DichotomousPair(termIds.get(0), termIds.get(1));
        DichotomousPair pair2 = new DichotomousPair(termIds.get(0), termIds.get(1));
        assertEquals(pair1, pair2);

    }

    @Test
    public void equals2() throws Exception {

        DichotomousPair pair1 = new DichotomousPair(termIds.get(0), termIds.get(1));
        DichotomousPair pair2 = new DichotomousPair(termIds.get(1), termIds.get(0));
        assertEquals(pair1, pair2);

    }

    @Test
    public void equals3() throws Exception {

        DichotomousPair pair1 = new DichotomousPair(termIds.get(0), termIds.get(1));
        DichotomousPair pair2 = new DichotomousPair(termIds.get(0), termIds.get(2));
        assertNotEquals(pair1, pair2);

    }

}