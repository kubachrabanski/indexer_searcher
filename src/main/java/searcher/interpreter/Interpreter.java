package searcher.interpreter;

import config.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import searcher.Searches;
import searcher.result.SearchResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class Interpreter implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(Interpreter.class);

    private static final Pattern propertyPattern = Pattern.compile("%{1}[a-z]+\\s{1}[a-z0-9]+");
    private static final Pattern searchPattern = Pattern.compile("%{1}[a-z]+");
    private static final Pattern integerPattern = Pattern.compile("0|[1-9]{1}[0-9]*");

    private final Terminal terminal;
    private final LineReader reader;

    private final Searches searches;
    private final Properties properties;

    public Interpreter(Path path, Properties properties) throws IOException {

        this.terminal = TerminalBuilder.builder()
                .jna(false)
                .jansi(true)
                .build();

        /*Completer c1 = new TreeCompleter(
                node("%term"),
                node("%phrase"),
                node("%fuzzy"),
                node("%color", node("on", "off")),
                node("%details", node("on", "off")),
                node("%lang", node("pl", "en")),
                node("%limit"));*/

        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        
        this.searches = new Searches(path, properties);
        this.properties = properties;

        logger.debug(format("Initialized interpreter with: %s", terminal.toString()));

    }
    
    public Interpreter(Path path) throws IOException {
        this(path, Properties.getDefault());    
    }
    
    private void search(String query) throws IOException {

        List<SearchResult> results = searches
                .forType(properties.getSearchType())
                .run(query);

        terminal.writer().println(format("File count: %d", results.size()));

        results.forEach(terminal.writer()::println);
    }
    
    private void eval(String command) {

        logger.debug(format("Parsed: [%s]", command));
        properties.setSearchType(command);

    }
    
    private void eval(String command, String argument) {

        logger.debug(format("Parsed: [%s], with [%s]", command, argument));

        switch (command) {
            case "lang" :
                properties.setSearchLanguage(argument); break;
            case "details" :
                switch (argument) {
                    case "on" : properties.setContextDetails(true); break;
                    case "off" : properties.setContextDetails(false); break;
                }
                break;
            case "limit" :
                if (integerPattern.matcher(argument).matches())
                    properties.setSearchLimit(Integer.parseInt(argument));
                break;
            case "color" :
                switch (argument) {
                    case "on" : properties.setContextColor("red"); break;
                    case "off" : properties.setContextColor("default"); break;
                }
                break;
        }
    }
    
    public void run() throws IOException {

        while (true) {

            String line = reader.readLine("> ").trim();

            if (propertyPattern.matcher(line).matches()) {
                String[] lineParts = line.substring(1).split("\\s");
                eval(lineParts[0], lineParts[1]);
            } else {
                if (searchPattern.matcher(line).matches()) {
                    eval(line.substring(1));
                } else {
                    search(line);
                }
            }

        }

    }
    
    @Override
    public void close() throws IOException {
        terminal.close();
        logger.debug("Stopped interpreter");
    }

}
