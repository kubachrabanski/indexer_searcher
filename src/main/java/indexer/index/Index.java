package indexer.index;

import config.Configuration;
import indexer.parse.Contents;
import indexer.parse.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.detect.LanguageResult;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode;

public final class Index implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(Index.class);

    private final IndexWriter writer;

    public Index(Path path, OpenMode mode) throws IOException {

        PerFieldAnalyzerWrapper fieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(
                new StandardAnalyzer(), Configuration.Languages.getAnalyzers().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> format("contents_%s", entry.getKey()),
                                Map.Entry::getValue)));

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(fieldAnalyzerWrapper);
        indexWriterConfig.setOpenMode(mode);


        this.writer = new IndexWriter(FSDirectory.open(path), indexWriterConfig);

        logger.debug(format("Opened index writer at: %s", path));

    }

    public Index(Path path) throws IOException {
        this(path, OpenMode.CREATE_OR_APPEND);
    }

    private Document getDocument(Path path, String contents, LanguageResult result) {

        Document document = new Document();

        Field pathField = new StringField(
                "real_path", path.toString(), Field.Store.YES);

        Field contentsField = new TextField(
                format("contents_%s", result.getLanguage()),
                contents.replaceAll("[\n\r]+", "\n"),
                Field.Store.YES);

        document.add(pathField);
        document.add(contentsField);

        return document;

    }

    public void addDocument(Path path) throws IOException {

        try {

            Contents contents = Contents.parse(path);
            writer.addDocument(getDocument(path, contents.getContents(), contents.getLanguageResult()));
            writer.commit();
            logger.info(format("Added: %s, %s, %s", path, contents.getType(), contents.getLanguageResult()));

        }
        catch (TikaException | ParseException exception) {
            logger.warn(format("Failed to parse: %s, caused by: %s, reason: %s",
                    path, exception.getClass(), exception.getMessage()));
        }

    }

    public void addDocuments(Path path) throws IOException {

        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {

                if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                    addDocument(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exception) {

                if (Files.isRegularFile(file)) {
                    logger.warn(format("Failed to access: %s, caused by: %s", file, exception.getClass()));
                }

                return FileVisitResult.SKIP_SUBTREE;
            }
        });

    }

    public void updateDocument(Path path) throws IOException {

        try {

            Contents contents = Contents.parse(path);
            writer.updateDocument(new Term("real_path", path.toString()),
                    getDocument(path, contents.getContents(), contents.getLanguageResult()));
            writer.commit();
            logger.info(format("Updated: %s, %s, %s", path, contents.getType(), contents.getLanguageResult()));

        }
        catch (TikaException | ParseException exception) {
            logger.warn(format("Failed to parse: %s, caused by: %s, reason: %s",
                    path, exception.getClass(), exception.getMessage()));
        }

    }

    private void deleteDocuments(Query query) throws IOException {
        writer.deleteDocuments(query);
        writer.commit();
    }

    public void deleteDocuments(Path path) throws IOException {
        deleteDocuments(new PrefixQuery(new Term("real_path", path.toString())));
        logger.info(format("Deleted document(s) from: %s", path));
    }

    public void purge() throws IOException {
        writer.deleteAll();
        writer.commit();
        logger.debug("Purged index writer");
    }

    @Override
    public void close() throws IOException {
        writer.close();
        logger.debug("Closed index writer");
    }
}
