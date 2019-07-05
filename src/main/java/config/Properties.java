package config;

public final class Properties {

    public static Properties getDefault() {
        return new Properties(
                new Search("term", "en", 5),
                new Context(false, "default", 5)
        );
    }

    private final Search search;
    private final Context context;

    private Properties(Search search, Context context) {
        this.search = search;
        this.context = context;
    }

    public String getSearchType() {
        return search.type;
    }

    public void setSearchType(String type) {
        if (config.Configuration.Searches.isSupported(type)) {
            search.type = type;
        }
    }

    public String getSearchLanguage() {
        return search.language;
    }

    public void setSearchLanguage(String language) {
        if (config.Configuration.Languages.isSupported(language)) {
            search.language = language;
        }
    }

    public int getSearchLimit() {
        return search.limit;
    }

    public void setSearchLimit(int limit) {
        search.limit = limit > 0 ? limit : Integer.MAX_VALUE;
    }

    public boolean getContextDetails() {
        return context.details;
    }

    public void setContextDetails(boolean details) {
        context.details = details;
    }

    public String getContextColor() {
        return context.color;
    }

    public void setContextColor(String color) {
        if (config.Configuration.Colors.isSupported(color)) {
            context.color = color;
        }
    }

    public int getContextPassage() {
        return context.passage;
    }

    public void setContextPassage(int passage) {
        context.passage = passage > 0 ? passage : Integer.MAX_VALUE;
    }

    private static final class Search {

        private String type;
        private String language;
        private int limit;

        private Search(String type, String language, int limit) {
            this.type = type;
            this.language = language;
            this.limit = limit;
        }

    }

    private static final class Context {

        private boolean details;
        private String color;
        private int passage;

        private Context(boolean details, String color, int passage) {
            this.details = details;
            this.color = color;
            this.passage = passage;
        }

    }

}
