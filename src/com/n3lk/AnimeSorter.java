package com.n3lk;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.*;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

 class AnimeSorter {
     private static final Logger LOGGER = Logger.getLogger( AnimeSorter.class.getName() );

     // Constructor initializes: Logger - Handler - Formatter
     AnimeSorter(String logDirectory) {
        try {
            /** @see <a href="http://docs.oracle.com/javase/7/docs/api/java/util/logging/FileHandler.html">FileHandler</a> for more on FileHandler function
            FileHandler(String pattern,int limit,int count, boolean append)
            @param pattern the pattern for naming the output file
            @param limit the maximum number of bytes to write to any one file
            @param count the number of files to use
            @param append specifies append mode
            **/
            // Create handler, give location for the log file
            FileHandler fh = new FileHandler(logDirectory,1000000,10,true);
            // Setting up the log Formatter. SimpleFormatter() can be used instead of overriding.
            // https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(record.getMillis());
                    return  logTime.format(cal.getTime()) + " "  //gets formatted DateTime
                            + record.getLevel() + " "  //gets level of log entry: info, fine,..., severe
                            + record.getSourceClassName().substring( record.getSourceClassName().lastIndexOf(".")+1, record.getSourceClassName().length()) //gets formatted class name
                            + "."
                            + record.getSourceMethodName() //gets method name that called logger
                            + "() : "
                            + record.getMessage() + "\n"; //gets message
                    }
                });
         //add handler to logger
         LOGGER.addHandler(fh);
         LOGGER.info("***AnimeSorter Log Init***");
        } catch (IOException ex) {
            ex.printStackTrace();
         }
      }


     /**
     *  Moves files that begin with "[HorribleSubs]" and end with ".mkv"
     *  @param sourceDirectory The source directory of the files
     *  @param targetDirectory The target directory of the files
     *  @throws IOException If an input or output exception occurred
     *
     **/
     void moveFiles(Path sourceDirectory, Path targetDirectory) throws IOException {

        //A pattern to match over each file (must be "\\" instead of "/")
        String pattern = "**\\[HorribleSubs]*.mkv";
        /**
         * Opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
         * The elements returned by the directory stream's iterator are of type Path, each one representing an entry in the directory.
         * The entries returned by the iterator are filtered by matching the String representation of their file names against the given globbing pattern.
         *  @see <a href="https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob">What is glob</a> for more on glob pattern matching
         *  @param sourceDirectory Directory over which to iterate
         *  @param pattern A glob pattern to match over each file
         *  @return a new and open DirectoryStream object
         *  @throws IOException  Runtime exception thrown if an I/O error is encountered when iterating over the entries in a directory.
         *
         * **/
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory, pattern)) {

            for (Path entry: stream) {
                LOGGER.info("File Transfered: " + entry.getFileName().toString());
                String input = entry.getFileName().toString();
                File destFolder = new File( targetDirectory.toString() + '/' + entry.getFileName().toString().substring(input.indexOf(']')+2,input.lastIndexOf('-')-1));

                Path destPath = destFolder.toPath().resolve(entry.getFileName());

                try {
                    if(destFolder.mkdir()) {
                        LOGGER.info("New Directory Created");
                    } else {
                        LOGGER.info("New Directory Not Created");
                    }
                    LOGGER.info("File Move Destination: " + destPath.toString());
                    move(entry,destPath, REPLACE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encountered during the files iteration. Retrieved as an IOException using the getCause() method.
            LOGGER.log(Level.SEVERE, ex.getMessage(),ex );
            throw ex.getCause();

        }

    }

    public static void main(String[] args) {
        //Location of source directory, target directory
        Path sourceDirectory = Paths.get("C:/Users/n3lk/testing/source/");
        Path targetDirectory = Paths.get("C:/Users/n3lk/testing/target/");
        //Location of log file
        String logDirectory = "C:/Users/n3lk/IdeaProjects/AnimeSorter/log/AnimeSorter.log";

        AnimeSorter sorterObject = new AnimeSorter(logDirectory);
        try {
            sorterObject.moveFiles(sourceDirectory,targetDirectory);
        } catch (Exception ex) {
            LOGGER.log( Level.SEVERE, "Exception: " + ex.toString(), ex );
            ex.printStackTrace();
        }
        LOGGER.info("***AnimeSorter Process Finished***");
    }

}