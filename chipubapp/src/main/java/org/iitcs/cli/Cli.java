package org.iitcs.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
    resourceBundle = "default_properties",
    name = "${bundle:name}",
    description = "${bundle:description}",
    version = "${bundle:version}",
    mixinStandardHelpOptions = true,
    subcommands = {Search.class, View.class}
)
public class Cli implements Runnable {
    @Spec CommandSpec spec;

    // first, the main function that defers all actions to subcommands
    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    };

    //
    // SUBCOMMANDS
    //

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
