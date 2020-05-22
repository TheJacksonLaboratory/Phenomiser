package org.jax.io;
import com.google.common.collect.ImmutableList;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.obo.hpo.HpoDiseaseAnnotationParser;

import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Import and process the disease/phenotype data
 * @author Aaron Zhang
 * @author Peter Robinson
 */
public class DiseaseParser {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseParser.class);
    private final Ontology hpo;
    /** key is disease termID, value is disease model */
    private final Map<TermId, HpoDisease> diseaseMap;
    /** key is disease id; value is a collection of hpo termIds (including direct and indirect annotations) */
    private final Map<TermId, Collection<TermId>> diseaseIdToHpoIdsPropagated;
    /** key is hpo termId; value is a collection of disease ids (includes propagated annotations). */
    private final Map<TermId, Collection<TermId>> hpoIdToDiseaseIdsPropagated;
    /** Key -- a disease id, e.g. OMIM:600123; value -- list of directly annotated HPOs */
    private final Map<TermId, Collection<TermId>> diseaseIdToDirectHpoTermIds;
    /** Key -- a disease id, e.g. OMIM:600123; value -- list of direct and indirect annotated HPOs */
    private final Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIdsDirect;
    /** We pass an integer index representing the disease. Here, we just count from 0...N-1 */
    private final Map<Integer, TermId> indexToDisease;

    private final Map<Integer, List<TermId>> diseaseIndexToHpoTermsWithExpansion;

    private final Map<Integer, List<TermId>> diseaseIndexToHpoTermsNoExpansion;


    /**
     * Set up the HPO ontology object and the disease resources
     * @param phenotypeHpoaPath Path to the phenotype.hpo file
     * @param hpOboPath Path to hp.obo
     * @param database Must be OMIM, ORPHA, or DECIPHER -- limit calculations to one of these databases
     */
    public DiseaseParser(String phenotypeHpoaPath, String hpOboPath, String database) {
        this.hpo = OntologyLoader.loadOntology(new File(hpOboPath));
        List<String> dbList = ImmutableList.of(database);
        this.diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(phenotypeHpoaPath, this.hpo, dbList);

        if(diseaseMap.values().stream().anyMatch(d -> d.getPhenotypicAbnormalities().isEmpty())) {
            logger.warn("Diseases with no annotations are found and to be removed...");
            Set<Map.Entry<TermId, HpoDisease>> noAnnotationDiseases = diseaseMap.entrySet().stream()
                    .filter(e -> e.getValue().getPhenotypicAbnormalities().isEmpty()).collect(Collectors.toSet());
            noAnnotationDiseases.forEach(e -> {
                diseaseMap.remove(e.getKey());
                logger.warn("Remove: " + e.getKey().getValue() + "\t" + e.getValue().getName());
            });
        }
        this.diseaseIdToDirectHpoTermIds = HpoDiseaseAnnotationParser.diseaseIdToDirectHpoTermIds(diseaseMap);
        this.hpoTermIdToDiseaseIdsDirect = HpoDiseaseAnnotationParser.hpoTermIdToDiseaseIdsDirect(diseaseMap, hpo);
        this.hpoIdToDiseaseIdsPropagated = HpoDiseaseAnnotationParser.hpoTermIdToDiseaseIdsPropagated(diseaseMap, hpo);
        this.diseaseIdToHpoIdsPropagated = HpoDiseaseAnnotationParser.diseaseIdToPropagatedHpoTermIds(diseaseMap, hpo);
        indexToDisease = new HashMap<>();
        diseaseIndexToHpoTermsWithExpansion = new HashMap<>();
        diseaseIndexToHpoTermsNoExpansion = new HashMap<>();
        int count = 0;
         for (Map.Entry<TermId, Collection<TermId>> entry : this.diseaseIdToHpoIdsPropagated.entrySet()) {
            diseaseIndexToHpoTermsWithExpansion.put(count, new ArrayList<>(entry.getValue()));
             indexToDisease.put(count, entry.getKey());
            count++;
        }
        count = 0;
        for (Map.Entry<TermId, Collection<TermId>> entry : this.diseaseIdToDirectHpoTermIds.entrySet()) {
            diseaseIndexToHpoTermsNoExpansion.put(count, new ArrayList<>(entry.getValue()));
            count++;
        }
    }

    public Ontology getHpo() {
        return hpo;
    }

    public Map<TermId, HpoDisease> getDiseaseMap() {
        return diseaseMap;
    }

    public Map<TermId, Collection<TermId>> getDiseaseIdToHpoIdsPropagated() {

        return diseaseIdToHpoIdsPropagated;
    }

    public Map<TermId, Collection<TermId>> getHpoIdToDiseaseIdsPropagated() {
        return hpoIdToDiseaseIdsPropagated;
    }

    public Map<Integer, List<TermId>> getDiseaseIndexToHpoTermsWithExpansion() {
        return diseaseIndexToHpoTermsWithExpansion;
    }

    public Map<Integer, TermId> getIndexToDisease() {
        return indexToDisease;
    }

    public Map<TermId, Collection<TermId>> getDiseaseIdToDirectHpoTermIds() {
        return diseaseIdToDirectHpoTermIds;
    }


    public Map<TermId, Collection<TermId>> getHpoTermIdToDiseaseIdsDirect() {
        return hpoTermIdToDiseaseIdsDirect;
    }

    public Map<Integer, List<TermId>> getDiseaseIndexToHpoTermsNoExpansion() {
        return diseaseIndexToHpoTermsNoExpansion;
    }
}
