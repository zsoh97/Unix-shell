package sg.edu.nus.comp.cs4218.impl.util;

import java.util.List;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandBuilderTest {
    // generated by square test
    @Test
    void testParseCommand() throws Exception {
        // Setup
        final ApplicationRunner appRunner = new ApplicationRunner();

        // Run the test
        final Command result = CommandBuilder.parseCommand("commandString", appRunner);

        // Verify the results
    }

    @Test
    public void testParseCommand2() throws ShellException {
        List<String> argsList = ((CallCommand) CommandBuilder.parseCommand("Command String", new ApplicationRunner()))
                .getArgsList();
        assertEquals(2, argsList.size());
        assertEquals("Command", argsList.get(0));
        assertEquals("String", argsList.get(1));
    }

    @Test
    public void testParseCommand3() throws ShellException {
        assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand("([^'\"`|<>;\\s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+",
                        new ApplicationRunner()));
    }

    @Test
    public void testParseCommand4() throws ShellException {
        assertThrows(ShellException.class, () -> CommandBuilder.parseCommand("", new ApplicationRunner()));
    }
}