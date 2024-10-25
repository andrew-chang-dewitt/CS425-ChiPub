package org.iitcs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iitcs.cli.Cli;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String... args) {
        Cli cli = new Cli(args);
        int exitCode = cli.run();
        System.exit(exitCode);
    }
}
