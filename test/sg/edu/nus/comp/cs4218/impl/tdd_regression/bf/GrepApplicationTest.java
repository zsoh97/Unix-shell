package sg.edu.nus.comp.cs4218.impl.tdd_regression.bf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class GrepApplicationTest {
    private static final String TEMP = "temp-grep";
    public static final Path TEMP_PATH = Paths.get(Environment.currentDirectory, TEMP);
    public static final byte[] BYTES_A = "First line\nSecond line\nThird line\nFourth line\n".getBytes();
    public static final byte[] BYTES_B = "Fifth line\nSixth line\nSeventh line\nEighth line\n".getBytes();
    public static Deque<Path> files = new ArrayDeque<>();

    @BeforeAll
    static void createTemp() throws IOException {
        Files.createDirectory(TEMP_PATH);
    }

    @AfterAll
    static void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(TEMP_PATH);
    }

    private Path createFile(String name) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private Path createDirectory(String folder) throws IOException {
        Path path = TEMP_PATH.resolve(folder);
        Files.createDirectory(path);
        files.push(path);
        return path;
    }

    private String[] toArgs(String flags, String pattern, String... files) {
        List<String> args = new ArrayList<>();
        if (!flags.isEmpty()) {
            args.add("-" + flags);
        }
        args.add(pattern);
        for (String file : files) {
            if ("-".equals(file)) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    private Path getRelativePath(String name) {
        return Paths.get(TEMP, name);
    }

    @Test
    void run_SingleFile_PrintsLine() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("a.txt"), BYTES_A);
        new GrepApplication().run(toArgs("", "th", "a.txt"), System.in, output);
        assertArrayEquals(("Fourth line" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileRegex_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("b.txt"), BYTES_A);
        new GrepApplication().run(toArgs("", "[th] l", "b.txt"), System.in, output);
        assertArrayEquals(("First line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFiles_PrintsLinesWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("c.txt"), BYTES_A);
        Files.write(createFile("d.txt"), BYTES_B);
        new GrepApplication().run(toArgs("", "Fi", "c.txt", "d.txt"), System.in, output);
        String expected = getRelativePath("c.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("d.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFilePrintName_PrintsLinesWithName() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("e.txt"), BYTES_A);
        Path path = getRelativePath("e.txt");
        new GrepApplication().run(toArgs("H", "F", "e.txt"), System.in, output);
        String expected = path + ": First line" + STRING_NEWLINE + path + ": Fourth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesPrintName_PrintsLinesWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("f.txt"), BYTES_A);
        Files.write(createFile("g.txt"), BYTES_B);
        new GrepApplication().run(toArgs("H", "Fi", "f.txt", "g.txt"), System.in, output);
        String expected = getRelativePath("f.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("g.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCaseInsensitive_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("h.txt"), BYTES_A);
        new GrepApplication().run(toArgs("i", "th", "h.txt"), System.in, output);
        String expected = "Third line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCountLines_PrintsCount() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("l.txt"), BYTES_A);
        new GrepApplication().run(toArgs("c", "th", "l.txt"), System.in, output);
        assertArrayEquals(("1" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesCountLines_PrintsCountWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("m.txt"), BYTES_A);
        Files.write(createFile("n.txt"), BYTES_B);
        new GrepApplication().run(toArgs("c", "th", "m.txt", "n.txt"), System.in, output);
        String expected = getRelativePath("m.txt") + ": 1" + STRING_NEWLINE +
                getRelativePath("n.txt") + ": 4" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_NoFiles_PrintsLinesFromStdin() throws AbstractApplicationException {
        String inputString = "abc" + STRING_NEWLINE + "def" + STRING_NEWLINE + "ghi" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("", "[ci]"), input, output);
        assertArrayEquals(("abc" + STRING_NEWLINE + "ghi" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_Stdin_PrintsLinesFromStdin() throws AbstractApplicationException {
        String inputString = "jkl" + STRING_NEWLINE + "mno" + STRING_NEWLINE + "pqr" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("", "[lo]", "-"), input, output);
        assertArrayEquals(("jkl" + STRING_NEWLINE + "mno" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_StdinAndFile_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_A);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("aa.txt"), BYTES_B);
        new GrepApplication().run(toArgs("", "Fi", "-", "aa.txt"), input, output);
        String expected = "(standard input): First line" + STRING_NEWLINE +
                getRelativePath("aa.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_EmptyFile_PrintsNothing() throws AbstractApplicationException, IOException {
        createFile("x.txt");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("", "a", "x.txt"), System.in, output);
        assertArrayEquals(new byte[]{}, output.toByteArray());
    }

    @Test
    void run_NoMatch_PrintsNothing() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("y.txt"), BYTES_A);
        new GrepApplication().run(toArgs("", "a", "y.txt"), System.in, output);
        assertArrayEquals(new byte[]{}, output.toByteArray());
    }

    @Test
    void run_Directory_OutputsError() throws AbstractApplicationException, IOException {
        createDirectory("directory");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("", "a", "directory"), System.in, output);
        String expected = getRelativePath("directory") + ": Is a directory" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_NonexistentFile_OutputsError() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("", "a", "not exist"), System.in, output);
        String expected = getRelativePath("not exist") + ": No such file or directory" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_UnknownFlag_Throws() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("z.txt"), BYTES_A);
        String expectedOutput = "l: " + ERR_FILE_NOT_FOUND + STRING_NEWLINE;
        new GrepApplication().run(toArgs("x", "l", "z.txt"), System.in, output);
        assertArrayEquals(expectedOutput.getBytes(), output.toString().getBytes());
    }

    @Test
    void run_ZeroArguments_Throws() {
        assertThrows(GrepException.class, () -> new GrepApplication().run(new String[]{}, System.in, System.out));
    }

    @Test
    void run_FlagOnly_Throws() {
        assertThrows(GrepException.class, () -> new GrepApplication().run(new String[]{"-c"}, System.in, System.out));
    }
}
