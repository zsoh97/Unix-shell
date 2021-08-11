package sg.edu.nus.comp.cs4218.impl.tdd_regression.ef1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.app.CdApplication;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;

class CdApplicationTest {

    @TempDir
    static File tempDir;

    private static CdApplication cdApplication;

    static final String ORIGINAL_DIR = Environment.currentDirectory;
    static final String FOLDER = "folder";
    static final String SUBFOLDER = "folder" + StringUtils.CHAR_FILE_SEP + "subfolder";
    static final String BLOCKED_FOLDER = "blocked";

    static final String VALID_FILE = "file.txt";


    @BeforeAll
    static void setupAll() throws IOException {
        new File(tempDir, FOLDER).mkdir();
        new File(tempDir, SUBFOLDER).mkdir();
        new File(tempDir, VALID_FILE).createNewFile();
        File blockedFolder = new File(tempDir, BLOCKED_FOLDER);
        blockedFolder.mkdir();
        blockedFolder.setExecutable(false);
    }
    
    @AfterAll
    static void tearDownAll() {
        Environment.setCurrentDirectory(System.getProperty("user.dir"));
    }

    @BeforeEach
    void setUp() {
        cdApplication = new CdApplication();
        Environment.setCurrentDirectory(tempDir.getAbsolutePath());
    }

    // Cd into valid relative path
    @Test
    public void run_CdIntoValidRelativePath_Success() throws CdException {
        String finalPath = tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + FOLDER;
        String[] argList = new String[]{FOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = Environment.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_CdIntoNestedFolder_Success() throws CdException {
        String finalPath = tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + SUBFOLDER;
        String[] argList = new String[]{SUBFOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = Environment.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_CdOutFromFolder_Success() throws CdException {
        String relativePath = tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + FOLDER;
        Environment.setCurrentDirectory(relativePath);
        String[] argList = new String[]{"../"};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = Environment.getCurrentDirectory();
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    @Test
    public void run_CdOutFromNestedFolder_Success() throws CdException {
        String relativePath = tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + SUBFOLDER;
        Environment.setCurrentDirectory(relativePath);
        String[] argList = new String[]{"../../"};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = Environment.getCurrentDirectory();
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    // Cd into invalid relative path
    @Test
    public void run_InvalidRelativePath_ThrowsException() {
        String[] argList = new String[]{"invalid"};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_FILE_NOT_FOUND).getMessage(), expectedException.getMessage());
    }

    // Cd into valid absolute path
    @Test
    public void run_CdIntoValidAbsolutePath_Success() throws CdException {
        String absolutePath = tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + FOLDER;
        String[] argList = new String[]{absolutePath};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = Environment.getCurrentDirectory();
        assertEquals(absolutePath, currDirectory);
    }

    // Cd into invalid absolute path
    @Test
    public void run_CdIntoInvalidAbsolutePath_ThrowsException() throws CdException {
        String absolutePath = tempDir.getAbsolutePath() + StringUtils.CHAR_FILE_SEP + "invalid";
        String[] argList = new String[]{absolutePath};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_FILE_NOT_FOUND).getMessage(), expectedException.getMessage());
    }

    // Cd into non directory
    @Test
    public void run_CdIntoFile_ThrowsException() {
        String[] argList = new String[]{VALID_FILE};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_IS_NOT_DIR).getMessage(), expectedException.getMessage());
    }

    // Cd with too many args
    @Test
    public void run_CdWithManyArgs_ThrowsCdException() {
        String[] argList = new String[]{FOLDER, SUBFOLDER};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_TOO_MANY_ARGS).getMessage(), expectedException.getMessage());
    }

    // Cd with blank arg
    @Test
    public void run_CdIntoBlankPath_ThrowsCdException() throws CdException {
        String[] argList = new String[]{""};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_NO_ARGS).getMessage(), expectedException.getMessage());
    }

    // Cd with null args
    @Test
    public void run_CdWithNullArgs_ThrowsCdException() {
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(null, System.in, System.out);
        });
        assertEquals(new CdException(ERR_NULL_ARGS).getMessage(), expectedException.getMessage());
    }

    // Cd with null input stream
    @Test
    public void run_CdWithNullInputStream_ThrowsCdException() {
        String[] argList = new String[]{};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, null, System.out);
        });
        assertEquals(new CdException(ERR_NO_ISTREAM).getMessage(), expectedException.getMessage());
    }

    // Cd with null output stream
    @Test
    public void run_CdWithNullOutputStream_ThrowsCdException() {
        String[] argList = new String[]{};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, null);
        });
        assertEquals(new CdException(ERR_NO_OSTREAM).getMessage(), expectedException.getMessage());
    }
}
