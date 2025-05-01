package org.jax.prioritizer;

import org.jax.model.PhenomizerScore;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.similarity.HpoResnikSimilarity;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ScoreBasedPhenomiser implements Phenomiser {
    private final Ontology ontology;
    private final Map<TermId, HpoDisease> termIdHpoDiseaseMap;
    private final HpoResnikSimilarity resnikSimilarity;

    public ScoreBasedPhenomiser(Ontology ontology,
                                HpoResnikSimilarity resnik,
                                Map<TermId, HpoDisease> termIdHpoDiseaseMap) {
        this.ontology = ontology;
        this.resnikSimilarity = resnik;
        this.termIdHpoDiseaseMap = termIdHpoDiseaseMap;
    }

    /**
     * Extract the observed terms from the GA4GH phenopacket, ignoring the excluded terms,
     * which are not relevant for the Phenomiser algorithm
     *  @param disease The HPO Disease model currently being analyzed
     *  @return List of observed HPO Term identifiers
     */
    private Collection<TermId> getObservedDiseaseTerms(HpoDisease disease) {
        return StreamSupport.stream(disease.presentAnnotations().spliterator(), false)
                .map(HpoDiseaseAnnotation::id)
                .collect(Collectors.toList());
    }

    /**
     * Perform Phenomizer analysis on all diseases and sort according to score.
     *
     * @param phenopacket
     * @return
     */
    @Override
    public List<PhenomizerScore> query(Phenopacket phenopacket) {
        // get observed HPO terms
        Collection<TermId> observedPhenotypicFeatures = phenopacket.getPhenotypicFeaturesList().stream()
                .filter(Predicate.not(PhenotypicFeature::getExcluded))
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .map(TermId::of)
                .toList();
        List<PhenomizerScore> similarityScores = new ArrayList<>();
        for (var entry : this.termIdHpoDiseaseMap.entrySet()) {
            TermId termId = entry.getKey();
            HpoDisease disease = entry.getValue();
            Collection<TermId> termsObservedInDisease = getObservedDiseaseTerms(disease);
            double score = resnikSimilarity.computeScoreSymmetric(observedPhenotypicFeatures, termsObservedInDisease);
            PhenomizerScore pscore = new PhenomizerScore(termId, disease.diseaseName(), score);
            similarityScores.add(pscore);
            System.out.println("Adding score: " + pscore);
        }
        similarityScores.sort(Comparator.reverseOrder());
        return similarityScores;
    }
}
