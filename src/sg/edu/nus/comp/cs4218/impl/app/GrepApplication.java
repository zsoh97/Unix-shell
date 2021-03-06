package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.convertToAbsolutePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.IS_DIRECTORY;

public class GrepApplication implements GrepInterface {
    public static final String INVALID_PATTERN = "Invalid pattern syntax";
    public static final String EMPTY_PATTERN = "Pattern should not be empty.";
    public static final String NULL_POINTER = "Null Pointer Exception";

    private static final int NUM_ARGUMENTS = 3;
    private static final char CASE_INSEN_IDENT = 'i';
    private static final char COUNT_IDENT = 'c';
    private static final char PREFIX_FN = 'H';
    private static final int CASE_INSEN_IDX = 0;
    private static final int COUNT_INDEX = 1;
    private static final int PREFIX_FN_IDX = 2;
    private static final String STDIN_FILEPREFIX = "(standard input): ";


    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws Exception {
        // TODO: To implement -H flag print file name with output lines
        if (fileNames == null || pattern == null) {
            throw new GrepException(NULL_POINTER);
        }

        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);

        grepResultsFromFiles(pattern, isCaseInsensitive, isPrefixFileName, lineResults, countResults, fileNames);

        String results = "";
        if (isCountLines) {
            results = countResults.toString() + STRING_NEWLINE;
        } else {
            if (!lineResults.toString().isEmpty()) {
                results = lineResults.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    private Pattern getCompiledPattern(String pattern, boolean isCaseInsensitive) {
        Pattern compiledPattern;
        if (isCaseInsensitive) {
            compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            compiledPattern = Pattern.compile(pattern);
        }
        return compiledPattern;
    }

    private void grepResultsSingleFile(boolean isFilePrefixName, StringJoiner lineResults, String filename, String line) {
        if (isFilePrefixName) {
            lineResults.add(filename + ": " + line);
        } else {
            lineResults.add(line);
        }
    }

    /**
     * Extract the lines and count number of lines for grep from files and insert them into
     * lineResults and countResults respectively.
     *
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param isFilePrefixName  supplied by user
     * @param lineResults       a StringJoiner of the grep line results
     * @param countResults      a StringJoiner of the grep line count results
     * @param fileNames         a String Array of file names supplied by user
     */
    private void grepResultsFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isFilePrefixName, StringJoiner lineResults, StringJoiner countResults, String... fileNames) throws Exception {
        int count;
        boolean isSingleFile = (fileNames.length == 1);
        for (String f : fileNames) {
            BufferedReader reader = null;
            try {
                String path = convertToAbsolutePath(f);
                File file = new File(path);
                if (!file.exists()) {
                    lineResults.add(f + ": " + ERR_FILE_NOT_FOUND);
                    countResults.add(f + ": " + ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (file.isDirectory()) { // ignore if it's a directory
                    lineResults.add(f + ": " + IS_DIRECTORY);
                    countResults.add(f + ": " + IS_DIRECTORY);
                    continue;
                }
                reader = new BufferedReader(new FileReader(path));
                String line;
                Pattern compiledPattern = getCompiledPattern(pattern, isCaseInsensitive);
                count = 0;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = compiledPattern.matcher(line);
                    if (matcher.find()) { // match
                        if (isSingleFile) {
                            grepResultsSingleFile(isFilePrefixName, lineResults, f, line);
                        } else {
                            lineResults.add(f + ": " + line);
                        }
                        count++;
                    }
                }
                if (isSingleFile && !isFilePrefixName) {
                    countResults.add("" + count);
                } else {
                    countResults.add(f + ": " + count);
                }
                reader.close();
            } catch (PatternSyntaxException pse) {
                throw new GrepException(ERR_INVALID_REGEX);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }

    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws Exception {
        // TODO: To implement -H flag print file name with output lines
        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line;
            Pattern compiledPattern;
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }
            while ((line = reader.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) { // match
                    if (isPrefixFileName) {
                        stringJoiner.add(STDIN_FILEPREFIX + line);
                    } else {
                        stringJoiner.add(line);
                    }
                    count++;
                }
            }
            reader.close();
        } catch (PatternSyntaxException pse) {
            throw new GrepException(ERR_INVALID_REGEX);
        } catch (NullPointerException npe) {
            throw new GrepException(ERR_FILE_NOT_FOUND);
        } catch (IOException ioe) {
            throw new IOException(ERR_STREAM_CLOSED);
        }

        String results = "";
        if (isCountLines) {
            if (isPrefixFileName) {
                results = STDIN_FILEPREFIX + count + STRING_NEWLINE;
            } else {
                results = count + STRING_NEWLINE;
            }
        } else {
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        String result = "";
        try {
            GrepArguments grepArgs = new GrepArguments();
            grepArgs.parse(args);
            boolean[] grepFlags = new boolean[]{grepArgs.isCaseInsensitive(), grepArgs.isCountOfLinesOnly(), grepArgs.isPrefixFileName()};
            List<String> inputFiles = grepArgs.getFiles();
            String pattern = grepArgs.getPattern();
            if (stdin == null && inputFiles.isEmpty()) {
                throw new Exception(ERR_NO_INPUT);
            }

            if (stdin == null) {
                throw new Exception(ERR_NO_ISTREAM);
            }

            if (stdout == null) {
                throw new Exception(ERR_NO_OSTREAM);
            }

            if (pattern == null) {
                throw new Exception(ERR_SYNTAX);
            }

            if (pattern.isEmpty()) {
                throw new Exception(EMPTY_PATTERN);
            } else {
                if (inputFiles.isEmpty()) {
                    result = grepFromStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin);
                } else if (inputFiles.contains("-")) {
                    String[] inputFilesArray = new String[inputFiles.size()];
                    inputFilesArray = inputFiles.toArray(inputFilesArray);
                    result = grepFromFileAndStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin, inputFilesArray);
                } else {
                    String[] inputFilesArray = new String[inputFiles.size()];
                    inputFilesArray = inputFiles.toArray(inputFilesArray);
                    result = grepFromFiles(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], inputFilesArray);
                }
            }
            stdout.write(result.getBytes());
            if (!stdout.equals(System.out)) {
                stdout.close();
            }
        } catch (GrepException grepException) {
            throw grepException;
        } catch (Exception e) {
            throw new GrepException(e.getMessage());
        }
    }

    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws Exception {
        // TODO: To implement
        ArrayList<String> queuedFileNames = new ArrayList<>();
        StringJoiner stringJoiner = new StringJoiner("");
        for (String file : fileNames) {
            if ((file.trim().equals("-"))) {
                if (!queuedFileNames.isEmpty()) {
                    stringJoiner.add(grepFromFiles(pattern, isCaseInsensitive, isCountLines, fileNames.length > 1, queuedFileNames.toArray(new String[0])));
                }
                queuedFileNames = new ArrayList<>();
                stringJoiner.add(grepFromStdin(pattern, isCaseInsensitive, isCountLines, fileNames.length > 1, stdin));
            } else {
                queuedFileNames.add(file);
            }
        }
        if (!queuedFileNames.isEmpty()) {
            stringJoiner.add(grepFromFiles(pattern, isCaseInsensitive, isCountLines, fileNames.length > 1, queuedFileNames.toArray(new String[0])));
        }

        String results = "";
        if (!stringJoiner.toString().isEmpty()) {
            results = stringJoiner.toString();
        }
        return results;
    }
}
