package searcher.search;

import config.Properties;
import searcher.Searches;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

import java.util.Arrays;

import static java.lang.String.format;

public class PhraseSearch extends Search {

    public PhraseSearch(Searches searches, Properties properties) {
        super(searches, properties);
    }

    @Override
    protected Query getQuery(String query) {

        try {
            QueryParser parser = new QueryParser(format("contents_%s", properties.getSearchLanguage()),
                    searches.getAnalyzer(properties.getSearchLanguage()));

            return parser.parse(query);
        }
        catch (ParseException exception) {
            PhraseQuery.Builder builder = new PhraseQuery.Builder();
            Arrays.stream(query.split("\\s+")).forEach(term ->
                    builder.add(new Term(format("contents_%s", properties.getSearchLanguage()), term)));

            return builder.build();
        }

    }
}
