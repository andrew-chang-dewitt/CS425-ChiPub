package org.iitcs.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// import org.apache.logging.log4j.Level;
// import org.apache.logging.log4j.core.config.Configurator;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import org.iitcs.util.PropertiesManager;

/**
 * Encapsulates everything needed to instantiate & execute the CLI program.
 */
public class Cli {
    private static PropertiesManager props = PropertiesManager.getInstance();

    private String[] args;
    private CommandLine cmd;

    /**
     * Create a CLI application from {@link ChiPub} w/ given arguments.
     *
     * Parses args for some configuration options, then waits for exectuion using Cli::run().
     */
    public Cli(String ...args) {
        // save args for execution time
        this.args = args;
        // create a new app using name, description, & version number from properties
        ChiPub chipub = new ChiPub(
            props.getName(),
            props.getDescription(),
            props.getVersion()
        );
        // build picocli command line from chipub root command
        // use custom execution strategy to initialize things before exec
        this.cmd = new CommandLine(chipub);
        // this.cmd.setExecutionStrategy(chipub::executionStrategy);
    }

    /**
     * Run the Cli application & receive an integer exit code where:
     * - 0 => OK
     * - 1 => Application error
     * - 2 => User error
     */
    public int run() {
        // execute command with args
        return cmd.execute(args);
    }
}

/**
 * Root command for entire CLI program. Everything starts here.
 */
@Command(
    resourceBundle = "default_properties",
    name = "${bundle:name}",
    description = "${bundle:description}",
    version = "${bundle:version}",
    mixinStandardHelpOptions = true,
    subcommands = {Search.class, View.class}
)
class ChiPub implements Runnable {
    @Spec CommandSpec spec;
    @Option(
        names = { "--interactive" },
        negatable = true,
        defaultValue = "true", // Default to interactive mode
        fallbackValue = "true",
        description = "Run cli in interactive mode w/ menus & other prompts. Defaults to interactive mode; specify `--no-interactive` to disable interactive mode."
    ) boolean interactive;
    // @Option(
    //     names = { "-v", "--verbose" },
    //     description = "Enable verbose mode. Increase verbosity by setting multiple times, e.g. `-vvv` or `--verbose --verbose --verbose`"
    // )
    // private boolean[] verbosity = new boolean[0];

    private String name;
    private String description;
    private String version;

    public ChiPub(String name, String description, String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }

    @Override
    public void run() {
        // prompt user with menu if interactive mode isn't disabled
        if ( interactive ) {
            List<AttributedString> header = new ArrayList<>();
            header.add(new AttributedStringBuilder()
                    .append(name)
                    .append(", version ")
                    .append(version)
                    .append("\n")
                    .append(description)
                    .append("\n")
                    .append("\n")
                    .toAttributedString());

            try (Terminal terminal = TerminalBuilder.builder().build()) {
                ConsolePrompt prompt = new ConsolePrompt(terminal);
                PromptBuilder builder = prompt.getPromptBuilder();

                // TODO: add more actions to main menu
                builder.createListPrompt()
                    .name("mainMenu")
                    .message("What would you like to do?")
                    .newItem("check-out")
                    .text("Check out a book")
                    .add()
                    .newItem("check-in")
                    .text("Check in a book")
                    .add()
                    .addPrompt();

                Map<String, PromptResultItemIF> result = prompt.prompt(header, builder.build());
                // TODO: make this direct user to appropriate handler
                System.out.println("result = " + result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    // /**
    //  * Custom execution strategty to set log level before proceeding to execute commands.
    //  */
    // public int executionStrategy(ParseResult parseResult) {
    //     configureLogger();

    //     return new CommandLine.RunLast().execute(parseResult);
    // }

    // /**
    //  * Configure log level from verbosity command
    //  */
    // private void configureLogger() {
    //     // set log level from verbosity flag
    //     Configurator.setRootLevel(calcLogLevel());
    // }

    // /**
    //  * Convert number of verbosity options given to log4j2 log level
    //  */
    // private Level calcLogLevel() {
    //     switch (verbosity.length) {
    //         case 0: return Level.WARN;
    //         case 1: return Level.INFO;
    //         case 2: return Level.DEBUG;
    //         default: return Level.TRACE;
    //     }
    // }

    /**
     * SUBCOMMANDS
     */

    // check out a book
    @Command(name = "check-out", description = "Check out a book to a cardholder")
    void checkOut(
        @Parameters(index = "0", paramLabel = "<copy id number>") int copyId,
        @Parameters(index = "1", paramLabel = "<member's card number>") int cardholderId
    ) {
        // TODO...
        // validate params
        // then call query from db code
        // result ?
        //   on success => return 0?
        //   else       => handle error ?
        //     if book already checked out,
        //     or book subject to pending hold,
        //     or cardholder has past due books still => return 1  // tell user
        //     else                                   => return 10 // report unhandled error, maybe code will be something else?
    }

    // return a book
    @Command(name = "check-in", description = "Return a book from a cardholder")
    void checkIn(
        @Parameters(index = "0", paramLabel = "<copy id number>") int copyId,
        @Parameters(index = "1", paramLabel = "<member's card number>") int cardholderId
    ) {
        // TODO...
        // validate params
        // call db code
        // result ?
        //   success => return 0
        //   else    => handle error ?
        //     book not checked out => return 1  // tell user
        //     unknown error        => return 10 // unhandled error
    }

    // request a hold
    @Command(name = "hold-request", description = "Request a hold be placed on a book for a cardholder")
    void holdRequest(
        @Parameters(index = "0", paramLabel = "<book id number>") int copyId,
        @Parameters(index = "1", paramLabel = "<member's card number>") int cardholderId
    ) {
        // TODO...
    }

    // cancel a hold
    @Command(name = "hold-cancel", description = "Cancel a previously requested hold for a cardholder")
    void holdCancel(
        @Parameters(index = "0", paramLabel = "<book id number>") int copyId,
        @Parameters(index = "1", paramLabel = "<member's card number>") int cardholderId
    ) {
        // TODO...
    }
}

@Command(name = "search", description = "Search for...", mixinStandardHelpOptions = true)
class Search implements Runnable {
    @Spec CommandSpec spec;

    // first, the main function that defers all actions to subcommands
    @Override
    public void run() {
        // TODO: maybe this actually can be run w/out subcommands, instead searching across all 3
        // types, but without any flags for narrowing...
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    };

    //
    // SUBCOMMANDS
    //

    // search for books
    @Command(name = "book", description = "books by a simple string, or by providing specific values for various properties, such as author, genre, isbn, etc")
    void book(
        @Parameters(index = "0", paramLabel = "<search terms>") String query,
        @Option(names = { "-a", "--author" }, description = "narrow search by author") String author,
        @Option(names = { "-g", "--genre" }, description = "narrow search by genre") String genre,
        @Option(names = { "-i", "--isbn" }, description = "narrow search by isbn") String isbn,
        @Option(names = { "-l", "--language" }, description = "narrow search by language") String language,
        @Option(names = { "-s", "--subject" }, description = "narrow search by subject") String subject
    ) {
        // TODO...
    };

    // search for cardholders
    @Command(name = "cardholder", description = "cardholders by a simple string, or by providing specific values for various properties, such as name, address, phone number, etc.")
    void cardholder(
        @Parameters(index = "0", paramLabel = "<search terms>") String query,
        @Option(names = { "-a", "--address" }, description = "narrow search by address") String address,
        @Option(names = { "-n", "--name" }, description = "narrow search by name") String name,
        @Option(names = { "-p", "--phone-number" }, description = "narrow search by phone number") String phoneNumber
    ) {
        // TODO...
    };
}

@Command(name = "view", description = "View information about...", mixinStandardHelpOptions = true)
class View implements Runnable {
    @Spec CommandSpec spec;

    // first, the main function that defers all actions to subcommands
    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    };

    //
    // SUBCOMMANDS
    //

    // view a collection of stuff about a cardholder, including:
    // - books checked out
    //   - current/past/all time
    //   - due soon
    //   - overdue
    // - holds requested
    //   - current/past/all time
    //   - position in hold queue (& expected wait time)
    @Command(name = "cardholder", description = "a cardholder")
    void cardholder(
        @Parameters(index = "0", paramLabel = "<cardholder id number>") int cardholderId
    ) {
        // TODO...
    }

    // view a collection of stuff about a book, including:
    // - info about copies owned by library, such as
    //   - how many copies are available/checked out/pending holds
    //   - where copies are located
    // - genre
    // - subject
    // - language
    // - author (name, short about/bio)
    // - isbn
    // - summary/description?
    // - cover image?
    @Command(name = "book", description = "a book")
    void book(
        @Parameters(index = "0", paramLabel = "<book id number>") int bookId
    ) {
        // TODO...
    }

    // view a collection of stuff about an author, including:
    // - bio/about the author
    // - genres/subjects the author has published in/on that the library has
    // - all books by the author the library has
    // - maybe other authors that people who've checked out books by this author also like?
    @Command(name = "author", description = "an author")
    void author(
        @Parameters(index = "0", paramLabel = "<author id number>") int authorId
    ) {
        // TODO...
    }
}
