package org.jax;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.jax.cmd.*;
import org.jax.services.*;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PhenomiserApp {

    private static Logger LOGGER = LoggerFactory.getLogger(PhenomiserApp.class);

    @Parameter(names = {"-h", "--help"}, help = true, arity = 0,description = "display this help message")
    private boolean helpRequested;

    private static AbstractResources resources;

    public static void main( String[] args ) {

        long startTime = System.currentTimeMillis();

        PhenomiserApp phenomiserApp = new PhenomiserApp();
        PreComputeCommand preComputeCommand = new PreComputeCommand();
        QueryCommand queryCommand = new QueryCommand();
        GridSearchCommand gridSearchCommand = new GridSearchCommand();
        PhenopacketCommand phenopacket = new PhenopacketCommand();
        JCommander jc = JCommander.newBuilder()
                .addObject(phenomiserApp)
                .addCommand("precompute", preComputeCommand)
                .addCommand("query", queryCommand)
                .addCommand("grid", gridSearchCommand)
                .addCommand("phenopacket",phenopacket)
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
            LOGGER.error(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        String command = jc.getParsedCommand();

        if (phenomiserApp.helpRequested) {
            jc.usage();
            System.exit(0);
        }

        if (command == null) {
            jc.usage();
            System.exit(1);
        }

        PhenomiserCommand phenomiserCommand = switch(command) {
            case "precompute" -> preComputeCommand;
            case "query" -> queryCommand;
            case "grid" ->  gridSearchCommand;
            case "phenopacket" -> phenopacket;
            default -> {
                jc.usage();
                throw new PhenolRuntimeException(String.format("[ERROR] command \"%s\" not recognized.\n",
                        command));
            }
        };

        phenomiserCommand.run();

        long stopTime = System.currentTimeMillis();
        System.out.printf("Phenomiser: Elapsed time was %f seconds.\n",
                (stopTime - startTime)*(1.0)/1000);
    }

}
