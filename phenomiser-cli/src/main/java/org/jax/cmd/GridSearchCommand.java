package org.jax.cmd;

import org.jax.Phenomiser;
import org.jax.grid.GridSearch;
import org.jax.io.DiseaseParser;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Run a grid search over number of terms and number of noise terms. Can be run with or with imprecision.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:aaron.zhang@jax.org">Aaron Zhang</a>
 */

@CommandLine.Command(name = "grid", aliases = {"G"},
        mixinStandardHelpOptions = true,
        description = "Grid search for simulation of phenotype-only cases")
public class GridSearchCommand extends PhenomiserCommand {
    private static Logger LOGGER = LoggerFactory.getLogger(GridSearchCommand.class);
    final String HOME = System.getProperty("user.home");
    private String cachePath = HOME + File.separator + "Phenomiser_data";;

    @CommandLine.Option(names={"-c","--n_cases"},
            description = "Number of cases to simulate")
    private int n_cases_to_simulate = 100;

    @CommandLine.Option(names={"--n-diseaseTerm"},
            description = "Number of disease terms")
    private int n_diseaseTerm = 10;


    @CommandLine.Option(names={"--noise"},
            description = "Number of noise terms")
    private int n_noiseTerm = 4;


    @CommandLine.Option(names={"--imprecision"},
            description = "Use imprecision?")
    private boolean imprecise_phenotype = false;

    @CommandLine.Option(names={"--seed"},
            description = "Set random number generator seed for simulation")
    private Integer seed = 42;

    private AbstractResources resources;

    @Override
    public void run() {

        checkSignal();

        Ontology ontology = OntologyLoader.loadOntology(new File(hpoPath));
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, ontology);
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, ontology);
        try {
            diseaseParser.init();
        } catch (PhenolException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }

        if (!Files.exists(Paths.get(cachePath))){
            System.err.print("Cannot find caching data at " + cachePath);
            System.exit(1);
        }
        resources = new CachedResources(ontology, diseaseParser, cachePath,
                1);
        resources.init();
        Phenomiser.setResources(resources);

        List<DiseaseDB> targetDb = Arrays.stream(diseaseDB.split(",")).map(DiseaseDB::valueOf).collect(Collectors.toList());

        Random random = null;
        if (seed != null) {
            random = new Random(seed);
        }
        //checkScoreDistributionsArePrecomputed();
        GridSearch gridSearch = new GridSearch(resources, targetDb, n_cases_to_simulate, n_diseaseTerm, n_noiseTerm, imprecise_phenotype, random);

        double [][] m = gridSearch.run();

        Writer writer = getWriter(outPath);

        try {
            GridSearch.write(m, writer);
        } catch (Exception e) {
            LOGGER.error("Rank matrix is successfully created but cannot be written out due to an IOException");
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Writer getWriter(String path) {
        Writer writer;
        try {
            writer = new FileWriter(new File(path));
        } catch (Exception e) {
            LOGGER.info("out path not found. writing to console: ");
            writer = new OutputStreamWriter(System.out);
        }
        return writer;
    }

    private void checkSignal() {
        if (n_diseaseTerm <= 0) {
            System.err.print("Signal needs to be at least 1");
            System.exit(1);
        }
    }

    private void checkScoreDistributionsArePrecomputed() {
        Optional<Integer> max = resources.getScoreDistributions().keySet().stream().max(Comparator.comparingInt(Integer::intValue));
        if (max.isPresent()) {
            int maxScoreDistribution = max.get();
            int maxSimulate = this.n_diseaseTerm + this.n_noiseTerm;
            if (maxSimulate > maxScoreDistribution) {
                if (maxScoreDistribution == 10) {
                    //this is okay, as >10 are treated as 10
                } else {
                    //this is not okay, as score distributions between (max, maxSimulate)
                    //will not be found
                    System.err.print("You do not have all score distributions for the simulation. " +
                            "ReRun precompute.");
                    System.exit(1);
                }
            }
        }
    }

}
