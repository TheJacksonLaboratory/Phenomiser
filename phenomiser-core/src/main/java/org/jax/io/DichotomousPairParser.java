package org.jax.io;

import com.google.common.collect.ImmutableSet;
import org.jax.dichotomy.DichotomousPair;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.Set;

public class DichotomousPairParser {

    final static Logger logger = LoggerFactory.getLogger(DichotomousPair.class);

    private Set<DichotomousPair> dichotomousPairSet;
    private InputStream inputStream;

    public DichotomousPairParser(String path) throws FileNotFoundException {
        this.inputStream = new FileInputStream(path);
        init();
    }

    public DichotomousPairParser(InputStream inputStream) {
        this.inputStream = inputStream;
        init();
    }

    private void init() {
        ImmutableSet.Builder<DichotomousPair> builder = new ImmutableSet.Builder<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine(); //skip header
            while ((line = reader.readLine()) != null) {
                String[] element = line.split(",");
                if (element.length != 3) {
                    logger.warn("line does not have three fields: " + line);
                }
                TermId yin = TermId.of(element[0]);
                TermId yang = TermId.of(element[1]);
                if (!yin.equals(yang)){
                    builder.add(new DichotomousPair(yin, yang));
                } else {
                    logger.warn("line has identical termIds for low and high: " + line);
                }

            }
            dichotomousPairSet = builder.build();
        } catch (IOException e) {
            logger.warn("IO exception happed when paring dichotomous pair file");
        }
    }

    public Set<DichotomousPair> getDichotomousPairSet() {
        return dichotomousPairSet;
    }
}
