
environment: Netbeans, Java >= 7

program is called with 3 arguments:  <pattern> <source directory> <destination directory>

copy recursively files specified by pattern from source tree to destination tree.
all subdirectories are copied before processing files. 
empty subdirectories are deleted at the end.

logging is done with slf4j and log4j.
log file can be found in directory logdir.

example source tree in directory testdir/sourceDir 

files specified by pattern are copied to directory testdir/destDir
this directory has to be deleted before a new run


