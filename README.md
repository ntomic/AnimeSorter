### AnimeSorter
##### Description

AnimeSorter script moves files that match a provided text pattern from a provided source directory to a provided target directory.
It creates a new folder in the target directory for the groups of files moved based on their name (provided such folders don't already exist). 
For all the files moved and folders created, alongside with possible error messages, there will be a log entry written to a log file ```AnimeSorter.log```

For example, if a source folder contains files: 
```
[HorribleSubs] Hunter x Hunter - 130 [720p].mkv
[HorribleSubs] Hunter x Hunter - 131 [720p].mkv
[HorribleSubs] One Piece - 130 [720p].mkv
[HorribleSubs] Gintama - 100 [720p].mkv
```
All files will be moved to target directory. First two to the same folder named "Hunter x Hunter", 
and last two each to their own folder, "One Piece" and "Gintama".
This assumes the given pattern  ```*[HorribleSubs]*.mkv```  which matches all files that contain the [HorribleSubs] expression and are ending with .mkv

For more information, see the [What Is a Glob lesson](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob).

To specify the source directory, target directory, log file directory 
and a pattern to be matched, the ```config.properties``` file must be edited. 

#####Instructions

TODO

The file: ```config.properties``` initially contains the following four statements:
```
sourceProperty=C:/testing/source/
targetProperty=C:/testing/target/
logProperty=C:/testing/log/
patternProperty=*[HorribleSubs]*.mkv
```

Where ```sourceProperty``` contains the source directory path of the files, 
```targetProperty``` contains target directory path, 
```logProperty``` contains directory path where the log file will be created, and
```patternProperty``` contains the glob pattern to match against files in the source directory.

Change these properties by replacing the text after the '=' operator.



