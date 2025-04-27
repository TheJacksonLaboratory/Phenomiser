package org.jax.cmd;


import org.jax.Phenomiser;
import org.jax.io.DiseaseParser;
import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(name = "query", aliases = {"Q"},
        mixinStandardHelpOptions = true,
        description = "Query with a list of HPO terms and rank diseases based on similarity score")
public class QueryCommand extends PhenomiserCommand {
    private static Logger LOGGER = LoggerFactory.getLogger(QueryCommand.class);
    final String HOME = System.getProperty("user.home");





    @CommandLine.Option(names={"-q","--query"},
            required = true,
            description = "HPO terms to query")
    private String query;



    private AbstractResources resources;


    @Override
    public void run() {
        final String cachePath = "FAKE"; // TODO
        Ontology ontology = OntologyLoader.loadOntology(new File(hpoPath));
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, ontology);
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, ontology);
        try {
            diseaseParser.init();
        } catch (PhenolException e) {
            LOGGER.error(e.getMessage(),e);
            System.exit(1);
        }

        if (!Files.exists(Paths.get(cachePath))){
            System.err.print("Cannot find caching data at " + cachePath);
            System.exit(1);
        }
        List<TermId> queryList = Arrays.stream(query.split(",")).map(TermId::of).collect(Collectors.toList());
        resources = new CachedResources(ontology, diseaseParser, cachePath, Math.min(queryList.size(), 10));
        resources.init();
        Phenomiser.setResources(resources);


        List<DiseaseDB> db = Arrays.stream(diseaseDB.split(",")).map(DiseaseDB::valueOf).collect(Collectors.toList());
        List<Item2PValueAndSimilarity<TermId>> result = Phenomiser.query(queryList, db);

        //output query result
        if (!result.isEmpty()) {
            write_query_result(result, outPath);
        }
    }

    public static Writer getWriter(String path) {
        Writer writer;
        try {
            writer = new FileWriter(new File(path));
        } catch (Exception e) {
            LOGGER.info("out path not found. writing to console: ");
            writer = new OutputStreamWriter(System.out);
        }
        return writer;
    }

    public void write_query_result(List<Item2PValueAndSimilarity<TermId>> result, @Nullable String
            outPath) {

        Writer writer = getWriter(outPath);

        try {
            writer.write("diseaseId\tdiseaseName\tp\tadjust_p" +
                    "\tsimilarityScore" +
                    "\n");
        } catch (IOException e) {
            LOGGER.error("io exception during writing header. writing output aborted.");
            return;
        }
        List<Item2PValueAndSimilarity<TermId>> newList = new ArrayList<>(result);
        Collections.sort(newList);

        newList.stream().forEach(e -> {
            try {
                writer.write(e.getItem().getValue());
                writer.write("\t");
                writer.write(resources.getDiseaseMap().get(e.getItem()).getName());
                writer.write("\t");
                writer.write(Double.toString(e.getRawPValue()));
                writer.write("\t");
                writer.write(Double.toString(e.getAdjustedPValue()));
                writer.write("\t");
                writer.write(Double.toString(e.getSimilarityScore()));
                writer.write("\n");
            } catch (IOException exception) {
                LOGGER.error("IO exception during writing out adjusted p values");
            }

        });

        try {
            writer.close();
        } catch (IOException e) {
            LOGGER.error("IO exception during closing writer");
        }
    }
}
