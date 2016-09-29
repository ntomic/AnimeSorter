package com.n3lk;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main {

    private static void moveFiles(Path sourceDirectory, Path targetDirectory) throws IOException {

        String pattern = "**\\[HorribleSubs]*.mkv";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory, pattern)) {
            for (Path entry: stream) {
                System.out.println(entry.getFileName());
                String input = entry.getFileName().toString();
                File destFolder = new File( targetDirectory.toString() + '\\' + entry.getFileName().toString().substring(input.indexOf(']')+2,input.lastIndexOf('-')-1));

                Path destPath = destFolder.toPath().resolve(entry.getFileName());

                try {
                    if(destFolder.mkdir()) {
                        System.out.println("Directory Created");
                    } else {
                        System.out.println("Directory is not created");
                    }
                    System.out.println(destPath);
                    move(entry,destPath, REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }

    }

    public static void main(String[] args) {
        Path sourceDirectory =  Paths.get("C:\\Users\\Nela\\0\\");
        Path targetDirectory = Paths.get("C:\\Users\\Nela\\1\\");

        try {
            moveFiles(sourceDirectory,targetDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}