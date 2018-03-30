package com.seal.contracts.generator.file;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jantonak on 13/06/17.
 */
@Slf4j
public class DataFolderInitializer extends FolderInitializer {

    private final static String FOLDERNAME = "in";
    private final static String configFile = "config.json";
    private static final InFolderReplicator replicator = new InFolderReplicator();

    public DataFolderInitializer() {
        super(FOLDERNAME);
    }

    protected void handleJar(final Path jar) throws URISyntaxException, IOException {
        URI zipURI = new URI(JAR, jar.toUri().toString(), null);
        Map<String, String> env = Maps.newHashMap();
        env.put("create", "false");
        FileSystem fileSystem = FileSystems.newFileSystem(zipURI, env);
        final Path sourceFolder = fileSystem.getPath(FOLDERNAME);
        Files.walkFileTree(sourceFolder, replicator);

        fileSystem.close();
    }

    @Override
    protected void handleJar() throws URISyntaxException, IOException {
        File jar = new File(".").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".jar");
            }
        })[0];
        handleJar(Paths.get(jar.toURI()));
    }

    @Override
    protected void handleNonJar() throws IOException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Path source = new File(loader.getResource(FOLDERNAME).getFile()).toPath();
        Files.walkFileTree(source, replicator);
    }

    private static class InFolderReplicator extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes basicFileAttributes) throws IOException {
            Matcher matcher;
            boolean isJar = file.toUri().getScheme().equals(JAR);
            String unixPath = FilenameUtils.separatorsToUnix(file.toString());
            if (isJar) {
                matcher = Pattern.compile("^(.*)").matcher(unixPath);
            } else {
                matcher = Pattern.compile("^.*/target/classes(/.*)").matcher(unixPath);
            }
            matcher.find();
            Path target = Paths.get(".", matcher.group(1));
            target.getParent().toFile().mkdirs();
            // Do not overwrite the config.json file.
            if (unixPath.endsWith(configFile) && target.toFile().exists()) {
                return FileVisitResult.CONTINUE;
            }
            log.debug("{} -> {}", file.toUri(), target);
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }

}

