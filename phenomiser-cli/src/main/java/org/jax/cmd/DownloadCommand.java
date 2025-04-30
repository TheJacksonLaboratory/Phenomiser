package org.jax.cmd;


import org.monarchinitiative.biodownload.FileDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import org.monarchinitiative.biodownload.BioDownloader;
import org.monarchinitiative.biodownload.BioDownloaderBuilder;
/**
 * Download a number of files needed for the analysis. We download by default to a subdirectory called
 * {@code data}, which is created if necessary. We download the files {@code hp.obo}, {@code phenotype.hpoa},
 * {@code Homo_sapiencs_gene_info.gz}, and {@code mim2gene_medgen}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */

@CommandLine.Command(name = "download", aliases = {"D"},
        mixinStandardHelpOptions = true,
        description = "Download files for fenominal")
public class DownloadCommand extends BaseCommand implements Callable<Integer>{
    private static final Logger logger = LoggerFactory.getLogger(DownloadCommand.class);


    @CommandLine.Option(names={"-w","--overwrite"}, description = "overwrite previously downloaded files (default: ${DEFAULT-VALUE})")
    public boolean overwrite;

    @Override
    public Integer call() throws FileDownloadException {
        logger.info(String.format("Download analysis to %s", datadir));
        Path destination = Paths.get(datadir);
        BioDownloaderBuilder builder = BioDownloader.builder(destination);
        BioDownloader downloader = builder
                .hpoJson()
                .hpDiseaseAnnotations()
                .build();
        List<File> files = downloader.download();
        return 0;
    }

}
