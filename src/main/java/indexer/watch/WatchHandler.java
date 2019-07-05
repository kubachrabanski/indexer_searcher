package indexer.watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;


public interface WatchHandler {

    void handle(Path path, WatchEvent.Kind<?> kind) throws IOException;

}
