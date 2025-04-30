package org.jax.cmd;

import org.jax.model.PhenomizerScore;
import org.jax.prioritizer.Phenomiser;
import org.jax.services.PhenomiserResources;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.similarity.HpoResnikSimilarity;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "phenopacket", aliases = {"P"},
        mixinStandardHelpOptions = true,
        description = "Query with a Phenopacket and rank diseases based on similarity score")
public class PhenopacketCommand extends BaseCommand  implements Callable<Integer> {

    private static Logger LOGGER = LoggerFactory.getLogger(PhenopacketCommand.class);

    @CommandLine.Option(names={"-p","--phenopacket"},
            required = true,
            description = "path to a phenopacket file",
            converter = FileExistenceValidator.class)
    private Path ppktPath;



    @Override
    public Integer call() throws Exception {
        PhenomiserResources resources = getPhenomiserResources();
        Ontology ontology = resources.getHpo();
        HpoResnikSimilarity similarity = resources.getHpoResnikSimilarity();
        Map<TermId, HpoDisease> diseaseIdToHpoDiseaseMap = resources.getDiseaseIdToHpoDiseaseMap();
        Phenomiser phenomiser = Phenomiser.scoreBased(ontology, similarity, diseaseIdToHpoDiseaseMap);
        Phenopacket ppkt = getPhenopacket(ppktPath);
        List<PhenomizerScore> query = phenomiser.query(ppkt);
        int limit = 25;
        int i = 0;
        System.out.printf("[INFO] Got Phenomizer scores for %d diseases, showing the first %d.\n", query.size(), limit);
        for (Iterator<PhenomizerScore> it = query.stream().limit(10).iterator(); it.hasNext(); ) {
            PhenomizerScore score = it.next();
            System.out.println(score);
            if (++i == limit) break;
        }
        return 0;
    }
}
