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

    private void loadProperties() throws IOException {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
        properties.load(resourceStream);
        resourceStream.close();
    }


    private AnimeSorter() {
        try {
            // Load Properites from config.properties file
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Logging initialization
        try {
            // Get properties for the log file
            String logProperty = properties.getProperty("logProperty");
            // Create FileHandler(location_of_log_ file + log_file_name, file_size_in_bytes, no_of_files_to_keep, bool_append)
            FileHandler fh = new FileHandler(logProperty + "AnimeSorter.log", 1000000, 10, true);
            // Setting up the log Formatter. SimpleFormatter() can be used instead of overriding.
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

    private void moveFiles() throws IOException {
        // Getting properties from config.properties file
        String sourceDirectory = properties.getProperty("sourceProperty");
        String targetDirectory = properties.getProperty("targetProperty");
        String pattern = properties.getProperty("patternProperty");

        // newDirectoryStream opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
        // The entries returned by the iterator are filtered by matching the String representation of their file names against the given pattern.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourceDirectory), pattern)) {
            for (Path entry : stream) {
                String tempInput = entry.getFileName().toString();  // getFileName() returns highest folder name in directory hierarchy
                // tempInput = "[HorribleSubs] Hunter x Hunter - 138 [720p]" => shortFileName = "Hunter x Hunter"
                String shortFileName = tempInput.substring(tempInput.indexOf(']') + 2, tempInput.lastIndexOf('-') - 1);
                File destinationFolder = new File(targetDirectory + shortFileName);

                Path destinationPath = destinationFolder.toPath().resolve(entry.getFileName()); // resolve() combines two paths
                try {
                    if (destinationFolder.mkdir()) LOGGER.info("Created Folder " + destinationFolder);
                    move(entry, destinationPath, REPLACE_EXISTING); // Path move(Path source, Path target, CopyOption option)
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