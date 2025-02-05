# Application for parsing csv files with questions to moodle xml format for further import.
developed by: 1ikill

## Manual:
- Load .csv file with questions in the resources/questions directory
- Change "inputCsvFile" variable in Main class to questions/<yourFileName.csv>
- Change "outputXmlName" variable in Main class with desired name of output .xml file.
- Change "outputDirectory" variable in Main class with desired directory of the output file
- Run main class

After completing manual steps the file with chosen name should appear in chosen directory and ready for import into moodle quiz.

The example of Exel table to save as .csv file: https://docs.google.com/spreadsheets/d/1yq6e7mnAV2ynSdY5YVCdzvMeN73Tj10aDipKgMyRC4w/edit?usp=sharing

Mandatory columns: "Question", "description", "type", "value", "isCorrect".

## Note:
\<pre>\<code>\</code>\</pre> tags in description column cells are mandatory for code inserting.

The current version of parser supports only single choice and multiple choice questions.