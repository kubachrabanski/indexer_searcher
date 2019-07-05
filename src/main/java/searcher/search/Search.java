package searcher.search;

import config.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import searcher.Searches;
import searcher.result.ContextSearchResult;
import searcher.result.SearchResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public abstract class Search { // logging, reconsider

    private static final Logger logger = LogManager.getLogger(Search.class);

    protected final Searches searches;
    protected final Properties properties;

    public Search(Searches searches, Properties properties) {
        this.searches = searches;
        this.properties = properties;
    }

    protected abstract Query getQuery(String query);

    private Document[] getDocuments(TopDocs tops) throws IOException {

        Document[] documents = new Document[tops.scoreDocs.length];
        for (int i = 0; i < tops.scoreDocs.length; i++) {
            documents[i] = searches.getSearcher().doc(tops.scoreDocs[i].doc);
        }
        return documents;

    }

    private String[] getContexts(TopDocs tops, Query queries) throws IOException {

        return searches.getHighlighter(properties.getSearchLanguage(), properties.getContextColor())
                .highlight(format("contents_%s", properties.getSearchLanguage()),
                        queries, tops, properties.getContextPassage());

    }

    public List<SearchResult> run(String query) throws IOException {

        Query queries = getQuery(query);
        TopDocs tops = searches.getSearcher().search(queries, properties.getSearchLimit());
        System.out.println(properties.getSearchLimit());

        logger.debug(format("Search: %s, results: %s", queries, Arrays.toString(tops.scoreDocs)));

        Document[] documents = getDocuments(tops);

        if (properties.getContextDetails()) {

            String[] contexts = getContexts(tops, queries);
            return IntStream.range(0, documents.length).mapToObj(index ->
                    new ContextSearchResult(documents[index], contexts[index]))
                    .collect(Collectors.toList());

        } else {

            return Arrays.stream(documents).map(SearchResult::new)
                    .collect(Collectors.toList());

        }

    }

}
