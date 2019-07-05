package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import searcher.search.FuzzySearch;
import searcher.search.PhraseSearch;
import searcher.search.Search;
import searcher.search.TermSearch;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

public final class Configuration {

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    public static Path getIndexPath() {
        return Paths.get(System.getProperty("user.home")).resolve(Paths.get(".index"));
    }

    public static final class Documents {

        private static final Set<String> documentTypes = Set.of(
                "text/plain", "application/rtf", "application/pdf"
        );

        private static final Set<String> documentGroups = Set.of(
                "openxml", "opendocument"
        );

        static {

            logger.debug(format("Supported document types: %s", documentTypes));
            logger.debug(format("Supported document groups: %s", documentGroups));

        }

        public static boolean isSupported(String description) {
            return documentTypes.contains(description) ||
                    documentGroups.stream().anyMatch(description::contains);
        }

    }

    public static final class Searches {

        private static final Map<String, Class<? extends Search>> searchTypes = Map.of(
                "term", TermSearch.class,
                "phrase", PhraseSearch.class,
                "fuzzy", FuzzySearch.class
        );

        static {

            logger.debug(format("Supported search types: %s", searchTypes));

        }

        public static Map<String, Search> getSearches(searcher.Searches searches, Properties properties) {

            return searchTypes.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        try {
                            return entry.getValue()
                                    .getConstructor(searcher.Searches.class, config.Properties.class)
                                    .newInstance(searches, properties);
                        }
                        catch (NoSuchMethodException | InstantiationException |
                                IllegalAccessException | InvocationTargetException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
            ));

        }

        public static boolean isSupported(String type) {
            return searchTypes.containsKey(type);
        }

    }

    public static final class Languages {

        private static final Map<String, Class<? extends Analyzer>> languageAnalyzers = Map.of(
                "pl", PolishAnalyzer.class,
                "en", EnglishAnalyzer.class
        );

        static {

            logger.debug(format("Supported language analyzers: %s", languageAnalyzers));

        }

        public static Map<String, Analyzer> getAnalyzers() {

            return languageAnalyzers.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        try {
                            return entry.getValue().getConstructor().newInstance();
                        }
                        catch (NoSuchMethodException | InstantiationException |
                                IllegalAccessException | InvocationTargetException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
            ));

        }

        public static Set<String> getLanguages() {
            return languageAnalyzers.keySet();
        }

        public static boolean isSupported(String language) {
            return languageAnalyzers.containsKey(language);
        }

    }

    public static final class Colors {

        private static final Map<String, String> colorCodes = Map.of(
                "default", "\u001b[0m",
                "bold", "\u001B[1m",
                "red", "\u001b[31m"
        );

        public static String getCode(String color) {
            return colorCodes.get(color);
        }

        public static boolean isSupported(String color) {
            return colorCodes.containsKey(color);
        }

    }

}
