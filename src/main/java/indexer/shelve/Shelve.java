package indexer.shelve;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class Shelve<E extends Serializable> implements AutoCloseable {

    private Collection<E> shelf;
    private Path location;

    @SuppressWarnings("unchecked")
    public Shelve(Path location, Class<? extends Collection> clazz) throws IOException {

        if(Files.notExists(location)) {
            try {
                this.shelf = clazz.getConstructor().newInstance();
            }
            catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException exception) {
                throw new IOException(exception);
            }
        } else {
            try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(location.toFile()))) {
                this.shelf = clazz.cast(stream.readObject());
            }
            catch (ClassNotFoundException | ClassCastException exception) {
                throw new IOException(exception);
            }
        }

        this.location = location;
    }

    public Collection<E> getShelf() {
        return shelf;
    }

    public void sync() throws IOException {
        try (ObjectOutputStream stream = new ObjectOutputStream(
                new FileOutputStream(location.toFile(), false))){
            stream.writeObject(shelf);
        }
    }

    @Override
    public void close() throws IOException {
        sync();
        shelf.clear();
    }

}
