package indexer.parse;

import config.Configuration;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

public class Contents {

    private static final OptimaizeLangDetector detector;
    private static final Tika tika;

    static {
        try {
            tika = new Tika(new TikaConfig(
                    Objects.requireNonNull(
                            ClassLoader.getSystemClassLoader()
                                    .getResource("tika-config.xml"))
            ));

            detector = new OptimaizeLangDetector();
            detector.loadModels(Configuration.Languages.getLanguages());
        }
        catch (IOException | TikaException | SAXException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Contents parse(Path path) throws ParseException, TikaException {

        try (TikaInputStream stream = TikaInputStream.get(path)) {

            String type = tika.detect(stream);

            if (!Configuration.Documents.isSupported(type)) {
                throw new ParseException("Type not supported");
            }

            String contents = tika.parseToString(stream);

            if (contents.isBlank()) {
                throw new ParseException("Contents are blank");
            }

            detector.addText(contents);
            LanguageResult result = detector.detect();
            detector.reset();

            if (result.isUnknown()) {
                throw new ParseException("Language is unknown");
            }

            return new Contents(type, contents, result);
        }
        catch (IOException | UncheckedIOException exception) {
            throw new ParseException(exception);
        }

    }

    private final String type;
    private final String contents;
    private final LanguageResult result;

    private Contents(String type, String contents, LanguageResult result) {
        this.type = type;
        this.contents = contents;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public String getContents() {
        return contents;
    }

    public LanguageResult getLanguageResult() {
        return result;
    }

}
