package org.jax;

import picocli.CommandLine;
import org.jax.cmd.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "Phenomiser", mixinStandardHelpOptions = true, version = "0.0.3",
        description = "Phenomizer implementation")
public class PhenomiserApp implements Callable<Integer> {

    private static Logger LOGGER = LoggerFactory.getLogger(PhenomiserApp.class);


    public static void main(String[] args){
        LOGGER.info("Starting Phenomiser");
        if (args.length == 0) {
            // if the user doesn't pass any command or option, add -h to show help
            args = new String[]{"-h"};
        }
        CommandLine cline = new CommandLine(new PhenomiserApp())
                .addSubcommand("download", new DownloadCommand())
                .addSubcommand("phenopacket", new PhenopacketCommand())
                ;
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        LOGGER.trace("Finished Phenomiser");
        System.exit(exitCode);
    }


    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }




}
