package org.jax;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.jax.cmd.*;
import org.jax.services.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PhenomiserApp {

    private static Logger logger = LoggerFactory.getLogger(PhenomiserApp.class);

    @Parameter(names = {"-h", "--help"}, help = true, arity = 0,description = "display this help message")
    private boolean helpInforRequested;

    private static AbstractResources resources;

    public static void main( String[] args ) {

        long startTime = System.currentTimeMillis();

        PhenomiserApp phenomiserApp = new PhenomiserApp();
        PhenomiserCommand preComputeCommand = new PreComputeCommand();
        PhenomiserCommand queryCommand = new QueryCommand();
        PhenomiserCommand gridSearchCommand = new GridSearchCommand();
        PhenomiserCommand phenopacketCmd = new PhenopacketCommand();
        JCommander jc = JCommander.newBuilder()
                .addObject(phenomiserApp)
                .addCommand("precompute", preComputeCommand)
                .addCommand("query", queryCommand)
                .addCommand("grid", gridSearchCommand)
                .addCommand("phenopacket", phenopacketCmd)
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
            case "phenopacket":
                phenomiserCommand = phenopacketCmd;
                break;
            default:
                System.err.println(String.format("[ERROR] command \"%s\" not recognized",command));
                jc.usage();
                System.exit(1);
        }

        phenomiserCommand.run();

        long stopTime = System.currentTimeMillis();
        System.out.println("Phenomiser: Elapsed time was " + (stopTime - startTime)*(1.0)/1000 + " seconds.");

    }

}
