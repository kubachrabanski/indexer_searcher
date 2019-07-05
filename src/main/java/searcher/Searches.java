package searcher;

import config.Configuration;
import config.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.FSDirectory;
import searcher.search.Search;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public final class Searches implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(Searches.class);

    private final IndexSearcher searcher;

    private final Map<String, Search> searches;
    private final Map<String, Analyzer> analyzers;

    private final Map<String, Map<String, UnifiedHighlighter>> highlighters;


    public Searches(Path path, Properties properties) throws IOException {

        this.searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(path)));

        this.searches = Configuration.Searches.getSearches(this, properties);
        this.analyzers = Configuration.Languages.getAnalyzers();

        this.highlighters = new HashMap<>();

        logger.debug(format("Opened index searcher at: %s", path));

    }

    public Analyzer getAnalyzer(String language) {
        return analyzers.get(language);
    }

    public UnifiedHighlighter getHighlighter(String language, String color) {

        if (highlighters.containsKey(color)) {
            if (highlighters.get(color).containsKey(language)) {
                return highlighters.get(color).get(language);
            }
        } else {
            highlighters.put(color, new HashMap<>());
        }

        UnifiedHighlighter highlighter = new UnifiedHighlighter(searcher, getAnalyzer(language));
        highlighter.setFormatter(new DefaultPassageFormatter(
                Configuration.Colors.getCode(color), Configuration.Colors.getCode("default"),
                "... ", false
        ));

        highlighters.get(color).put(language, highlighter);

        return highlighter;

    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    public Search forType(String type) {
        return searches.get(type);
    }

    @Override
    public void close() throws IOException {
        searcher.getIndexReader().close();
        logger.debug("Closed index searcher");
    }
}
