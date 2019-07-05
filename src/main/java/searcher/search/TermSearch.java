package searcher.search;

import config.Properties;
import searcher.Searches;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import static java.lang.String.format;

public class TermSearch extends Search {

    public TermSearch(Searches searches, Properties properties) {
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
            return new TermQuery(new Term(format("contents_%s", properties.getSearchLanguage()), query));
        }
    }
}
