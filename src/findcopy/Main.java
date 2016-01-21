package findcopy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gP
 *
 * find and copy files in source directory and copy them to dest directory
 *
 * usage: FindCopy pattern source directory destination directory
 *
 * description: find recursively all files that match specified pattern and copy
 * them to destination directory
 *
 * example: FindCopy "*.bak" testdir/directory1 testdir/directory2
 *
 */
public class Main {
    private final Logger log;
    private final String pattern;
    private final Path source, dest;

    public Main(String[] args) {
        this.pattern = args[0];
        this.source = Paths.get(args[1]);
        this.dest = Paths.get(args[2]);
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    static void usage() {
        System.err.println("java Find <pattern> <source> <dest>");
        System.exit(1);
    }

    private void doIt() {
        try {
            log.info("application started successfully");

            // abort, if destination path already exists
            if (Files.exists(dest)) {
                log.debug("{} already existing, aborting...", dest);
                System.err.println(dest + " already existing, aborting...");
                System.exit(1);
            }

            // start copying file with pattern from source directory to destination directory
            FindCopy fc = new FindCopy(pattern, source, dest);

            Path walkFileTree;
            // walk through the source file tree
            // for every directory methods preVisitDirectory(..) and postVisitDirectory(..) will be executed
            // for every file method visitFile(..) will be executed
            walkFileTree = Files.walkFileTree(source, fc);
            log.info("walked through source tree: {} and copied {} files with pattern {}", walkFileTree, fc.getNumMatches(), pattern);
            
            // walk through the destinatation tree and delete all empty directories
            EmptyDirectory ed;
            ed = new EmptyDirectory();
            walkFileTree = Files.walkFileTree(dest, ed);
            log.info("walked through destination tree {} and and removed {} empty directories", walkFileTree, ed.getNum());
            
            log.info("application terminated successfully");
        } catch (Exception ex) {
            String result = ex.toString() + "\n";
            StackTraceElement[] trace = ex.getStackTrace();
            for (StackTraceElement trace1 : trace) {
                result += trace1.toString() + "\n";
            }     
            log.error("{}", result);
        }
    }

    public static void main(String[] args) {
        // there must be 3 arguments: pattern, source directory and destination directory
        if (args.length < 3) {
            usage();
        }

        Main m = new Main(args);
        m.doIt();
    }
}
