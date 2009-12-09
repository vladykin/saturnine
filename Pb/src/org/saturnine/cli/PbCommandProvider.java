package org.saturnine.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.saturnine.cli.commands.*;

/**
 * @author Alexey Vladykin
 */
public class PbCommandProvider {

    private static final PbCommandProvider INSTANCE = new PbCommandProvider();

    public static PbCommandProvider getInstance() {
        return INSTANCE;
    }
    private final List<PbCommand> commands;

    public PbCommandProvider() {
        commands = Arrays.asList(
                new InitCommand(),
                new CloneCommand(),
                new StatusCommand(),
                new AddCommand(),
                new RemoveCommand(),
                new MoveCommand(),
                new CommitCommand(),
                new LogCommand(),
                new InCommand(),
                new OutCommand(),
                new PullCommand(),
                new PushCommand());
    }

    public List<PbCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
