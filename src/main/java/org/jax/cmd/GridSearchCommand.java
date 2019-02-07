package org.jax.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jax.Phenomiser;
import org.jax.grid.GridSearch;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Run a grid search over number of terms and number of noise terms for
 * phenotype-only LR2PG. Can be run with or with imprecision.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@Parameters(commandDescription = "Grid search for simulation of phenotype-only cases")
public class GridSearchCommand extends PhenomiserCommand {
    private static Logger logger = LoggerFactory.getLogger(GridSearchCommand.class);
    final String HOME = System.getProperty("user.home");
    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file")
    private String diseasePath;
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath = HOME + File.separator + "Phenomiser_data";;
    @Parameter(names = {"-db", "--diseaseDB"},
            description = "choose disease database [OMIM,ORPHA]")
    private String diseaseDB = "OMIM";

    @Parameter(names={"-c","--n_cases"}, description="Number of cases to simulate")
    private int n_cases_to_simulate = 100;
    @Parameter(names = { "-signal", "--n-diseaseTerm"}, description = "Number of disease terms")
    private int n_diseaseTerm = 10;
    @Parameter(names = {"-noise", "--noise"}, description = "Number of noise terms")
    private int n_noiseTerm = 5;
    @Parameter(names={"-i","--imprecision"}, description="Use imprecision?")
    private boolean imprecise_phenotype = false;
    @Parameter(names = {"-o", "--output"}, description = "Output path")
    private String outPath;

    private AbstractResources resources;

    @Override
    public void run() {
        HpoParser hpoParser = new HpoParser(hpoPath);
        hpoParser.init();
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, hpoParser.getHpo());
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, hpoParser.getHpo());
        try {
            diseaseParser.init();
        } catch (PhenolException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (!Files.exists(Paths.get(cachePath))){
            System.err.print("Cannot find caching data at " + cachePath);
            System.exit(1);
        }
        resources = new CachedResources(hpoParser, diseaseParser, cachePath);
        resources.init();
        Phenomiser.setResources(resources);

        GridSearch gridSearch = new GridSearch(resources, n_cases_to_simulate, n_diseaseTerm, n_noiseTerm, imprecise_phenotype);

        double [][] m = gridSearch.run();

        Writer writer = getWriter(outPath);

        try {
            GridSearch.write(m, writer);
        } catch (Exception e) {
            logger.error("Rank matrix is successfully created but cannot be written out due to an IOException");
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
            logger.info("out path not found. writing to console: ");
            writer = new OutputStreamWriter(System.out);
        }
        return writer;
    }

}
