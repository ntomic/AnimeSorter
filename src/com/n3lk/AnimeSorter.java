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
            FileHandler fh = new FileHandler(logProperty + "AnimeSorter.log", 1000000, 10, true);
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
            LOGGER.info("***AnimeSorter Log Init***");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Moves files beginning with "[HorribleSubs]" and ending with ".mkv" to other directory
    private void moveFiles() throws IOException {
        // Getting properties from config.properties file
        String sourceDirectory = properties.getProperty("sourceProperty");
        String targetDirectory = properties.getProperty("targetProperty");
        String pattern = properties.getProperty("patternProperty");  // A pattern to match over each file

        /* Opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
         * The entries returned by the iterator are filtered by matching the String representation of their file names against the given globbing pattern.
         * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
         */
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourceDirectory), pattern)) {
            for (Path entry : stream) {
                String tempInput = entry.getFileName().toString();  // getFileName() returns highest folder name in directory hierarchy
                // tempInput = "[HorribleSubs] Hunter x Hunter - 138 [720p]" => shortFileName = "Hunter x Hunter"
                String shortFileName = tempInput.substring(tempInput.indexOf(']') + 2, tempInput.lastIndexOf('-') - 1);
                File destinationFolder = new File(targetDirectory + shortFileName);

                Path destinationPath = destinationFolder.toPath().resolve(entry.getFileName()); // resolve() function combines two paths
                try {
                    if (destinationFolder.mkdir()) LOGGER.info("Created Folder " + destinationFolder);
                    // Path move(Path source, Path target, CopyOption option)
                    // https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#move-java.nio.file.Path-java.nio.file.Path-java.nio.file.CopyOption
                    move(entry, destinationPath, REPLACE_EXISTING);
                    LOGGER.info(tempInput + " MOVED TO " + destinationFolder);
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