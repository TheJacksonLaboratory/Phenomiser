package org.jax;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.jax.cmd.GridSearchCommand;
import org.jax.cmd.PhenomiserCommand;
import org.jax.cmd.PreComputeCommand;
import org.jax.cmd.QueryCommand;
import org.jax.grid.GridSearch;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.services.*;
import org.jax.utils.DiseaseDB;
import org.jax.utils.OptionsFactory;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PhenomiserApp {

    private static Logger logger = LoggerFactory.getLogger(PhenomiserApp.class);

    @Parameter(names = {"-h", "--help"}, help = true, arity = 0,description = "display this help message")
    private boolean helpInforRequested;

    private static AbstractResources resources;

    public static void main( String[] args ) {

        long startTime = System.currentTimeMillis();

        PhenomiserApp phenomiserApp = new PhenomiserApp();
        PreComputeCommand preComputeCommand = new PreComputeCommand();
        QueryCommand queryCommand = new QueryCommand();
        GridSearchCommand gridSearchCommand = new GridSearchCommand();
        JCommander jc = JCommander.newBuilder()
                .addObject(phenomiserApp)
                .addCommand("precompute", preComputeCommand)
                .addCommand("query", queryCommand)
                .addCommand("grid", gridSearchCommand)
                .build();
        jc.setProgramName("java -jar PhenomiserApp.jar");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            for (String arg : args) {
                if (arg.contains("h")) {
                    jc.usage();
                    System.exit(0);
                }
            }
            e.printStackTrace();
            jc.usage();
            System.exit(1);
        }

        String command = jc.getParsedCommand();

        if (phenomiserApp.helpInforRequested) {
            jc.usage();
            System.exit(0);
        }

        if (command == null) {
            jc.usage();
            System.exit(1);
        }

        PhenomiserCommand phenomiserCommand=null;

        switch (command) {
            case "precompute":
                phenomiserCommand = preComputeCommand;
                break;
            case "query":
                phenomiserCommand = queryCommand;
                break;
            case "grid":
                phenomiserCommand = gridSearchCommand;
                break;
            default:
                System.err.println(String.format("[ERROR] command \"%s\" not recognized",command));
                jc.usage();
                System.exit(1);

        }

        phenomiserCommand.run();

        long stopTime = System.currentTimeMillis();
        System.out.println("Phenomiser: Elapsed time was " + (stopTime - startTime)*(1.0)/1000 + " seconds.");

        //run(args);

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
        String caching_folder = HOME + File.separator + "Phenomiser_data";
        Properties properties = new Properties();


        List<Item2PValue<TermId>> result;

        try {
            commandLine = parser.parse(options, args);

            //print help info
            if (commandLine.hasOption("h") || args.length == 0) {
                formatter.printHelp("Phenomiser", options);
            }

            //get hpo path from command line args
            if (commandLine.hasOption("hpo")) {
                hpoPath = commandLine.getOptionValue("hpo");
                //System.out.println("load hpo");
            }

            //get disease annotation path from command line args
            if (commandLine.hasOption("da")) {
                diseaseAnnotationPath = commandLine.getOptionValue("da");
                //System.out.println("load disease annotations " + diseaseAnnotationPath);
            }

            //get disease databases from command line args
            if (commandLine.hasOption("db")) {
                String dbParam = StringUtils.join(commandLine.getOptionValues("db"), " ");
                dbs = Arrays.stream(dbParam.split(",")).map(StringUtils::strip)
                        .map(DiseaseDB::valueOf).collect(Collectors.toList());
                //System.out.println("db: " + commandLine.getOptionValue("db"));
            }

            //check whether to use debug mode
            if (commandLine.hasOption("debug")) {
                debugMode = true;
            }

            //number of threads for computing similarity score distributions
            if (commandLine.hasOption("cpu")) {
                numThreads = commandLine.getOptionValue("cpu");
            }

            //let user overwrite cache folder; use default otherwise
            if (commandLine.hasOption("cachePath")) {
                caching_folder = commandLine.getOptionValue("cachePath");
                //System.out.println("11111cachingPath:" + caching_folder);
                properties.setProperty("cachingPath", caching_folder);
            }

            //if user wants to force recompute resources, do so.
            if (Files.exists(Paths.get(caching_folder)) && commandLine
                    .hasOption("f")) {
                try {
                    Files.walk(Paths.get(caching_folder))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    logger.error("failed to delete some or all cached " +
                            "files");
                    System.exit(1);
                }
            }

            //if user wants to change the default values for ScoreSamplingOptions
            if (commandLine.hasOption("sampling")) {
                String[] range = commandLine.getOptionValue("sampling").split("-");
                String pattern = "^[0-9]{1,2}";
                if (range[0].matches(pattern) && range[1].matches(pattern)) {
                    properties.setProperty("sampleMin", range[0]);
                    properties.setProperty("sampleMax", range[1]);
                    logger.trace("sampling range reset to: " + range[0] + "-" + range[1]);
                } else {
                    System.err.print("range format error (correct example, 1-10");
                    System.exit(1);
                }

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
                                        hpoParser.getHpo()),
                                hpoParser.getHpo());
                        diseaseParser.init();
                    } catch (Exception e) {
                        logger.error("resource initialization error");
                        formatter.printHelp("Phenomiser", options);
                    }
                } else {
                    logger.error("resource initialization error");
                    formatter.printHelp("Phenomiser", options);
                    System.exit(1);
                }

                //if there is cached scoreDistributions, use it; otherwise, compute from scratch
                if (Files.exists(Paths.get(caching_folder))) {
                    resources = new CachedResources(hpoParser, diseaseParser, caching_folder);
                    resources.init();
                    logger.trace("using cached data");
                } else {
                    properties.setProperty("numThreads", numThreads);
                    properties.setProperty("cache", "true");
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

            if (commandLine.hasOption("grid")){
                //init hpo and disease parser
                if (hpoPath != null && diseaseAnnotationPath != null) {
                    try {
                        hpoParser = new HpoParser(hpoPath);
                        hpoParser.init();
                        diseaseParser = new DiseaseParser(
                                new HpoDiseaseAnnotationParser(diseaseAnnotationPath,
                                        hpoParser.getHpo()),
                                hpoParser.getHpo());
                        diseaseParser.init();
                    } catch (Exception e) {
                        logger.error("resource initialization error");
                        formatter.printHelp("Phenomiser", options);
                    }
                } else {
                    logger.error("resource initialization error");
                    formatter.printHelp("Phenomiser", options);
                    System.exit(1);
                }

                //if there is cached scoreDistributions, use it; otherwise, compute from scratch
                if (Files.exists(Paths.get(caching_folder))) {
                    resources = new CachedResources(hpoParser, diseaseParser, caching_folder);
                    resources.init();
                    logger.trace("using cached data");
                } else {
                    properties.setProperty("numThreads", numThreads);
                    properties.setProperty("cache", "true");
                    resources = new ComputedResources(hpoParser, diseaseParser, properties, debugMode);
                    resources.init();
                    logger.trace("using computed data");
                }
                Phenomiser.setResources(resources);
                GridSearch gridSearch = new GridSearch(resources, Arrays.asList(DiseaseDB.OMIM), 100, 10, 5, false, null);
                double[][] matrix = gridSearch.run();
                System.out.println(matrix[0][0]);

            }
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

    public static void write_query_result(List<Item2PValue<TermId>> result, @Nullable String outPath) {

//        if (adjusted_p_value == null) {
//            return;
//        }

        Writer writer = getWriter(outPath);

        try {
            writer.write("diseaseId\tdiseaseName\tp\tadjust_p\n");
        } catch (IOException e) {
            logger.error("io exception during writing header. writing output aborted.");
            return;
        }
        List<Item2PValue<TermId>> newList = new ArrayList<>(result);
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
