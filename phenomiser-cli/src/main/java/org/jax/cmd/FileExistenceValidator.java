package org.jax.cmd;



import java.nio.file.Files;
import java.nio.file.Path;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;


public class FileExistenceValidator implements ITypeConverter<Path> {
    @Override
    public Path convert(String value) throws TypeConversionException {
        Path path = Path.of(value);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new TypeConversionException("File does not exist or is not a regular file: " + value);
        }
        return path;
    }
}