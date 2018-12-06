package org.jax;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class OptionsFactory {

    public static Options getInstance() {
        Options options = new Options();

        Option help = Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .desc("print usage").build();

        Option hpoPath = Option.builder("hpo")
                .hasArg(true)
                .desc("specify full path to hp.obo").build();

        Option disease_annotation_path = Option.builder("da")
                .longOpt("disease-annotations")
                .hasArg(true)
                .desc("specify full path to disease annotations").build();

        Option disease_database = Option.builder("db")
                .longOpt("disease-database")
                .hasArg(true)
                .desc("specify OMIM and/or Ophanet").build();

        Option queryTerms = Option.builder("q")
                .longOpt("query-terms")
                .hasArg(true)
                .desc("specify space separated query terms (with prefix)").build();


        Option exit = Option.builder("exit")
                .longOpt("exit")
                .hasArg(false)
                .desc("exit afterward").build();


        options.addOption(help)
                .addOption(hpoPath)
                .addOption(disease_annotation_path)
                .addOption(disease_database)
                .addOption(queryTerms)
                .addOption(exit);

        return options;
    }

}
