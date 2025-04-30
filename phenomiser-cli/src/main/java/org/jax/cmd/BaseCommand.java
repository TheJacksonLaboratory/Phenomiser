package org.jax.cmd;

import org.jax.io.PhenopacketImporter;
import org.jax.prioritizer.Phenomiser;
import org.jax.services.PhenomiserResources;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.phenopackets.schema.v2.Phenopacket;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public abstract class BaseCommand {

    @CommandLine.Option(names={"-d","--data"},
            description ="directory to download data (default: ${DEFAULT-VALUE})" )
    public String datadir="data";

    protected Optional<File> hpoFile() {
        File hpoFile = new File(datadir + File.separator + "hp.json");
        return Optional.of(hpoFile).filter(File::isFile);
    }

    protected Optional<Path> phenotypeHpoaPath() {
        Path phenotypeDotHpoa = Path.of(datadir, File.separator + "phenotype.hpoa");
        return Optional.of(phenotypeDotHpoa).filter(Files::isRegularFile);
    }

    protected PhenomiserResources getPhenomiserResources() throws PhenolRuntimeException {
        File hpoFile = hpoFile().orElseThrow(() -> new PhenolRuntimeException("Could not find hp.json file"));
        Path disease = phenotypeHpoaPath().orElseThrow(() -> new PhenolRuntimeException("Could not find phenotype.hpoa file"));
        try {
            PhenomiserResources resources = PhenomiserResources.withOmim(hpoFile.toPath(), disease);
            return resources;
        } catch (IOException e) {
            throw new PhenolRuntimeException(e);
        }
    }

    protected Phenopacket getPhenopacket(Path path) throws PhenolRuntimeException {
        PhenopacketImporter ppimporter = PhenopacketImporter.fromJson(path.toFile());
        return ppimporter.getPhenoPacket();
    }

    @CommandLine.Option(names={"--diseaseDB"},
            description = "disease database (OMIM, ORPHA, or ALL; default: ${DEFAULT-VALUE})")
    protected String diseaseDB = "OMIM";

    @CommandLine.Option(names={"-o","--output"},
            description = "output path")
    protected String outPath = "outdir";


}
