package org.jax;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.services.*;
import org.jax.utils.DiseaseDB;
import org.jax.utils.OptionsFactory;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.PValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PhenomiserApp {

    private static Logger logger = LoggerFactory.getLogger(PhenomiserApp.class);

    private static AbstractResources resources;

    private static Phenomiser phenomiser;

    public static void main( String[] args ) {

        run(args);

    }

    public static void run(String[] args) {
        //set up command line options
        Options options = OptionsFactory.getInstance();
        HelpFormatter formatter = new HelpFormatter();

        //parse and handle command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        //parameters from command line
        String hpoPath = null;
        String diseaseAnnotationPath = null;
        List<DiseaseDB> dbs = Arrays.asList(DiseaseDB.OMIM, DiseaseDB.ORPHA); //default
        List<TermId> queryTerms;
        String outPath;
        boolean debugMode = false;
        String numThreads = "4";

        HpoParser hpoParser = null;
        DiseaseParser diseaseParser = null;
        final String HOME = System.getProperty("user.home");
        final String caching_folder = HOME + File.separator + "Phenomiser_data";
        Properties properties = new Properties();

        Map<TermId, PValue> result;

        try {
            commandLine = parser.parse(options, args);
            //print help info
            if (commandLine.hasOption("h") || args.length == 0) {
                formatter.printHelp("Phenomiser", options);
            }

            //get hpo path from command line args
            if (commandLine.hasOption("hpo")) {
                hpoPath = commandLine.getOptionValue("hpo");
                System.out.println("load hpo");
            }

            //get disease annotation path from command line args
            if (commandLine.hasOption("da")) {
                diseaseAnnotationPath = commandLine.getOptionValue("da");
                System.out.println("load disease annotations");
            }

            //get disease databases from command line args
            if (commandLine.hasOption("db")) {
                String dbParam = StringUtils.join(commandLine.getOptionValues("db"), " ");
                dbs = Arrays.stream(dbParam.split(",")).map(StringUtils::strip)
                        .map(DiseaseDB::valueOf).collect(Collectors.toList());
                System.out.println("db: " + commandLine.getOptionValue("db"));
            }

            //check whether to use debug mode
            if (commandLine.hasOption("debug")) {
                debugMode = true;
            }

            //number of threads for computing similarity score distributions
            if (commandLine.hasOption("cpu")) {
                numThreads = commandLine.getOptionValue("cpu");
            }

            //the real working request: do query request with terms from command line
            if (commandLine.hasOption("q")) {
                //init hpo and disease parser
                if (hpoPath != null && diseaseAnnotationPath != null) {
                    try {
                        hpoParser = new HpoParser(hpoPath);
                        hpoParser.init();
                        diseaseParser = new DiseaseParser(
                                new HpoDiseaseAnnotationParser(diseaseAnnotationPath,
                                        (Ontology) hpoParser.getHpo()),
                                (Ontology) hpoParser.getHpo());
                        diseaseParser.init();
                    } catch (Exception e) {
                        logger.error("resource initialization error");
                        formatter.printHelp("Phenomiser", options);
                    }
                } else {
                    logger.error("resource initialization error");
                    formatter.printHelp("Phenomiser", options);
                }

                //if there is cached scoreDistributions, use it; otherwise, compute from scratch
                if (Files.exists(Paths.get(caching_folder))) {
                    resources = new CachedResources(hpoParser, diseaseParser, caching_folder);
                    resources.init();
                    logger.trace("using cached data");
                } else {
                    properties.setProperty("numThreads", numThreads);
                    resources = new ComputedResources(hpoParser, diseaseParser, properties, debugMode);
                    resources.init();
                    logger.trace("using computed data");
                }

                //get query terms
                String queryParam = StringUtils.join(commandLine.getOptionValues("q"), " ");
                queryTerms = Arrays.asList(queryParam.split(",")).stream()
                        .map(StringUtils::strip)
                        .map(e -> {
                    TermId termId = null;
                    if (e.split(":").length == 2) {
                        String[] elements = e.split(":");
                        termId = TermId.of(elements[0], elements[1]);
                    }
                    return termId;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                logger.trace("number of query terms: " + queryTerms.size());
                queryTerms.forEach(t -> logger.info(t.toString()));
                Phenomiser.setResources(resources);
                //perform query
                result = Phenomiser.query(queryTerms, dbs);

                //output query result
                if (!result.isEmpty()) {
                    write_query_result(result, commandLine.getOptionValue("o"));
                }
            }

            //exit if requested
            if (commandLine.hasOption("exit")) {
                System.out.println("exiting");
            }
            //BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        } catch (ParseException e) {
            e.printStackTrace();
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

    public static void write_query_result(Map<TermId, PValue> adjusted_p_value, @Nullable String outPath) {

        if (adjusted_p_value == null) {
            return;
        }

        Writer writer = getWriter(outPath);

        try {
            writer.write("diseaseId\tp\tadjust_p\n");
        } catch (IOException e) {
            logger.error("io exception during writing header. writing output aborted.");
            return;
        }

        adjusted_p_value.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> {
            try {
                writer.write(e.getKey().getValue());
                writer.write("\t");
                writer.write(Double.toString(e.getValue().getRawPValue()));
                writer.write("\t");
                writer.write(Double.toString(e.getValue().getAdjustedPValue()));
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
