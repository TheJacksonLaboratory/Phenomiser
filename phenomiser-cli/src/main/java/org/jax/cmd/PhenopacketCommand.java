package org.jax.cmd;

import com.beust.jcommander.Parameter;
import org.jax.Phenomiser;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.io.PhenopacketImporter;
import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.utils.DiseaseDB;
import org.json.simple.parser.ParseException;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
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

public class PhenopacketCommand extends PhenomiserCommand {

    private static Logger logger = LoggerFactory.getLogger(QueryCommand.class);
    private final String HOME = System.getProperty("user.home");

    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file")
    private String diseasePath;
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath = HOME + File.separator + "Phenomiser_data";
    @Parameter(names = {"-db", "--diseaseDB"},
            description = "choose disease database [OMIM,ORPHA]")
    private String diseaseDB = "OMIM";
    @Parameter(names = {"-pp", "--phenopacket"}, description = "specify the path to a phenopachet file")
    private String phenopacket;

    @Parameter(names = {"-o", "--output"}, description = "specify output path")
    private String outPath="phenomiser-results.txt";

    private AbstractResources resources;

    private  Writer writer;


    /**
     * Simulate a case using one Phenopacket. Only use OMIM data.
     * @param phenopacketPath
     */
    private void runOneSimulation(String phenopacketPath) {
        List<TermId> queryList;

        PhenopacketImporter ppimporter = PhenopacketImporter.fromJson(phenopacketPath);
        String correctDiagnosis = ppimporter.getDiagnosisCurie();
        TermId correctTid=TermId.of(correctDiagnosis);
        queryList = ppimporter.getHpoTerms();

        //List<DiseaseDB> db = Arrays.stream(diseaseDB.split(",")).map(DiseaseDB::valueOf).collect(Collectors.toList());
        List<DiseaseDB> db = new ArrayList<>();
        db.add(DiseaseDB.OMIM);
        List<Item2PValueAndSimilarity<TermId>> result = Phenomiser.query(queryList, db);
        int r = 0;
        if (result==null) {
            logger.error("result was NULL for " + phenopacketPath);
            return;
        }
        for (Item2PValueAndSimilarity<TermId> i2p : result) {
            r++;
            if (i2p.getItem().equals(correctTid)) {
                System.out.println("Rank of correct disease ("+correctTid.getValue() + ")="+ r);
                if (this.writer!= null) {
                    try {
                        writer.write(r+"\t"); // rank
                        writer.write(i2p.getItem().getValue() + "\t");
                        writer.write(resources.getDiseaseMap().get(i2p.getItem()).getName() +"\t");
                        writer.write(i2p.getRawPValue() + "\t");
                        writer.write(i2p.getAdjustedPValue() + "\t");
                        writer.write(i2p.getSimilarityScore() + "\n");
                        writer.flush();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        //output query result
       // if (!result.isEmpty()) {
         //   write_query_result(result, outPath);
        //}

    }





    @Override
    public void run() {
        HpoParser hpoParser = new HpoParser(hpoPath);
        hpoParser.init();
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, hpoParser.getHpo());
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, hpoParser.getHpo());
        try {
            diseaseParser.init();
        } catch (PhenolException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            this.writer = new FileWriter(new File(this.outPath));
            writer.write("rank\tdiseaseId\tdiseaseName\tp\tadjust_p\tsimilarityScore\n");
        } catch (IOException e){
            e.printStackTrace();

        }

        if (!Files.exists(Paths.get(cachePath))){
            System.err.print("Cannot find caching data at " + cachePath);
            System.exit(1);
        }
        resources = new CachedResources(hpoParser, diseaseParser, cachePath);
        resources.init();
        Phenomiser.setResources(resources);

        File phenofile = new File(phenopacket);
        if (phenofile.isDirectory()) {
            // run across multiple phenopackets
            int counter=0;
            for (final File fileEntry : phenofile.listFiles()) {
                if (fileEntry.isFile() && fileEntry.getAbsolutePath().endsWith(".json")) {
                    logger.info("\tPhenopacket: \"{}\"", fileEntry.getAbsolutePath());
                    System.out.println(++counter + ") "+ fileEntry.getName());
                    runOneSimulation(fileEntry.getAbsolutePath());
                }
            }
        } else {
            // phenopacket is a single file
            runOneSimulation(phenopacket);
        }



    }

    private static Writer getWriter(String path) {
        Writer writer;
        try {
            writer = new FileWriter(new File(path));
        } catch (Exception e) {
            logger.info("out path not found. writing to console: ");
            writer = new OutputStreamWriter(System.out);
        }
        return writer;
    }

    public void write_query_result(List<Item2PValueAndSimilarity<TermId>> result, @Nullable String
            outPath) {

        Writer writer = getWriter(outPath);

        try {
            writer.write("rank\tdiseaseId\tdiseaseName\tp\tadjust_p" +
                    "\tsimilarityScore" +
                    "\n");
        } catch (IOException e) {
            logger.error("io exception during writing header. writing output aborted.");
            return;
        }
        List<Item2PValueAndSimilarity<TermId>> newList = new ArrayList<>(result);
        Collections.sort(newList);
        int r=0;
        for (Item2PValueAndSimilarity<TermId> e : newList){

            try {
                r++;
                writer.write(r+")\t"); // rank
                writer.write(e.getItem().getValue() + "\t");
                writer.write(resources.getDiseaseMap().get(e.getItem()).getName() +"\t");
                writer.write(e.getRawPValue() + "\t");
                writer.write(e.getAdjustedPValue() +"\t");
                writer.write(e.getSimilarityScore() +"\n");
            } catch (IOException exception) {
                logger.error("IO exception during writing out adjusted p values");
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            logger.error("IO exception during closing writer");
        }
    }
}
