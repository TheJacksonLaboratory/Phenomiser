package org.jax.services;

import com.google.common.collect.Sets;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermIds;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.*;

public abstract class AbstractResources {

    private static Logger logger = LoggerFactory.getLogger(AbstractResources.class);

    protected HpoParser hpoParser;

    protected DiseaseParser diseaseParser;

    protected Ontology hpo;

    protected Map<TermId, HpoDisease> diseaseMap;

    protected Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds;

    protected Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds;

    protected Map<TermId, Double> icMap;

    protected PrecomputingPairwiseResnikSimilarity precomputingPairwiseResnikSimilarity;

    protected ResnikSimilarity resnikSimilarity;

    protected Map<Integer, List<TermId>> diseaseIndexToHpoTerms;

    protected Map<Integer, TermId> diseaseIndexToDisease;

    protected Map<Integer, ScoreDistribution> scoreDistributions;

    public AbstractResources(HpoParser hpoParser, DiseaseParser diseaseParser) {
        this.hpoParser = hpoParser;
        this.diseaseParser = diseaseParser;
    }

    public void defaultInit() {
        logger.trace("hpo initiation started");
        hpo = this.getHpoParser().getHpo();
        logger.trace("hpo initiation success");



        logger.trace("disease annotation initiation started");
        if (this.getDiseaseParser().getDiseaseMap() == null) {
            try {
                this.getDiseaseParser().init();
            } catch (PhenolException e) {
                e.printStackTrace();
                logger.trace("disease annotation initiation failed");
            }
        }

        logger.trace("disease annotation initiation success");

        logger.trace("disease map initiation started");
        diseaseMap = this.diseaseParser.getDiseaseMap();
        diseaseIdToHpoTermIds = this.diseaseParser.getDiseaseIdToHpoTermIds();
        hpoTermIdToDiseaseIds = this.diseaseParser.getHpoTermIdToDiseaseIds();
        diseaseIndexToDisease = this.diseaseParser.getDiseaseIndexToDisease();
        diseaseIndexToHpoTerms = this.diseaseParser.getDiseaseIndexToHpoTerms();
        logger.trace("disease map initiation success");

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

    public Ontology getHpo() {
        return hpo;
    }

    public void setHpo(Ontology hpo) {
        this.hpo = hpo;
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

    public Map<Integer, List<TermId>> getDiseaseIndexToHpoTerms() {
        return diseaseIndexToHpoTerms;
    }

    public void setDiseaseIndexToHpoTerms(Map<Integer, List<TermId>> diseaseIndexToHpoTerms) {
        this.diseaseIndexToHpoTerms = diseaseIndexToHpoTerms;
    }

    public Map<Integer, TermId> getDiseaseIndexToDisease() {
        return diseaseIndexToDisease;
    }

    public void setDiseaseIndexToDisease(Map<Integer, TermId> diseaseIndexToDisease) {
        this.diseaseIndexToDisease = diseaseIndexToDisease;
    }

    public Map<Integer, ScoreDistribution> getScoreDistributions() {
        return scoreDistributions;
    }

    public void setScoreDistributions(Map<Integer, ScoreDistribution> scoreDistributions) {
        this.scoreDistributions = scoreDistributions;
    }
}
