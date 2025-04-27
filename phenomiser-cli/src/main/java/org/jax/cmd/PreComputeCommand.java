package org.jax.cmd;

import org.jax.io.DiseaseParser;
import org.jax.services.AbstractResources;
import org.jax.services.ComputedResources;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@CommandLine.Command(name = "precompute", aliases = {"C"},
        mixinStandardHelpOptions = true,
        description = "Precompute similarity score distributions")
public class PreComputeCommand extends PhenomiserCommand {

    private static Logger logger = LoggerFactory.getLogger(PreComputeCommand.class);


    private String cachePath = "FAKE";

    @CommandLine.Option(names={"--numThreads"},
            description = "number of threads")
    private Integer numThreads = 4;
    /*@Parameter(names = {"-sampling", "--sampling-range"},
            description = "range of HPO terms to create similarity distributions for. Max 10",
            arity = 2)*/
    private List<Integer> sampling = Arrays.asList(1, 10);
    @CommandLine.Option(names={"--debug"},
            description = "use debug mode")
    private boolean debug = false;

    @Override
    public void run() {
        Ontology ontology = OntologyLoader.loadOntology(new File(hpoPath));
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, ontology);
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, ontology);
        try {
            diseaseParser.init();
        } catch (PhenolException e) {
            e.printStackTrace();
            System.exit(1);
        }
        logger.trace("1111");
        Properties properties = new Properties();
        properties.setProperty("numThreads", Integer.toString(numThreads));
        if (cachePath != null) {
            properties.setProperty("cachingPath", cachePath);
        }

        if (sampling.get(0) > sampling.get(1)) {
            System.exit(1);
        }


//        properties.setProperty("diseaseDB", diseaseDB);
        properties.setProperty("sampleMin", Integer.toString(sampling.get(0)));
        properties.setProperty("sampleMax", Integer.toString(sampling.get(1)));

        AbstractResources resources = new ComputedResources(ontology, diseaseParser, properties, debug);
        resources.init();
    }
}
