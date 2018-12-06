package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;

import java.util.Properties;

public class ComputedResources extends AbstractResources {

    private Properties properties;

    public ComputedResources(HpoParser hpoParser, DiseaseParser diseaseParser, Properties properties) {
        super(hpoParser, diseaseParser);
        this.properties = properties;
    }

    @Override
    public void init() {
        //TODO: compute all resources from scratch. Use properties for computation.
    }
}
