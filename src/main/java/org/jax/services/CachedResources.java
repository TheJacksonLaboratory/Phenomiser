package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;

public class CachedResources extends AbstractResources{

    private String cachingPath;

    public CachedResources(HpoParser hpoParser, DiseaseParser diseaseParser, String cachePath) {
        super(hpoParser, diseaseParser);
        this.cachingPath = cachePath;
    }

    @Override
    public void init() {
        //TODO: read in relevant resource from cache
    }
}
