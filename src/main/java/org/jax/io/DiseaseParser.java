package org.jax.io;
import com.google.common.collect.Sets;
import org.jax.Phenomiser;
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
import java.util.stream.Stream;

/**
 *
 */
public class DiseaseParser {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseParser.class);
    private Ontology hpo;

    private HpoDiseaseAnnotationParser diseaseAnnotationParser;

    private Map<TermId, HpoDisease> diseaseMap; //key is disease termID, value is disease model (has phenotype list)

    private Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds; //key is disease id; value is a collection of hpo termIds

    private Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds; //key is hpo termId; value is a collection of disease ids

    private Map<Integer, List<TermId>> diseaseIndexToHpoTerms;

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

        diseaseIdToHpoTermIds = new HashMap<>();
        hpoTermIdToDiseaseIds = new HashMap<>();
        diseaseIndexToDisease = new HashMap<>();
        diseaseIndexToHpoTerms = new HashMap<>();

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

//        int count = 0;
//        for (Map.Entry<TermId, Collection<TermId>> entry : diseaseIdToHpoTermIds.entrySet()) {
//            diseaseIndexToHpoTerms.put(count, new ArrayList<TermId>(entry.getValue()));
//            diseaseIndexToDisease.put(count, entry.getKey());
//            count++;
//        }
        diseaseIndexToHpoTerms = diseaseIdToHpoTermIds.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().hashCode(), e -> new ArrayList<>(e.getValue())));

        diseaseIndexToDisease = diseaseIdToHpoTermIds.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().hashCode(), e -> e.getKey()));
    }


    public Map<TermId, HpoDisease> getDiseaseMap() {
        return diseaseMap;
    }

    public Map<TermId, Collection<TermId>> getDiseaseIdToHpoTermIds() {

        return diseaseIdToHpoTermIds;
    }

    public Map<TermId, Collection<TermId>> getHpoTermIdToDiseaseIds() {
        return hpoTermIdToDiseaseIds;
    }

    public Map<Integer, List<TermId>> getDiseaseIndexToHpoTerms() {
        return diseaseIndexToHpoTerms;
    }

    public Map<Integer, TermId> getDiseaseIndexToDisease() {
        return diseaseIndexToDisease;
    }
}
