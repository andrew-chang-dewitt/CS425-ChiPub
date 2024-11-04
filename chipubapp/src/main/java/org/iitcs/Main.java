package org.iitcs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iitcs.cli.Cli;

import picocli.CommandLine;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new Cli());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
