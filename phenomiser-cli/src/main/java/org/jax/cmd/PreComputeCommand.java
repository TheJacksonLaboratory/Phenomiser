package org.jax.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jax.io.DiseaseParser;
import org.jax.services.AbstractResources;
import org.jax.services.ComputedResources;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Parameters(commandDescription = "Precompute similarity score distributions")
public class PreComputeCommand extends PhenomiserCommand {

    private static Logger logger = LoggerFactory.getLogger(PreComputeCommand.class);
    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file .hpoa")
    private String diseasePath;
    @Parameter(names = {"-db", "--diseaseDB"}, description = "choose disease database [OMIM,ORPHA]")
    private String diseaseDB = "OMIM";
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath;
    @Parameter(names = {"-numThreads"}, description = "specify the number of threads")
    private Integer numThreads = 4;
    @Parameter(names = {"-sampling", "--sampling-range"},
            description = "range of HPO terms to create similarity distributions for. Max 10",
            arity = 2)
    private List<Integer> sampling = Arrays.asList(1, 10);
    @Parameter(names = {"--debug"}, description = "use debug mode")
    private boolean debug = false;

    @Override
    public void run() {
        DiseaseParser diseaseParser = new DiseaseParser(diseasePath, hpoPath, diseaseDB);
        logger.trace("Starting precompute");
        Properties properties = new Properties();
        properties.setProperty("numThreads", Integer.toString(numThreads));
        if (cachePath != null) {
            properties.setProperty("cachingPath", cachePath);
        }
        if (sampling.get(0) > sampling.get(1)) {
            // TODO what is this?
            throw new PhenolRuntimeException("sampling.get(0) > sampling.get(1)");
        }
        properties.setProperty("sampleMin", Integer.toString(sampling.get(0)));
        properties.setProperty("sampleMax", Integer.toString(sampling.get(1)));
        AbstractResources resources = new ComputedResources(diseaseParser, properties, debug);
        resources.init();
    }
}
