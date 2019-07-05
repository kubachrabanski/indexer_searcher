package searcher.result;

import config.Configuration;
import org.apache.lucene.document.Document;

import static java.lang.String.format;

public class ContextSearchResult extends SearchResult {

    private final String context;

    public ContextSearchResult(Document document, String context) {
        super(document);
        this.context = context;
    }

    @Override
    public String toString() {
        return format("%s%s\n%s",
                Configuration.Colors.getCode("bold"),
                super.toString(), context);
    }
}
