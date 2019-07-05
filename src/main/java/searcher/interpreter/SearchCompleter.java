package searcher.interpreter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.FSDirectory;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.lang.String.format;

public class SearchCompleter implements Completer {

    private static final Logger logger = LogManager.getLogger(SearchCompleter.class);

    private final AnalyzingInfixSuggester suggester;

    public SearchCompleter(Path path, Analyzer analyzer) throws IOException {
        this.suggester = new AnalyzingInfixSuggester(FSDirectory.open(path), analyzer);

        logger.debug(format("Initialized search completer at: %s", path));
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word().substring(0, line.wordCursor());

        try {
            List<Lookup.LookupResult> results = suggester.lookup(
                    word, 5, false, false); // TEMPORARY!
            results.stream().map(result -> new Candidate(result.key.toString())).forEach(candidates::add);
        }
        catch (IOException exception) {
            logger.error(format("Failed to lookup search suggestions, caused by: %s", exception.getMessage()));
        }
    }
}
