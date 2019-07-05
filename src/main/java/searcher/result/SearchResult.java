package searcher.result;

import config.Configuration;
import org.apache.lucene.document.Document;

import static java.lang.String.format;

public class SearchResult {

    private final String path;

    public SearchResult(Document document) {
        this.path = document.get("real_path");
    }

    @Override
    public String toString() {
        return format("%s%s%s",
                Configuration.Colors.getCode("bold"), path,
                Configuration.Colors.getCode("default"));
    }
}
