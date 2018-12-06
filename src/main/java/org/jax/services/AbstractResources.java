package org.jax.services;

import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractResources {

    private HpoParser hpoParser;

    private DiseaseParser diseaseParser;

    private static HpoOntology hpo;

    private Map<TermId, HpoDisease> diseaseMap;

    private Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds;

    private Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds;

    private Map<TermId, Double> icMap;

    private PrecomputingPairwiseResnikSimilarity precomputingPairwiseResnikSimilarity;

    private ResnikSimilarity resnikSimilarity;

    private Map<Integer, List<TermId>> diseaseIdHashToHpoTerms;

    private Map<Integer, TermId> diseaseIdHashToDisease;

    private Map<Integer, ScoreDistribution> scoreDistributions;

    public AbstractResources(HpoParser hpoParser, DiseaseParser diseaseParser) {
        this.hpoParser = hpoParser;
        this.diseaseParser = diseaseParser;
    }

    public abstract void init();

    public HpoParser getHpoParser() {
        return hpoParser;
    }

    public void setHpoParser(HpoParser hpoParser) {
        this.hpoParser = hpoParser;
    }

    public DiseaseParser getDiseaseParser() {
        return diseaseParser;
    }

    public void setDiseaseParser(DiseaseParser diseaseParser) {
        this.diseaseParser = diseaseParser;
    }

    public static HpoOntology getHpo() {
        return hpo;
    }

    public static void setHpo(HpoOntology hpo) {
        AbstractResources.hpo = hpo;
    }

    public Map<TermId, HpoDisease> getDiseaseMap() {
        return diseaseMap;
    }

    public void setDiseaseMap(Map<TermId, HpoDisease> diseaseMap) {
        this.diseaseMap = diseaseMap;
    }

    public Map<TermId, Collection<TermId>> getDiseaseIdToHpoTermIds() {
        return diseaseIdToHpoTermIds;
    }

    public void setDiseaseIdToHpoTermIds(Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds) {
        this.diseaseIdToHpoTermIds = diseaseIdToHpoTermIds;
    }

    public Map<TermId, Collection<TermId>> getHpoTermIdToDiseaseIds() {
        return hpoTermIdToDiseaseIds;
    }

    public void setHpoTermIdToDiseaseIds(Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds) {
        this.hpoTermIdToDiseaseIds = hpoTermIdToDiseaseIds;
    }

    public Map<TermId, Double> getIcMap() {
        return icMap;
    }

    public void setIcMap(Map<TermId, Double> icMap) {
        this.icMap = icMap;
    }

    public PrecomputingPairwiseResnikSimilarity getPrecomputingPairwiseResnikSimilarity() {
        return precomputingPairwiseResnikSimilarity;
    }

    public void setPrecomputingPairwiseResnikSimilarity(PrecomputingPairwiseResnikSimilarity precomputingPairwiseResnikSimilarity) {
        this.precomputingPairwiseResnikSimilarity = precomputingPairwiseResnikSimilarity;
    }

    public ResnikSimilarity getResnikSimilarity() {
        return resnikSimilarity;
    }

    public void setResnikSimilarity(ResnikSimilarity resnikSimilarity) {
        this.resnikSimilarity = resnikSimilarity;
    }

    public Map<Integer, List<TermId>> getDiseaseIdHashToHpoTerms() {
        return diseaseIdHashToHpoTerms;
    }

    public void setDiseaseIdHashToHpoTerms(Map<Integer, List<TermId>> diseaseIdHashToHpoTerms) {
        this.diseaseIdHashToHpoTerms = diseaseIdHashToHpoTerms;
    }

    public Map<Integer, TermId> getDiseaseIdHashToDisease() {
        return diseaseIdHashToDisease;
    }

    public void setDiseaseIdHashToDisease(Map<Integer, TermId> diseaseIdHashToDisease) {
        this.diseaseIdHashToDisease = diseaseIdHashToDisease;
    }

    public Map<Integer, ScoreDistribution> getScoreDistributions() {
        return scoreDistributions;
    }

    public void setScoreDistributions(Map<Integer, ScoreDistribution> scoreDistributions) {
        this.scoreDistributions = scoreDistributions;
    }
}
