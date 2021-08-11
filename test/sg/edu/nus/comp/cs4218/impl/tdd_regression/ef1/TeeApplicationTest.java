package sg.edu.nus.comp.cs4218.impl.tdd_regression.ef1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.app.TeeApplication;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

class TeeApplicationTest {
    @TempDir
    static File tempDir;

    private static TeeApplication teeApplication;
    private static InputStream stdin;
    private static OutputStream stdout;
    private static PrintStream standardOut;
    static final String ORIGINAL_DIR = Environment.currentDirectory;
    public static final String ILLEGAL_FLAG_MSG = "illegal option -- ";

    static final String STDIN_STRING = "Hello world!" + StringUtils.STRING_NEWLINE
            + "Welcome to CS4218!" + StringUtils.STRING_NEWLINE;
    static final String TEE_ERR_FORMAT = "tee: %s: %s" + StringUtils.STRING_NEWLINE;
    public static final String TEE_ONE = "tee1.txt";
    public static final String UNWRITABLE_FILE = "unwritable.txt";
    public static final String EXISTING_FILE = "existing.txt";

    @BeforeEach
    void setUp() throws IOException {
        teeApplication = new TeeApplication();
        stdin = new ByteArrayInputStream(STDIN_STRING.getBytes(StandardCharsets.UTF_8));
        standardOut = System.out;
        stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        Environment.setCurrentDirectory(tempDir.getAbsolutePath());
        File existing = new File(tempDir, EXISTING_FILE);
        FileWriter fileWriter = new FileWriter(existing, false);
        File existing2 = new File(tempDir, "existing2.txt");
        FileWriter fileWriter2 = new FileWriter(existing2, false);
        try {
            fileWriter.write("Hello World" + StringUtils.STRING_NEWLINE);
            fileWriter2.write("Hello CS4218" + StringUtils.STRING_NEWLINE);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            fileWriter.close();
            fileWriter2.close();
        }
        File unwritable = new File(tempDir, UNWRITABLE_FILE);
        unwritable.createNewFile();
        unwritable.setReadOnly();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
    }

    @AfterAll
    static void tearDownAll() {
        Environment.setCurrentDirectory(System.getProperty("user.dir"));
    }

    // tee with single file: write to stdout and file
    @Test
    public void run_SingleFileNoAppend_WriteToFileAndStdout() throws AbstractApplicationException, TeeException, IOException {
        String[] argList = new String[]{TEE_ONE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, TEE_ONE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee with absolute path: write to stdout and file
    @Test
    public void run_SingleFileAbsolutePath_WriteToFileAndStdout() throws AbstractApplicationException, TeeException, IOException {
        String[] argList = new String[]{tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + TEE_ONE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, TEE_ONE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee with multiple files: write to stdout and files
    @Test
    public void run_MultipleFileNoAppend_WriteToFileAndStdout() throws AbstractApplicationException, TeeException, IOException {
        String[] argList = new String[]{TEE_ONE, "tee2.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, TEE_ONE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
        File outputFile2 = new File(tempDir, "tee2.txt");
        assertTrue(outputFile2.exists());
        String fileContents2 = Files.readString(Paths.get(outputFile2.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents2);
    }

    // tee: write to stdout
    @Test
    public void run_NoFile_WriteToStdout() throws AbstractApplicationException, TeeException {
        String[] argList = new String[]{};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
    }

    // tee with unwritable file: throws exception
    @Test
    public void run_UnwritableFile_PrintsNoPermAndWritesToStdout() throws AbstractApplicationException, TeeException {
        String[] argList = new String[]{UNWRITABLE_FILE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(String.format(TEE_ERR_FORMAT, UNWRITABLE_FILE, ERR_NO_PERM) + STDIN_STRING, stdout.toString());
    }

    // tee with mixture of valid and invalid cases
    @Test
    public void run_WritableAndUnWritableFile_PrintsNoPermAndWritesToStdoutAndFile() throws AbstractApplicationException, TeeException, IOException {
        String[] argList = new String[]{UNWRITABLE_FILE, TEE_ONE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(String.format(TEE_ERR_FORMAT, UNWRITABLE_FILE, ERR_NO_PERM) + STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, TEE_ONE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee -a with single file: write to stdout and append to file
    @Test
    public void run_SingleFileAppend_WriteToFileAndStdout() throws AbstractApplicationException, TeeException, IOException {
        String[] argList = new String[]{"-a", EXISTING_FILE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, EXISTING_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello World" + StringUtils.STRING_NEWLINE + STDIN_STRING, fileContents);
    }

    // tee -a with multiple files: write to stdout and append to file
    @Test
    public void run_MultipleFileAppend_WriteToFileAndStdout() throws AbstractApplicationException, TeeException, IOException {
        String[] argList = new String[]{"-a", EXISTING_FILE, "existing2.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, EXISTING_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello World" + StringUtils.STRING_NEWLINE + STDIN_STRING, fileContents);

        File outputFile2 = new File(tempDir, "existing2.txt");
        assertTrue(outputFile2.exists());
        String fileContents2 = Files.readString(Paths.get(outputFile2.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello CS4218" + StringUtils.STRING_NEWLINE + STDIN_STRING, fileContents2);
    }

    // tee -a: write to stdout
    @Test
    public void run_NoFileWithFlag_WriteToStdout() throws AbstractApplicationException, TeeException {
        String[] argList = new String[]{"-a"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
    }


    // tee with invalid flag: throws exception
    @Test
    public void run_InvalidArgs_ThrowsTeeException() {
        String[] argList = new String[]{"-d", TEE_ONE};
        Exception expectedException = assertThrows(TeeException.class, () -> {
            teeApplication.run(argList, stdin, stdout);
        });
        assertEquals(new TeeException(ILLEGAL_FLAG_MSG + "d").getMessage(), expectedException.getMessage());
    }

    // null streams: throws exception
    @Test
    public void run_StdinIsNull_ThrowsTeeException() {
        Exception expectedException = assertThrows(TeeException.class, () -> {
            teeApplication.run(new String[0], null, stdout);
        });
        assertEquals(new TeeException(ERR_NO_ISTREAM).getMessage(), expectedException.getMessage());
    }

    @Test
    public void run_StdoutIsNull_ThrowsTeeException() {
        Exception expectedException = assertThrows(TeeException.class, () -> {
            teeApplication.run(new String[0], System.in, null);
        });
        assertEquals(new TeeException(ERR_NO_OSTREAM).getMessage(), expectedException.getMessage());
    }

}
