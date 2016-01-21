package findcopy;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gP
 */
public class EmptyDirectory extends SimpleFileVisitor<Path> {

    private int num = 0;
    private final Logger log;

    public EmptyDirectory() {
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        try {
            log.debug("cleanup: deleting empty directory {}", dir);
            Files.delete(dir);
            num++;
        } catch (IOException ex) {
            // an IOexception thrown when attempting to remove a nonempty directory will be ignored
        }

        return CONTINUE;
    }

    protected int getNum() {
        return (num);
    }
}
