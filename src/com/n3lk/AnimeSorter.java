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
     private static final Logger LOGGER = Logger.getLogger( AnimeSorter.class.getName() );
     private Properties properties = new Properties();
     private final String propertiesFile = "config.properties";

     // Load Properites from config.properties
     private void loadProperties() throws IOException {
         InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
         properties.load(resourceStream);
         resourceStream.close();
     }

     // Constructor initializes: Logger - Handler - Formatter
     AnimeSorter()
     {

         //Location of log file

         try {
             loadProperties();
         } catch (IOException e) {
             e.printStackTrace();
         }

         String logProperty = properties.getProperty("logProperty");

         try {
            /** @see <a href="http://docs.oracle.com/javase/7/docs/api/java/util/logging/FileHandler.html">FileHandler</a> for more on FileHandler function
            FileHandler(String pattern,int limit,int count, boolean append)
            *param pattern the pattern for naming the output file
            *param limit the maximum number of bytes to write to any one file
            *param count the number of files to use
            *param append specifies append mode
            **/
            // Create handler, give location for the log file
            FileHandler fh = new FileHandler(logProperty,1000000,10,true);
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
     *  @throws IOException If an input or output exception occurred
     *
     **/
       void moveFiles() throws IOException {

         //Getting source/target properties from config.properties
         String sourceProperty = properties.getProperty("sourceProperty");
         String targetProperty = properties.getProperty("targetProperty");

         // Creating Path objects
         Path sourceDirectory = Paths.get(sourceProperty.toString());
         Path targetDirectory = Paths.get(targetProperty.toString());

         //A pattern to match over each file (must be "\\" instead of "/")
        String pattern = "**\\[HorribleSubs]*.mkv";
        /**
         * Opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
         * The elements returned by the directory stream's iterator are of type Path, each one representing an entry in the directory.
         * The entries returned by the iterator are filtered by matching the String representation of their file names against the given globbing pattern.
         * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
         * **/
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory, pattern)) {

            for (Path entry: stream) {
                // LOGGER.info("File Transfered: " + entry.getFileName().toString());
                String input = entry.getFileName().toString();
                File destFolder = new File( targetDirectory.toString() + '/' + entry.getFileName().toString().substring(input.indexOf(']')+2,input.lastIndexOf('-')-1));

                Path destPath = destFolder.toPath().resolve(entry.getFileName());

                try {
                    if(destFolder.mkdir()) {
                       // LOGGER.info("New Directory Created");
                    } else {
                       // LOGGER.info("New Directory Not Created");
                    }
                   // LOGGER.info("File Move Destination: " + destPath.toString());
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
        try {
        AnimeSorter sorterObject = new AnimeSorter();
        sorterObject.moveFiles();

        } catch (Exception ex) {
            LOGGER.log( Level.SEVERE, "Exception: " + ex.toString(), ex );
            ex.printStackTrace();
        }
        //LOGGER.info("***AnimeSorter Process Finished***");
    }



 }