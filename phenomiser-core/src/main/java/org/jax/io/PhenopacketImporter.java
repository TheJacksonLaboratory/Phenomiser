package org.jax.io;

import com.google.protobuf.util.JsonFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class ingests a phenopacket, which is required to additionally contain the
 * path of a VCF file that will be used for the analysis.
 * @author Peter Robinson
 */
public class PhenopacketImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhenopacketImporter.class);
    /** The Phenopacket that represents the individual being sequenced in the current run. */
    private final Phenopacket phenoPacket;
    /** A list of non-negated HPO terms observed in the subject of this Phenopacket. */
    private List<TermId> hpoTerms;
    /** A list of negated HPO terms observed in the subject of this Phenopacket. */
    private List<TermId> negatedHpoTerms;
    /** Name of the proband of the Phenopacket (corresponds to the {@code id} element of the phenopacket). */
    private final String samplename;


    /**
     * Factory method to obtain a PhenopacketImporter object starting from a phenopacket in Json format
     * @param pathToJsonPhenopacketFile -- path to the phenopacket
     * @return {@link PhenopacketImporter} object corresponding to the PhenoPacket
     * @throws ParseException if the JSON code cannot be parsed
     * @throws IOException if the File cannot be found
     */
    public static PhenopacketImporter fromJson(String pathToJsonPhenopacketFile)  {
        JSONParser parser = new JSONParser();
        LOGGER.trace("Importing Phenopacket: " + pathToJsonPhenopacketFile);
        try {
            Object obj = parser.parse(new FileReader(pathToJsonPhenopacketFile));
            JSONObject jsonObject = (JSONObject) obj;
            String phenopacketJsonString = jsonObject.toJSONString();
            Phenopacket.Builder builder = Phenopacket.newBuilder();
            JsonFormat.Parser jfparser = JsonFormat.parser();
            jfparser.merge(phenopacketJsonString, builder);
            Phenopacket phenopacket = builder.build();
            /*
            Phenopacket.Builder phenoPacketBuilder = Phenopacket.newBuilder();
            JsonFormat.parser().merge(phenopacketJsonString, phenoPacketBuilder);
            Phenopacket phenopacket = phenoPacketBuilder.build();*/
            return new PhenopacketImporter(phenopacket);
        } catch (IOException|ParseException e1) {
            LOGGER.error(e1.getMessage());
            throw new RuntimeException("Could not load phenopacket at " + pathToJsonPhenopacketFile);
        }

    }

    public PhenopacketImporter(Phenopacket ppack){
        this.phenoPacket=ppack;
        this.samplename = this.phenoPacket.getSubject().getId();
        extractProbandHpoTerms();
        extractNegatedProbandHpoTerms();

    }

    public List<TermId> getHpoTerms() {
        return hpoTerms;
    }

    public List<TermId> getNegatedHpoTerms() {
        return negatedHpoTerms;
    }

   public String getSamplename() {
        return samplename;
    }

    public String getDiagnosisCurie() {
        if (this.phenoPacket.getDiseasesCount() !=1) { // should never happen with our simulations
            throw new RuntimeException("Phenopacket did not have a single disease: "+phenoPacket);
        }
        return this.phenoPacket.getDiseases(0).getTerm().getId();
    }

    public boolean checkForObsoleteTerms(Ontology ontology) {
        boolean clean=true;
        for (TermId tid : hpoTerms) {
            if (ontology.getObsoleteTermIds().contains(tid)) {
                clean=false;
                LOGGER.error("Use of obsolete term id: {}",tid);
                Term term = ontology.getTermMap().get(tid);
                if (term==null) {
                    LOGGER.error("Could not find TermObject.");
                    continue;
                }
                LOGGER.error("The corresponding term label is {}",term.getName());
                LOGGER.error("We recommend replacing the term id with the current id: {}", term.getId().getValue());
            }
        }
        for (TermId tid : negatedHpoTerms) {
            if (ontology.getObsoleteTermIds().contains(tid)) {
                clean=false;
                LOGGER.error("Use of obsolete term id: {}",tid);
                Term term = ontology.getTermMap().get(tid);
                if (term==null) {
                    LOGGER.error("Could not find TermObject.");
                    continue;
                }
                LOGGER.error("The corresponding term label is {}",term.getName());
                LOGGER.error("We recommend replacing the term id with the current id: {}", term.getId().getValue());
            }
        }

        return clean;
    }



    /**
     * This method extracts a list of
     * all of the non-negated HPO terms that are annotated to the proband of this
     * phenopacket. Note that we use "distinct" to get only distinct elements, defensively,
     * even though a valid phenopacket should not have duplicates.
     */
    private void extractProbandHpoTerms() {
        this.hpoTerms= phenoPacket
                .getPhenotypicFeaturesList()
                .stream()
                .distinct()
                .filter(((Predicate<PhenotypicFeature>) PhenotypicFeature::getExcluded).negate()) // i.e., just take non-negated phenotypes
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .map(TermId::of)
                .toList();
    }

    /**
     *  Extract a list of all negated HPO terms associated with the proband.
     */
    private void extractNegatedProbandHpoTerms() {
        this.negatedHpoTerms = phenoPacket
                .getPhenotypicFeaturesList()
                .stream()
                .filter(PhenotypicFeature::getExcluded) // i.e., just take negated phenotypes
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .map(TermId::of)
                .toList();
    }


}
