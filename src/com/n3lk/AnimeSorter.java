package com.n3lk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.*;

import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class AnimeSorter {
    private static final Logger LOGGER = Logger.getLogger(AnimeSorter.class.getName());
    private Properties properties = new Properties();
    private final String propertiesFile = "config.properties";

    // Load Properites from config.properties file
    private void loadProperties() throws IOException {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
        properties.load(resourceStream);
        resourceStream.close();
    }

    // Constructor initializes: Properties - Logger - Handler - Formatter
    private AnimeSorter() {
        try {
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String logProperty = properties.getProperty("logProperty");

        try {
            /* FileHandler(String pattern,int limit,int count, boolean append): http://docs.oracle.com/javase/7/docs/api/java/util/logging/FileHandler.html
             **/
            // Create handler, give location for the log file
            FileHandler fh = new FileHandler(logProperty, 1000000, 10, true);
            // Setting up the log Formatter. SimpleFormatter() can be used instead of overriding.
            // https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(record.getMillis());
                    return logTime.format(cal.getTime()) + " "  //gets formatted DateTime
                            + record.getLevel() + " "  //gets level of log entry: info, fine,..., severe
                            + record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf(".") + 1, record.getSourceClassName().length()) //gets formatted class name
                            + "."
                            + record.getSourceMethodName() //gets method name that called logger
                            + "() : "
                            + record.getMessage() + "\n"; //gets message
                }
            });
            //add handler to logger
            LOGGER.addHandler(fh);
            // LOGGER.info("***AnimeSorter Log Init***");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Moves files beginning with "[HorribleSubs]" and ending with ".mkv" to other directory
    private void moveFiles() throws IOException {
        // Getting source/target properties from config.properties file
        String sourceProperty = properties.getProperty("sourceProperty");
        String targetProperty = properties.getProperty("targetProperty");

        // Creating Path objects from String
        Path sourceDirectory = Paths.get(sourceProperty);
        Path targetDirectory = Paths.get(targetProperty);

        //A pattern to match over each file (must be "\\" instead of "/")
        String pattern = "**\\[HorribleSubs]*.mkv";

        /* Opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
         * The entries returned by the iterator are filtered by matching the String representation of their file names against the given globbing pattern.
         * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
         * Can also be done with regex!
         **/
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory, pattern)) {
            for (Path entry : stream) {
                String input = entry.getFileName().toString();
                File destinationFolder = new File(targetDirectory.toString() + '/' + entry.getFileName().toString().substring(input.indexOf(']') + 2, input.lastIndexOf('-') - 1));
                // resolve() function combines paths
                Path destinationPath = destinationFolder.toPath().resolve(entry.getFileName());
                try {
                    if (destinationFolder.mkdir()) //LOGGER.info("Created Folder");
                        // Path move (Path source, Path target, CopyOption option)
                        // https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#move-java.nio.file.Path-java.nio.file.Path-java.nio.file.CopyOption
                        // REPLACE_EXISTING: If the target file exists, then the target file is replaced
                        move(entry, destinationPath, REPLACE_EXISTING);
                    // LOGGER.info("File Move Destination: " + destPath.toString());
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        } catch (DirectoryIteratorException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex.getCause();
        }
    }

    public static void main(String[] args) {
        try {
            AnimeSorter sorterObject = new AnimeSorter();
            sorterObject.moveFiles();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception: " + ex.toString(), ex);
            ex.printStackTrace();
        }
        //LOGGER.info("***AnimeSorter Process Finished***");
    }

}