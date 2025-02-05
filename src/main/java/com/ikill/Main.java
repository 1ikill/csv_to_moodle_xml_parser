package com.ikill;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.ikill.parser.CsvToXmlParser.parseData;

public class Main {
    private static final Logger logger = LogManager.getLogger();
    private static final String STARTUP_MESSAGE = """
            
            ___________CSV to Moodle XML Parser App___________
            _______________developed by 1ikill________________
            """;

    public static void main(String[] args) {
        final String inputCsvFile = "questions/questions.csv";
        final String outputXmlFile = "quiz_parsed.xml";
        final String outputDirectory = "example/path/to/output/directory";
        logger.info(STARTUP_MESSAGE);

        parseData(inputCsvFile, outputXmlFile, outputDirectory);
    }
}