package com.n3lk;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import org.apache.commons.cli.*;


class AnimeSorter
{
	private static final Logger LOGGER = Logger.getLogger(AnimeSorter.class.getName());
	private Properties properties = new Properties();
	private final String propertiesFile = "config.properties";

	private void loadProperties() throws IOException
	{
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
		properties.load(resourceStream);
		resourceStream.close();
	}

	private static boolean tryMove(Path startPath, Path destinationPath) {
		try
		{
			move(startPath, destinationPath, REPLACE_EXISTING);
			LOGGER.info("Moved File " + startPath.getFileName());
			return true;
		}
		catch(IOException ex) {
			LOGGER.log(Level.SEVERE, "Exception: locked file \nException message: " + ex.getMessage());
			return false;
		}
	}

	private AnimeSorter()
	{
		try
		{
			// Load Properties from config.properties file
			loadProperties();
			// Get properties for the log file
			String logProperty = properties.getProperty("logProperty");

			// Create a Directory for the Log file
			Path logPath = Paths.get(logProperty);
			boolean logExisted = true;
			if(notExists(logPath))
			{
				Files.createDirectory(logPath);
				logExisted = false;

			}

			// Create FileHandler(location_of_log_ file + log_file_name, file_size_in_bytes, no_of_files_to_keep, bool_append)
			FileHandler fh = new FileHandler(logProperty + "AnimeSorter.log", 1000000, 10, true);
			// Setting up the log Formatter. SimpleFormatter() can be used instead of overriding.
			fh.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
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
			// Add handler to logger
			LOGGER.addHandler(fh);

			if(!logExisted)LOGGER.info("Created log folder: " + logPath );
			LOGGER.info("***AnimeSorter Log Init***");

		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private void moveFiles() throws IOException
	{
		// Getting properties from config.properties file
		String sourceDirectoryString = properties.getProperty("sourceProperty");
		String targetDirectoryString = properties.getProperty("targetProperty");
		String pattern = properties.getProperty("patternProperty");

		Path targetPath = Paths.get(targetDirectoryString);
		if(notExists(targetPath))
		{
			try
			{
				createDirectory(targetPath);
				LOGGER.info("Created Folder " + targetPath);
			} catch(IOException ex)
			{
				LOGGER.log(Level.SEVERE, "Exception: target DIR cannot be created " + ex.getMessage());
				throw ex;
			}
		}

		// The entries returned by the iterator are filtered by matching the String representation of their file names against the given pattern.
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourceDirectoryString), pattern))
		{
			for(Path entry : stream)
			{
				// fileName = "[HorribleSubs] Hunter x Hunter - 138 [720p]" => shortFileName = "Hunter x Hunter"
				String fileName = entry.getFileName().toString();
				String shortFileName = fileName.substring(fileName.indexOf(']') + 2, fileName.lastIndexOf('-') - 1);
				// Function resolve() makes a new path from a given path and a string
				Path folderDestinationPath = targetPath.resolve(shortFileName);
				Path fileDestinationPath = folderDestinationPath.resolve(fileName);
				try
				{
					if(notExists(folderDestinationPath))
					{
						createDirectories(folderDestinationPath);
						LOGGER.info("Created Folder " + folderDestinationPath);
					}
					if(!tryMove(entry, fileDestinationPath)) {
                        Files.copy(entry, fileDestinationPath, REPLACE_EXISTING);
                        Files.delete(entry);
					}

				} catch(IOException ex)
				{
					LOGGER.log(Level.SEVERE, "Exception:  " + ex.getMessage());
				}
			}
		} catch(IOException | DirectoryIteratorException ex)
		{
			LOGGER.log(Level.SEVERE, "Exception: " + ex.getMessage());
		}
	}

	public static void main(String[] args)
	{
		try
		{
			AnimeSorter sorterObject = new AnimeSorter();
			sorterObject.moveFiles();

		} catch(Exception ex)
		{
			LOGGER.log(Level.SEVERE, "Exception: " + ex.getMessage());
		}
		//LOGGER.info("***AnimeSorter Process Finished***");
	}

}