import config.Configuration;
import indexer.index.Index;
import indexer.shelve.Shelve;
import indexer.watch.WatchDirectory;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import searcher.interpreter.Interpreter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import static java.lang.String.format;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public final class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    static {
        if(Files.notExists(Configuration.getIndexPath())) {
            if (Configuration.getIndexPath().toFile().mkdir()) {
                logger.debug(format("Created directory for index: %s", Configuration.getIndexPath()));
            } else {
                logger.error(format("Failed to create directory for index: %s", Configuration.getIndexPath()));
            }
        }
    }

    private static void purge() throws IOException {
        try (Index index = new Index(Configuration.getIndexPath())) {
            index.purge();
        }
    }

    private static void list() throws IOException {
        try (Shelve<String> shelve = new Shelve<>(
                Configuration.getIndexPath().resolve("directories.shelve"),
                HashSet.class)) {
            shelve.getShelf().forEach(System.out::println);
        }
    }

    private static void reindex() throws IOException {
        try (Index index = new Index(Configuration.getIndexPath());
             Shelve<String> shelve = new Shelve<>(
                     Configuration.getIndexPath().resolve("directories.shelve"),
                     HashSet.class)) {

            index.purge();

            for (String pathString : shelve.getShelf()) {
                index.addDocuments(Paths.get(pathString));
            }

        }
    }

    private static void add(Path path) throws IOException {

        if (Files.isDirectory(path, NOFOLLOW_LINKS)) {

            Path realPath = path.toRealPath(NOFOLLOW_LINKS);

            try (Shelve<String> shelve = new Shelve<>(
                         Configuration.getIndexPath().resolve("directories.shelve"),
                         HashSet.class)) {

                if (!shelve.getShelf().contains(realPath.toString())) {

                    try (Index index = new Index(Configuration.getIndexPath())) {
                        index.addDocuments(realPath);
                    }

                    shelve.getShelf().add(realPath.toString());
                }

            }
        } else {
            logger.warn(format("Failed to add: %s, is not a directory, or does not exist", path));
        }
    }

    private static void remove(Path path) throws IOException {

        Path realPath = path.toRealPath(NOFOLLOW_LINKS);

        try (Shelve<String> shelve = new Shelve<>(
                     Configuration.getIndexPath().resolve("directories.shelve"),
                     HashSet.class)) {

            if (shelve.getShelf().contains(realPath.toString())) {

                try (Index index = new Index(Configuration.getIndexPath())) {
                    index.deleteDocuments(realPath);
                }

                shelve.getShelf().remove(realPath.toString());
            } else {
                logger.warn(format("Failed to remove: %s, is not on the list", realPath));
            }

        }

    }

    private static void watch() throws IOException {

        Index index = new Index(Configuration.getIndexPath());
        WatchDirectory watch = new WatchDirectory();
        Shelve<String> shelve = new Shelve<>(
                Configuration.getIndexPath().resolve("directories.shelve"),
                HashSet.class);

        for (String pathString : shelve.getShelf()) {
            watch.registerAll(Paths.get(pathString));
        }

        watch.run(((path, kind) -> {
            switch (kind.name()) {
                case "ENTRY_CREATE":
                    if (Files.isDirectory(path)) {
                        watch.registerAll(path);
                    }
                    index.addDocuments(path);
                    break;
                case "ENTRY_MODIFY":
                    if (Files.isRegularFile(path)) {
                        index.updateDocument(path);
                    }
                    break;
                case "ENTRY_DELETE":
                    index.deleteDocuments(path);
                    break;
            }
        }));

    }

    private static void search() throws IOException {

        try (Interpreter interpreter = new Interpreter(Configuration.getIndexPath())) {
            interpreter.run();

        }
        catch (UserInterruptException | EndOfFileException exception) {
            logger.debug(format("Terminated interpreter, caused by: %s, reason: %s",
                    exception.getClass(), exception.getMessage()));
        }

    }

    public static void main(String[] args) {

        Options options = new Options();

        Option search = Option.builder()
                .argName("search")
                .longOpt("search")
                .hasArg(false)
                .desc("Launches interpreter for searching")
                .build();

        options.addOption(search);

        Option add = Option.builder()
                .argName("add")
                .longOpt("add")
                .hasArg(true)
                .desc("Adds a directory to monitored list, indexes its contents")
                .build();

        options.addOption(add);

        Option remove = Option.builder()
                .argName("remove")
                .longOpt("rm")
                .hasArg(true)
                .desc("Removes a directory from monitored list, removes its contents from index")
                .build();

        options.addOption(remove);

        Option reindex = Option.builder()
                .argName("reindex")
                .longOpt("reindex")
                .hasArg(false)
                .desc("Purges and rebuilds the index")
                .build();

        options.addOption(reindex);

        Option purge = Option.builder()
                .argName("purge")
                .longOpt("purge")
                .hasArg(false)
                .desc("Purges the index")
                .build();

        options.addOption(purge);

        Option list = Option.builder()
                .argName("list")
                .longOpt("list")
                .hasArg(false)
                .desc("Prints monitored list")
                .build();

        options.addOption(list);

        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine commands = parser.parse(options, args);

            switch (commands.getOptions().length) {
                case 0 :
                    watch(); break;
                case 1 :
                    switch (commands.getOptions()[0].getArgName()) {
                        case "reindex" : reindex(); break;
                        case "purge" : purge(); break;
                        case "list" : list(); break;
                        case "search" : search(); break;

                        case "add" :
                            add(Paths.get(commands.getOptionValue("add")));
                            break;
                        case "remove" :
                            remove(Paths.get(commands.getOptionValue("rm")));
                            break;
                    }
                    break;

                default:
                    throw new ParseException("Too many options provided");
            }

        }
        catch (ParseException exception) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "indexer, searcher", "a tool set for indexing and searching through documents", options,
                    "Please report issues at: https://github.com/kubachrabanski/indexer_searcher/issues", true
            );

            logger.debug(format("Failed to parse commandline arguments, caused by: %s, reason: %s",
                    exception.getClass(), exception.getMessage()));
        }
        catch (IOException | UncheckedIOException exception) {
            logger.fatal(format("Fatal error, caused by: %s, reason: %s",
                    exception.getClass(), exception.getMessage()));
            System.exit(-1);
        }

    }

}
