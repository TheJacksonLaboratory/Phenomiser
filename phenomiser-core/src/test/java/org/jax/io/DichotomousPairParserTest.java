package org.jax.io;

import org.jax.dichotomy.DichotomousPair;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

@Ignore
public class DichotomousPairParserTest {

    private static DichotomousPairParser parser;

    @BeforeClass
    public static void setUp() throws Exception {
        parser = new DichotomousPairParser(DichotomousPairParserTest.class.getClassLoader().getResource("dichotomousPair.csv").getPath());
    }

    @Test
    public void getDichotomousPairSet() throws Exception {
        Set<DichotomousPair> pairs = parser.getDichotomousPairSet();
        assertFalse(pairs.isEmpty());
    }

}