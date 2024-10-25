package org.iitcs.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jline.consoleui.elements.ConfirmChoice.ConfirmationValue;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.ConsolePrompt.UiConfig;
import org.jline.consoleui.prompt.builder.ConfirmPromptBuilder;
import org.jline.consoleui.prompt.builder.ListPromptBuilder;
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
import picocli.CommandLine.Spec;

import org.iitcs.database.QueryExecutor;
import org.iitcs.util.PropertiesLoader;

/**
 * Encapsulates everything needed to instantiate & execute the CLI program.
 */
public class Cli {
    private static PropertiesLoader props = PropertiesLoader.getInstance();

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
        this.cmd = new CommandLine(chipub);
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
    subcommands = {Search.class/*, View.class*/}
)
class ChiPub implements Callable<Integer> {
    @Spec CommandSpec spec;
    @Option(
        names = { "--interactive" },
        negatable = true,
        defaultValue = "true", // Default to interactive mode
        fallbackValue = "true",
        description = "Run cli in interactive mode w/ menus & other prompts. Defaults to interactive mode; specify `--no-interactive` to disable interactive mode."
    ) boolean interactive;

    private String name;
    private String description;
    private String version;

    public ChiPub(String name, String description, String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }

    QueryExecutor qexec = new QueryExecutor();
    // first, the main function that defers all actions to subcommands
    @Override
    public Integer call() {
        // prompt user with menu if interactive mode isn't disabled
        if ( interactive ) {
            return interact();
        }

        // otherwise, a subcommand is required.
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    };

    /**
     * Interactive menu for navigating application w/out using subcommands.
     */
    int interact() {
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
            UiConfig cfg = new ConsolePrompt.UiConfig(">", "( )", "(x)", "( )");
            ConsolePrompt prompt = new ConsolePrompt(terminal, cfg);

            PromptBuilder menuPrompt = menuBuilder(prompt);
            Map<String, PromptResultItemIF> listResult = prompt.prompt(header, menuPrompt.build());

            PromptBuilder confirmPrompt = confirmBuilder(prompt, listResult.toString());
            Map<String, PromptResultItemIF> confirmResult = prompt.prompt(confirmPrompt.build());

            // TODO: make this direct user to appropriate handler
            System.out.println("results:\n  list = " + listResult + "\n  confirm = " + confirmResult);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
    }

    PromptBuilder menuBuilder(ConsolePrompt prompt) {
        PromptBuilder builder = prompt.getPromptBuilder();
        // TODO: add more actions to main menu
        ListPromptBuilder menu = builder
            .createListPrompt()
            .name("mainMenu")
            .message("Main Menu: What would you like to do?")
            .newItem("check-out")
            .text("Check out a book")
            .add()
            .newItem("check-in")
            .text("Check in a book")
            .add();
        menu.addPrompt();

        return builder;
    }

    PromptBuilder confirmBuilder(ConsolePrompt prompt, String prevResult) {
        PromptBuilder builder = prompt.getPromptBuilder();
        ConfirmPromptBuilder confirm = builder
            .createConfirmPromp()
            .name("confirm")
            .message("you chose " + prevResult + ", is this correct?")
            .defaultValue(ConfirmationValue.YES);
        confirm.addPrompt();

        return builder;
    }

    /**
     * SUBCOMMANDS
     */

    // check out a book
    @Command(name = "check-out", description = "Check out a book to a cardholder")
    void checkOut(
        @Parameters(index = "0", paramLabel = "<copy id number>") int copyId,
        @Parameters(index = "1", paramLabel = "<member's card number>") int cardholderId
    ) {
        switch(qexec.executeCheckOut(copyId, cardholderId)){
            case(0):
                System.out.println("Attempted to check-out, but no check-out was found with these parameters.");
                break;
            case(1):
                System.out.println("The copy was successfully checked out!");
                break;
            case(-1):
                System.out.println("Check-out attempt failed.");
                break;
        }
    }

    // return a book
    @Command(name = "check-in", description = "Return a book from a cardholder")
    void checkIn(
        @Parameters(index = "0", paramLabel = "<copy id number>") int copyId,
        @Parameters(index = "1", paramLabel = "<member's card number>") int cardholderId
    ) {
        switch(qexec.executeCheckIn(copyId, cardholderId)){
            case(0):
                System.out.println("Attempted to check-in, but no check-out was found with these parameters.");
                break;
            case(1):
                System.out.println("The copy was successfully checked in!");
                break;
            case(-1):
                System.out.println("Check-in attempt failed.");
                break;
        }
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
class Search implements Callable<Integer> {
    QueryExecutor qexec = new QueryExecutor();
    @Spec CommandSpec spec;

    // first, the main function that defers all actions to subcommands
    @Override
    public Integer call() {
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
        // Removing the generic query for now, save time on dev work
        // @Parameters(arity = "1..*", paramLabel = "<search terms>") String[] query,
        @Option(names = { "-a", "--author" }, description = "narrow search by author") String author,
        @Option(names = { "-g", "--genre" }, description = "narrow search by genre") String genre,
        @Option(names = { "-i", "--isbn" }, description = "narrow search by isbn") String isbn,
        @Option(names = { "-l", "--language" }, description = "narrow search by language") String language,
        @Option(names = { "-s", "--subject" }, description = "narrow search by subject") String subject
    ) {
        qexec.executeBookSearch(author, genre, isbn, language, subject);
    };

    // search for cardholders
    @Command(name = "cardholder", description = "cardholders by a simple string, or by providing specific values for various properties, such as name, address, phone number, etc.")
    void cardholder(
        //Removing the generic query for now, save time on dev work
        //@Parameters(index = "0", paramLabel = "<search terms>") String query,
        @Option(names = { "-a", "--address" }, description = "narrow search by address") String address,
        @Option(names = { "-n", "--name" }, description = "narrow search by name") String name,
        @Option(names = { "-p", "--phone-number" }, description = "narrow search by phone number") String phoneNumber
    ) {
       qexec.executeCardholderSearch(name, address, phoneNumber);
    };
}

//TODO Will come back to these later!
@Command(name = "view", description = "View information about...", mixinStandardHelpOptions = true)
class View implements Callable<Integer> {
    @Spec CommandSpec spec;

    // first, the main function that defers all actions to subcommands
    @Override
    public Integer call() {
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
