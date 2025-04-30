package org.jax.prioritizer;

import org.jax.model.PhenomizerScore;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.similarity.HpoResnikSimilarity;
import org.phenopackets.schema.v2.Phenopacket;

import java.util.List;
import java.util.Map;

public interface Phenomiser {

    List<PhenomizerScore> query(Phenopacket phenopacket);

    static Phenomiser scoreBased(Ontology ontology, HpoResnikSimilarity resnikSimilarity, Map<TermId, HpoDisease> termIdHpoDiseaseMap) {
        return new ScoreBasedPhenomiser(ontology, resnikSimilarity, termIdHpoDiseaseMap);
    }

}
