package org.jax.services;

import com.google.common.collect.Sets;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermIds;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.ResnikSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractResources {

    private static Logger logger = LoggerFactory.getLogger(AbstractResources.class);

    protected HpoParser hpoParser;

    protected DiseaseParser diseaseParser;

    protected HpoOntology hpo;

    protected Map<TermId, HpoDisease> diseaseMap;

    protected Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds;

    protected Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds;

    protected Map<TermId, Double> icMap;

    protected PrecomputingPairwiseResnikSimilarity precomputingPairwiseResnikSimilarity;

    protected ResnikSimilarity resnikSimilarity;

    protected Map<Integer, List<TermId>> diseaseIdHashToHpoTerms;

    protected Map<Integer, TermId> diseaseIdHashToDisease;

    protected Map<Integer, ScoreDistribution> scoreDistributions;

    public AbstractResources(HpoParser hpoParser, DiseaseParser diseaseParser) {
        this.hpoParser = hpoParser;
        this.diseaseParser = diseaseParser;
    }

    public void defaultInit() {
        try {
            logger.trace("hpo initiation started");
            hpo = (HpoOntology) this.getHpoParser().parse();
            logger.trace("hpo initiation success");
        } catch (FileNotFoundException e) {
            logger.error("hpo initiation failed");
            return;
        } catch (PhenolException e) {
            logger.error("hpo initiation failed");
            return;
        }

        try {
            logger.trace("disease annotation initiation started");
            this.getDiseaseParser().parse();
            logger.trace("disease annotation initiation success");
        } catch (PhenolException e) {
            logger.trace("disease annotation initiation failed");
            return;
        }
        diseaseMap = this.getDiseaseParser().getDiseaseMap();

        //init disease maps
        logger.trace("disease map initiation started");
        for (TermId diseaseId : diseaseMap.keySet()) {
            HpoDisease disease = diseaseMap.get(diseaseId);
            List<TermId> hpoTerms = disease.getPhenotypicAbnormalityTermIdList();
            diseaseIdToHpoTermIds.putIfAbsent(diseaseId, new HashSet<>());

            // add term anscestors
            final Set<TermId> inclAncestorTermIds = TermIds.augmentWithAncestors(hpo, Sets.newHashSet(hpoTerms), true);

            for (TermId tid : inclAncestorTermIds) {
                hpoTermIdToDiseaseIds.putIfAbsent(tid, new HashSet<>());
                hpoTermIdToDiseaseIds.get(tid).add(diseaseId);
                diseaseIdToHpoTermIds.get(diseaseId).add(tid);
            }
        }

        diseaseIdHashToHpoTerms = diseaseIdToHpoTermIds.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().hashCode(), e -> new ArrayList<TermId>(e.getValue())));

        diseaseIdHashToDisease = diseaseIdToHpoTermIds.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().hashCode(), e -> e.getKey()));
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

    public HpoOntology getHpo() {
        return hpo;
    }

    public void setHpo(HpoOntology hpo) {
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
