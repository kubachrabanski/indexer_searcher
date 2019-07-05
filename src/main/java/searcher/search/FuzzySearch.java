package searcher.search;

import config.Properties;
import searcher.Searches;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;

import static java.lang.String.format;

public class FuzzySearch extends Search {

    public FuzzySearch(Searches searches, Properties properties) {
        super(searches, properties);
    }

    @Override
    protected Query getQuery(String query) {
        return new FuzzyQuery(new Term(format("contents_%s", properties.getSearchLanguage()), query));
    }
}
