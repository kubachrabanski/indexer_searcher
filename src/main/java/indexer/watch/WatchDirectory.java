package indexer.watch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.*;

public final class WatchDirectory {

    private static final Logger logger = LogManager.getLogger(WatchDirectory.class);

    private final WatchService watchService;
    private final Map<WatchKey, Path> watchKeys;

    public WatchDirectory() throws IOException {

        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchKeys = new HashMap<>();

        logger.debug("Initialized directory watcher");

    }

    private void register(final Path path) throws IOException {

        WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        watchKeys.put(key, path);

    }

    public void registerAll(final Path path) throws IOException {

        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {

                register(directory);
                logger.info(format("Registered directory: %s", directory));
                return FileVisitResult.CONTINUE;

            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exception) {

                logger.warn(format("Failed to register directory: %s, caused by: %s",
                        file, exception.getClass()));
                return FileVisitResult.SKIP_SUBTREE;

            }

        });

    }

    public void run(WatchHandler handler) throws IOException {

        logger.info("Started directory watcher");

        try {
            while (!watchKeys.isEmpty()) {

                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {

                    WatchEvent.Kind<?> kind = event.kind();

                    Path path = watchKeys.get(key).resolve((Path) event.context());

                    logger.debug(format("Event: %s, at: %s", kind, path));

                    handler.handle(path, kind);

                }

                if (!key.reset()) {
                    logger.info(format("Unregistered directory: %s", watchKeys.get(key)));
                    watchKeys.remove(key);
                }
            }

            logger.error("No directory is registered");
        }
        catch (InterruptedException exception) {
            logger.error(format("Directory watcher was interrupted, caused by: %s", exception.getClass()));
        }

        logger.info("Stopped directory watcher");

    }

}
