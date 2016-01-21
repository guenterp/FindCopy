
package findcopy;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gP
 */
public class FindCopy extends SimpleFileVisitor<Path> {
    private final Logger log;
    private final String pattern;
    private final Path source, dest;

    CopyOption[] options;

    private final PathMatcher matcher;
    private int numMatches = 0;
        
    FindCopy(String pattern, Path source, Path dest) {
        this.pattern = pattern;
        this.source = source;
        this.dest = dest;
        this.options = new CopyOption[]{COPY_ATTRIBUTES};
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    // Compares the glob pattern against
    // the file or directory name.
    boolean find(Path file) {
        Path name = file.getFileName();
        boolean found = false;
        if (name != null && matcher.matches(name)) {
            log.debug("found file {}", file);
            found = true;
        }
        return (found);
    }

    protected int getNumMatches() {
        return (numMatches);
    }

    // Invoke the pattern matching method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) throws IOException {
        log.debug("visitFile: " + file);
        if (find(file)) {
            numMatches++;
            if (Files.notExists(dest.resolve(source.relativize(file)))) {
                Path copy = Files.copy(file, dest.resolve(source.relativize(file)), options);
                log.debug("visiting file: {}", copy);
            }
        }
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        log.debug("preVisitDirectory: {}", dir);
        find(dir);

        Path newdir;
        newdir = dest.resolve(source.relativize(dir));
        try {
            Files.copy(dir, newdir, options);
        } catch (FileAlreadyExistsException x) {
            // ignore
        } catch (IOException x) {
            System.err.format("Unable to create: %s: %s%n", newdir, x);
            return SKIP_SUBTREE;
        }
        return CONTINUE;
    }

    /**
     * postVisitDirectory
     *
     * is called when navigating to directory after processing files set access
     * time for subdirectory destination folder
     *
     * @param dir
     * @param exc
     * @return
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        // fix up modification time of directory when done
        if (exc == null) {
            Path newdir = dest.resolve(source.relativize(dir));
            try {
                FileTime time = Files.getLastModifiedTime(dir);
                Path setLastModifiedTime = Files.setLastModifiedTime(newdir, time);
                log.debug("set last modified time for {} to {}", newdir, setLastModifiedTime);
            } catch (IOException ex) {
                log.debug("Unable to copy all attributes to {}, exception: {}", newdir, ex.toString());
                
            }
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        if (exc instanceof FileSystemLoopException) {
            log.debug("cycle detected for file {}, exception: {}", file, exc);
        } else {
            log.debug("Unable to copy file {}, exception: {}", file, exc);
        }
        return CONTINUE;
    }
}
