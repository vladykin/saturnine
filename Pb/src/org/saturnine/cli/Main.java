package org.saturnine.cli;

import java.util.Arrays;
import org.saturnine.api.PbException;

/**
 * @author Alexey Vladykin
 */
public class Main {

    private Main() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        PbCommandProvider commandProvider = PbCommandProvider.getInstance();

        if (args.length == 0) {
            System.err.println("Usage:  pb command args");
            System.err.println();
            for (PbCommand command : commandProvider.getCommands()) {
                System.err.printf("%10s  %s\n", command.getName(), command.getDescription());
            }
            System.exit(1);
        }

        String commandName = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        for (PbCommand command : commandProvider.getCommands()) {
            if (command.getName().equals(commandName)) {
                try {
                    command.execute(commandArgs);
                    return;
                } catch (PbException ex) {
                    ex.printStackTrace();
                    System.exit(2);
                }
            }
        }

        System.err.println("Unknown command " + commandName);
        System.exit(1);
    }
}
