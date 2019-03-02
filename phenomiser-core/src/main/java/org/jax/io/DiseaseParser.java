package org.jax.io;
import com.google.common.collect.Sets;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DiseaseParser {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseParser.class);
    private Ontology hpo;

    private HpoDiseaseAnnotationParser diseaseAnnotationParser;

    private Map<TermId, HpoDisease> diseaseMap; //key is disease termID, value is disease model (has phenotype list)

    private Map<TermId, Collection<TermId>> diseaseIdToHpoTermIdsWithExpansion; //key is disease id; value is a collection of hpo termIds

    private Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIdsWithExpansion; //key is hpo termId; value is a collection of disease ids

    private Map<Integer, List<TermId>> diseaseIndexToHpoTermsWithExpansion;

    private Map<TermId, Collection<TermId>> diseaseIdToHpoTermIdsNoExpansion;

    private Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIdsNoExpansion;

    private Map<Integer, List<TermId>> diseaseIndexToHpoTermsNoExpansion;


    private Map<Integer, TermId> diseaseIndexToDisease;


//    public DiseaseParser(String diseaseAnnotation, HpoOntology hpoOntology) {
//        this.hpo = hpoOntology;
//        this.diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseaseAnnotation, hpoOntology);
//    }

    public DiseaseParser(HpoDiseaseAnnotationParser diseaseAnnotationParser, Ontology hpoOntology){
        this.diseaseAnnotationParser = diseaseAnnotationParser;
        this.hpo = hpoOntology;
    }

    public void init() throws PhenolException {
        diseaseMap = diseaseAnnotationParser.parse();
        //remove diseases with no annotation as they mess up downstream analysis
        if(diseaseMap.values().stream().anyMatch(d -> d.getPhenotypicAbnormalities().isEmpty())) {
            logger.warn("Diseases with no annotations are found and to be removed...");
            Set<Map.Entry<TermId, HpoDisease>> noAnnotationDiseases = diseaseMap.entrySet().stream()
                    .filter(e -> e.getValue().getPhenotypicAbnormalities().isEmpty()).collect(Collectors.toSet());
            noAnnotationDiseases.forEach(e -> {
                diseaseMap.remove(e.getKey());
                logger.warn("Remove: " + e.getKey().getValue() + "\t" + e.getValue().getName());
            });
        }

        diseaseIdToHpoTermIdsWithExpansion = new HashMap<>();
        hpoTermIdToDiseaseIdsWithExpansion = new HashMap<>();
        diseaseIndexToDisease = new HashMap<>();
        diseaseIndexToHpoTermsWithExpansion = new HashMap<>();

        diseaseIdToHpoTermIdsNoExpansion = new HashMap<>();
        hpoTermIdToDiseaseIdsNoExpansion = new HashMap<>();
        diseaseIndexToHpoTermsNoExpansion = new HashMap<>();

        for (TermId diseaseId : diseaseMap.keySet()) {
            HpoDisease disease = diseaseMap.get(diseaseId);
            List<TermId> hpoTerms = disease.getPhenotypicAbnormalityTermIdList();
            diseaseIdToHpoTermIdsWithExpansion.putIfAbsent(diseaseId, new HashSet<>());
            diseaseIdToHpoTermIdsNoExpansion.putIfAbsent(diseaseId, hpoTerms);

            // prepare no term ancestor maps
            for (TermId hpoTerm : hpoTerms) {
                hpoTermIdToDiseaseIdsNoExpansion.putIfAbsent(hpoTerm, new
                        HashSet<>());
                hpoTermIdToDiseaseIdsNoExpansion.get(hpoTerm).add(diseaseId);
            }


            // add term anscestors
            final Set<TermId> inclAncestorTermIds = TermIds.augmentWithAncestors(hpo, Sets.newHashSet(hpoTerms), true);

            for (TermId tid : inclAncestorTermIds) {
                hpoTermIdToDiseaseIdsWithExpansion.putIfAbsent(tid, new HashSet<>());
                hpoTermIdToDiseaseIdsWithExpansion.get(tid).add(diseaseId);
                diseaseIdToHpoTermIdsWithExpansion.get(diseaseId).add(tid);
            }
        }

//        int count = 0;
//        for (Map.Entry<TermId, Collection<TermId>> entry : diseaseIdToHpoTermIdsWithExpansion.entrySet()) {
//            diseaseIndexToHpoTermsWithExpansion.put(count, new ArrayList<TermId>(entry.getValue()));
//            diseaseIndexToDisease.put(count, entry.getKey());
//            count++;
//        }
        diseaseIndexToHpoTermsWithExpansion = diseaseIdToHpoTermIdsWithExpansion.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().hashCode(), e -> new ArrayList<>(e.getValue())));

        diseaseIndexToDisease = diseaseIdToHpoTermIdsWithExpansion.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().hashCode(), e -> e.getKey()));

        diseaseIndexToHpoTermsNoExpansion = diseaseIdToHpoTermIdsNoExpansion
                .entrySet().stream().collect(Collectors.toMap(e -> e.getKey()
                        .hashCode(), e -> new ArrayList<>(e.getValue())));
    }


    public Map<TermId, HpoDisease> getDiseaseMap() {
        return diseaseMap;
    }

    public Map<TermId, Collection<TermId>> getDiseaseIdToHpoTermIdsWithExpansion() {

        return diseaseIdToHpoTermIdsWithExpansion;
    }

    public Map<TermId, Collection<TermId>> getHpoTermIdToDiseaseIdsWithExpansion() {
        return hpoTermIdToDiseaseIdsWithExpansion;
    }

    public Map<Integer, List<TermId>> getDiseaseIndexToHpoTermsWithExpansion() {
        return diseaseIndexToHpoTermsWithExpansion;
    }

    public Map<Integer, TermId> getDiseaseIndexToDisease() {
        return diseaseIndexToDisease;
    }

    public Map<TermId, Collection<TermId>> getDiseaseIdToHpoTermIdsNoExpansion() {
        return diseaseIdToHpoTermIdsNoExpansion;
    }


    public Map<TermId, Collection<TermId>> getHpoTermIdToDiseaseIdsNoExpansion() {
        return hpoTermIdToDiseaseIdsNoExpansion;
    }

    public Map<Integer, List<TermId>> getDiseaseIndexToHpoTermsNoExpansion() {
        return diseaseIndexToHpoTermsNoExpansion;
    }
}
