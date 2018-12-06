package org.jax.io;
import com.google.common.collect.Sets;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermIds;

import java.util.*;

public class DiseaseParser {


    private HpoOntology hpo;

    private HpoDiseaseAnnotationParser diseaseAnnotationParser;

    private Map<TermId, HpoDisease> diseaseMap; //key is disease termID, value is disease model (has phenotype list)

    private Map<TermId, Collection<TermId>> diseaseIdToHpoTermIds; //key is disease id; value is a collection of hpo termIds

    private Map<TermId, Collection<TermId>> hpoTermIdToDiseaseIds; //key is hpo termId; value is a collection of disease ids

    public DiseaseParser(String diseaseAnnotation, HpoOntology hpoOntology) {
        this.hpo = hpoOntology;
        this.diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseaseAnnotation, hpoOntology);
    }

    public void parse() throws PhenolException {
        diseaseMap = diseaseAnnotationParser.parse();

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

}
