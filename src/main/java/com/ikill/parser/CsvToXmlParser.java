package com.ikill.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CsvToXmlParser {
    private static final Logger logger = LogManager.getLogger();

    // Constants for XML formatting
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String QUIZ_OPEN = "<quiz>\n";
    private static final String QUIZ_CLOSE = "</quiz>\n";
    private static final String QUESTION_OPEN = "  <question type=\"multichoice\">\n";
    private static final String QUESTION_CLOSE = "  </question>\n";
    private static final String NAME_OPEN = "    <name>\n";
    private static final String NAME_CLOSE = "    </name>\n";
    private static final String QUESTION_TEXT_OPEN = "    <questiontext format=\"html\">\n";
    private static final String QUESTION_TEXT_CLOSE = "    </questiontext>\n";
    private static final String SINGLE_OPEN = "    <single>";
    private static final String SINGLE_CLOSE = "</single>\n";
    private static final String ANSWER_OPEN = "    <answer fraction=\"";
    private static final String ANSWER_CLOSE = "    </answer>\n";

    public static void parseData(String inputFile, String outputFileName, String outputDirectory) {
        logger.info("Starting the parsing of {} to {}", inputFile, outputFileName);

        try (Reader reader = new FileReader(getFile(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(getFile(outputFileName, outputDirectory)))) {

            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
            );
            List<CSVRecord> records = csvParser.getRecords();

            writer.write(XML_HEADER);
            writer.write(QUIZ_OPEN);

            int questionCounter = 1;
            for (int i = 0; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                if (isRowEmpty(record)) continue;

                logger.info("Processing Question {}", questionCounter);

                // Collect all rows belonging to this question
                int newIndex = writeQuestion(writer, record, questionCounter, records, i);

                questionCounter++;
                i = newIndex; // Update index to avoid duplicate processing
            }

            writer.write(QUIZ_CLOSE);
            logger.info("XML conversion completed successfully. Output written to {}", outputDirectory);

        } catch (URISyntaxException | IOException e) {
            logger.error("Error occurred while processing the CSV file: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static int writeQuestion(BufferedWriter writer, CSVRecord record, int questionNumber, List<CSVRecord> records, int index) throws IOException {
        writer.write(QUESTION_OPEN);
        writer.write(NAME_OPEN);
        writer.write("      <text>Question " + questionNumber + "</text>\n");
        writer.write(NAME_CLOSE);

        String questionText = record.get("Question") + "\n" + record.get("description");
        writer.write(QUESTION_TEXT_OPEN);
        writer.write("      <text><![CDATA[" + questionText + "]]></text>\n");
        writer.write(QUESTION_TEXT_CLOSE);

        boolean isSingle = record.get("type").equals("SINGLE");
        writer.write(SINGLE_OPEN + isSingle + SINGLE_CLOSE);

        // Collect all rows belonging to this question
        List<CSVRecord> questionRows = new ArrayList<>();
        while (index < records.size() && !isRowEmpty(records.get(index))) {
            questionRows.add(records.get(index));
            index++;
        }
        index--; // Adjust index for outer loop

        long correctAnswerCount = questionRows.stream()
                .filter(row -> row.get("isCorrect").equalsIgnoreCase("TRUE"))
                .count();

        for (CSVRecord row : questionRows) {
            writeAnswer(writer, row, isSingle, correctAnswerCount);
        }

        writer.write(QUESTION_CLOSE);

        return index; // Return updated index to avoid duplicate processing
    }

    private static void writeAnswer(BufferedWriter writer, CSVRecord row, boolean isSingle, long correctAnswerCount) throws IOException {
        String value = row.get("value");
        String isCorrect = row.get("isCorrect");

        if (!value.isEmpty() && !isCorrect.isEmpty()) {
            int fraction = isCorrect.equalsIgnoreCase("TRUE")
                    ? (isSingle ? 100 : (int) (100.0 / correctAnswerCount))
                    : -100;

            writer.write(ANSWER_OPEN + fraction + "\">\n");
            writer.write("      <text><![CDATA[" + value + "]]></text>\n");
            writer.write(ANSWER_CLOSE);
        }
    }

    private static File getFile(String path) throws URISyntaxException {
        ClassLoader classLoader = CsvToXmlParser.class.getClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            logger.error("File not found: {}", path);
            throw new IllegalArgumentException("File not found: " + path);
        }

        return new File(resource.toURI());
    }

    private static File getFile(String fileName, String directory) {
        File outputFile = new File(directory, fileName);

        if (!outputFile.getParentFile().exists()) {
            boolean dirsCreated = outputFile.getParentFile().mkdirs();
            if (!dirsCreated) {
                logger.warn("Failed to create directory: {}", directory);
            }
        }

        return outputFile;
    }

    private static boolean isRowEmpty(CSVRecord record) {
        return record.stream().allMatch(value -> value == null || value.trim().isEmpty());
    }
}



