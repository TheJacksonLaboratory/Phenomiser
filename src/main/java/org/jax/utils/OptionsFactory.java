package org.jax.utils;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class OptionsFactory {

    public static Options getInstance() {
        Options options = new Options();

        Option help = Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .required(false)
                .desc("print usage").build();

        Option hpoPath = Option.builder("hpo")
                .hasArg(true)
                //.required(true)
                .desc("[required]specify full path to hp.obo").build();

        Option disease_annotation_path = Option.builder("da")
                .longOpt("disease-annotations")
                .hasArg(true)
                //.required(true)
                .desc("[required]specify full path to disease annotations").build();

        Option disease_database = Option.builder("db")
                .longOpt("disease-database")
                .hasArg(true)
                .required(false)
                .desc("specify comma-separated database [OMIM,ORPHA]").build();

        Option queryTerms = Option.builder("q")
                .longOpt("query-terms")
                .hasArg(true)
                //.required(true)
                .desc("[required]specify comma-separated query terms (e.g. HP:0003074)").build();

        Option samplingRange = Option.builder("sampling")
                .longOpt("sampling")
                .hasArg(true)
                .required(false)
                .desc("specify sampling range in format \"min-max\" (default 1-10)").build();

        Option cachePath = Option.builder("cachePath")
                .longOpt("cachePath")
                .hasArg(true)
                .required(false)
                .desc("specify the path to cache folder").build();

        Option out = Option.builder("o")
                .longOpt("out")
                .hasArg(true)
                .required(false)
                .desc("specify output path").build();

        Option debug = Option.builder("debug")
                .longOpt("debug_mode")
                .hasArg(false)
                .required(false)
                .desc("save computation time if this option is used").build();

        Option recache = Option.builder("f")
                .longOpt("force_recache")
                .hasArg(false)
                .desc("force recompute to get a new cache of resources")
                .build();

        Option thread = Option.builder("cpu")
                .longOpt("cpu")
                .hasArg(true)
                .required(false)
                .desc("specify the number of threads to use (default: 4)").build();

        Option exit = Option.builder("exit")
                .longOpt("exit")
                .hasArg(false)
                .required(false)
                .desc("exit afterward").build();


        options.addOption(help)
                .addOption(hpoPath)
                .addOption(disease_annotation_path)
                .addOption(disease_database)
                .addOption(queryTerms)
                .addOption(cachePath)
                .addOption(samplingRange)
                .addOption(out)
                .addOption(debug)
                .addOption(recache)
                .addOption(thread)
                .addOption(exit);

        return options;
    }

}
