package org.saturnine;

import java.util.Arrays;
import org.saturnine.commands.AddCommand;
import org.saturnine.commands.CloneCommand;
import org.saturnine.commands.CommitCommand;
import org.saturnine.commands.InCommand;
import org.saturnine.commands.InitCommand;
import org.saturnine.commands.LogCommand;
import org.saturnine.commands.MoveCommand;
import org.saturnine.commands.OutCommand;
import org.saturnine.commands.PullCommand;
import org.saturnine.commands.PushCommand;
import org.saturnine.commands.RemoveCommand;
import org.saturnine.commands.StatusCommand;

/**
 *
 * @author Alexey Vladykin
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: ...");
            System.exit(1);
        }

        try {
            String command = args[0];
            String[] restArgs = Arrays.copyOfRange(args, 1, args.length);
            if ("init".equals(command)) {
                new InitCommand().execute(restArgs);
            } else if ("clone".equals(command)) {
                new CloneCommand().execute(restArgs);
            } else if ("status".equals(command)) {
                new StatusCommand().execute(restArgs);
            } else if ("commit".equals(command)) {
                new CommitCommand().execute(restArgs);
            } else if ("pull".equals(command)) {
                new PullCommand().execute(restArgs);
            } else if ("push".equals(command)) {
                new PushCommand().execute(restArgs);
            } else if ("rm".equals(command)) {
                new RemoveCommand().execute(restArgs);
            } else if ("mv".equals(command)) {
                new MoveCommand().execute(restArgs);
            } else if ("add".equals(command)) {
                new AddCommand().execute(restArgs);
            } else if ("in".equals(command)) {
                new InCommand().execute(restArgs);
            } else if ("out".equals(command)) {
                new OutCommand().execute(restArgs);
            } else if ("log".equals(command)) {
                new LogCommand().execute(restArgs);
            } else {
                System.out.println("Unknown command: " + command);
                System.exit(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
