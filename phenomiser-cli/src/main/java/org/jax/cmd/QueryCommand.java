package org.jax.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jax.Phenomiser;
import org.jax.io.DiseaseParser;
import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Query with a list of HPO terms and rank diseases based on similarity score")
public class QueryCommand extends PhenomiserCommand {
    private static Logger logger = LoggerFactory.getLogger(QueryCommand.class);
    final String HOME = System.getProperty("user.home");

    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file")
    private String diseasePath;
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath = HOME + File.separator + "Phenomiser_data";
    @Parameter(names = {"-db", "--diseaseDB"},
            description = "choose disease database [OMIM,ORPHA]")
    private String diseaseDB = "OMIM";
    @Parameter(names = {"-query", "--query-terms"}, description = "specify HPO terms to query")
    private String query;

    @Parameter(names = {"-o", "--output"}, description = "specify output path")
    private String outPath;

    private AbstractResources resources;


    @Override
    public void run() {
        DiseaseParser diseaseParser = new DiseaseParser(diseasePath, hpoPath, diseaseDB);

        if (!Files.exists(Paths.get(cachePath))){
            System.err.print("Cannot find caching data at " + cachePath);
            System.exit(1);
        }
        List<TermId> queryList = Arrays.stream(query.split(",")).map(TermId::of).collect(Collectors.toList());
        resources = new CachedResources(diseaseParser, cachePath, Math.min(queryList.size(), 10));
        resources.init();
        Phenomiser.setResources(resources);


        List<DiseaseDB> db = Arrays.stream(diseaseDB.split(",")).map(DiseaseDB::valueOf).collect(Collectors.toList());
        List<Item2PValueAndSimilarity> result = Phenomiser.query(queryList, db);

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
            logger.info("out path not found. writing to console: ");
            writer = new OutputStreamWriter(System.out);
        }
        return writer;
    }

    public void write_query_result(List<Item2PValueAndSimilarity> result, @Nullable String
            outPath) {

        Writer writer = getWriter(outPath);

        try {
            writer.write("diseaseId\tdiseaseName\tp\tadjust_p" +
                    "\tsimilarityScore" +
                    "\n");
        } catch (IOException e) {
            logger.error("io exception during writing header. writing output aborted.");
            return;
        }
        List<Item2PValueAndSimilarity> newList = new ArrayList<>(result);
        Collections.sort(newList);

        newList.forEach(e -> {
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
                logger.error("IO exception during writing out adjusted p values");
            }

        });

        try {
            writer.close();
        } catch (IOException e) {
            logger.error("IO exception during closing writer");
        }
    }
}
