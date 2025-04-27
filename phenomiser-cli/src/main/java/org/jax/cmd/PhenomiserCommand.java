package org.jax.cmd;

import picocli.CommandLine;

public abstract class PhenomiserCommand {

    @CommandLine.Option(names={"--hpo-json"},
            required=true,
            description = "path to hp.json")
    protected String hpoPath;



    @CommandLine.Option(names={"--disease_annotation"},
            required=true,
            description = "path to disease annotation file")
    protected String diseasePath;

    @CommandLine.Option(names={"--diseaseDB"},
            description = "disease database")
    protected String diseaseDB = "OMIM";

    @CommandLine.Option(names={"-o","--output"},
            required = true,
            description = " output path")
    protected String outPath;

    public abstract void run();
}
