package com.example.converter.Utils;

import javadbf.DBFReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileConverter {

    private static List<String> dbcFiles = new ArrayList<>();
    private static List<String> dbfFiles = new ArrayList<>();

    public static void convertFiles() {
        listDbcFiles();

        if (!dbcFiles.isEmpty()) {
            dbcToDbf();
            dbfToCsv();
        }
    }

    private static void listDbcFiles() {
        try {

            String dbcDirectoryPath = System.getProperty("user.dir") + File.separator + "dbc-files";

            File dbcDirectory = new File(dbcDirectoryPath);

            if (!dbcDirectory.exists()) {
                System.out.println("The DBC directory '" + dbcDirectoryPath + "' does not exist.");
                return;
            }

            File[] dbcFilesArray = dbcDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".dbc"));

            if (dbcFilesArray != null && dbcFilesArray.length > 0) {
                System.out.println("DBC Files in the directory '" + dbcDirectoryPath + "':");
                for (File dbcFile : dbcFilesArray) {
                    String fileName = dbcFile.getName();
                    System.out.println(fileName);
                    dbcFiles.add(fileName);
                }
            } else {
                System.out.println("No DBC files found in the directory '" + dbcDirectoryPath + "'.");
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void dbcToDbf() {
        for (String fileName : dbcFiles) {
            String dbfFileName = fileName.replace(".dbc", ".dbf");
            dbfFiles.add(dbfFileName);

            try {
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
                        "cd blast-dbf/; ./blast-dbf ../dbc-files/" + fileName + " ../dbc-files/" + dbfFileName);
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    System.out.println(fileName + ": File converted to DBF.");
                } else {
                    System.out.println(fileName + ": File conversion to DBF failed with exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



// ...

    private static void dbfToCsv() {
        for (String dbfFileName : dbfFiles) {
            String csvFilePath = Paths.get("dbc-files", Paths.get(dbfFileName).getFileName().toString().replace(".dbf", ".csv")).toString();

            try {
                CsvWriter csvWriter = new CsvWriter(csvFilePath);

                readDbfAndWriteToCsv("dbc-files" + File.separator + dbfFileName, csvWriter);

                System.out.println(dbfFileName + ": File converted to CSV.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readDbfAndWriteToCsv(String dbfFilePath, CsvWriter csvWriter) {
        try (DBFReader dbfReader = new DBFReader(new FileInputStream(dbfFilePath))) {

            int numFields = dbfReader.getFieldCount();
            String[] headers = new String[numFields];

            for (int i = 0; i < numFields; i++) {
                headers[i] = dbfReader.getField(i).getName();
            }

            csvWriter.writeRecord(headers);

            Object[] row;
            while ((row = dbfReader.nextRecord()) != null) {
                String[] record = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    record[i] = String.valueOf(row[i]);
                }
                csvWriter.writeRecord(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static class CsvWriter {
        private final Path csvFilePath;

        public CsvWriter(String csvFilePath) throws IOException {
            this.csvFilePath = Paths.get(csvFilePath);
            java.nio.file.Files.createDirectories(this.csvFilePath.getParent());
        }

        public void writeRecord(String[] record) throws IOException {
            java.nio.file.Files.write(csvFilePath, String.join(",", record).getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        }
    }
}
