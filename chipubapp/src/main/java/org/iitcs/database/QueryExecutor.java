package org.iitcs.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iitcs.Main;
import org.iitcs.util.Constants.CRUD;
import org.iitcs.util.PropertiesLoader;

import java.sql.*;
import java.util.Properties;

import static org.iitcs.util.Constants.*;

public class QueryExecutor {

    /*Update queries can give back a state of 0 (nothing changes), 1 (update success), or -1 (something went wrong)*/
    private ChiPubConnection conn;
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final PropertiesLoader props = PropertiesLoader.getInstance();
    private static final QueryLoader queries = QueryLoader.getInstance();

    public QueryExecutor(){
        Properties dbCredentials = new Properties();
        dbCredentials.put("user", props.getDbAdminUsername());
        dbCredentials.put("password", props.getDbAdminPassword());

        conn = new ChiPubConnection(props.getDbJdbcUrl(), props.getDbJdbcSchema(), dbCredentials);
    }
    public void printTabulatedResultSet(ResultSet rs){
        try{

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            printHeader(metaData, columnCount);

            printSeperator(columnCount);

            printRows(rs, columnCount);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printHeader(ResultSetMetaData metaData, int columnCount) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf(TABULATE_FORMATTING, metaData.getColumnName(i));
        }
        System.out.println();
    }

    private void printSeperator(int columnCount) {
        for (int i = 1; i <= columnCount; i++) {
            System.out.print(SEPERATOR);
        }
        System.out.println();
    }

    private void printRows(ResultSet rs, int columnCount) throws SQLException {
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String val = rs.getString(i);
                if(val != null){
                    val = val.replaceAll(CARRIAGE_RETURN, EMPTY_STRING); //deal with random carriage returns
                    if(val.length() > 12){ //trim very long titles
                        val = val.substring(0, 12);
                        val = val.concat(ELLIPSES);
                    }
                }
                System.out.printf(TABULATE_FORMATTING, val);
            }
            System.out.println();
        }
    }
    public void executeCardholderSearch(String name, String addr, String phonenum){
        executeDropBookSearchTables(); //clear database's search tables
        executeCardholderPhoneIndexTempTableCreation();

        StringBuilder qCardholderSearch = new StringBuilder();

        String qName = queries.get(CRUD.READ, "cardholder.where.name");
        String qAddr = queries.get(CRUD.READ, "cardholder.where.addr");
        String qPhoneNum = queries.get(CRUD.READ, "cardholder.where.phone");

        qCardholderSearch.append(queries.get(CRUD.READ, "cardholder.where"));

        concatSearchQuery(name, qCardholderSearch, qName);
        concatSearchQuery(addr, qCardholderSearch, qAddr);
        concatSearchQuery(phonenum, qCardholderSearch, qPhoneNum);

        qCardholderSearch.delete(qCardholderSearch.length()-TRAILING_CHARACTERS_CONCAT_BOOK_QUERY, qCardholderSearch.length());

        //TODO Transition the dynamic query to prepared statements at some point
        try(PreparedStatement ps = conn.getChiPubConnectionObj().prepareStatement(qCardholderSearch.toString())){
            System.out.println("Executing search for cardholder...");
            LOGGER.info("Executing search query with: ".concat(qCardholderSearch.toString()));
            ResultSet joinedSearchResult = ps.executeQuery();
            printTabulatedResultSet(joinedSearchResult);
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }
    }

    public void executeBookSearch(String author, String genre, String isbn, String lang, String subject){
        executeDropBookSearchTables(); //clear database's search tables
        executeMasterBookIndexTempTableCreation();

        StringBuilder qBookSearch = new StringBuilder();

        //get where queries
        String qAuthor = queries.get(CRUD.READ, "book.where.searchauthor.lastname");
        String qGenre = queries.get(CRUD.READ, "book.where.genre");
        String qIsbn = queries.get(CRUD.READ, "book.where.isbn");
        String qLang = queries.get(CRUD.READ, "book.where.language");
        String qSubj = queries.get(CRUD.READ, "book.where.subject");

        //setup book search query to be concatenated
        qBookSearch.append(queries.get(CRUD.READ, "book.selectwhere.masterbookindex"));

        concatSearchQuery(author, qBookSearch, qAuthor);
        concatSearchQuery(genre, qBookSearch, qGenre);
        concatSearchQuery(isbn, qBookSearch, qIsbn);
        concatSearchQuery(lang, qBookSearch, qLang);
        concatSearchQuery(subject, qBookSearch, qSubj);

        qBookSearch.delete(qBookSearch.length()-TRAILING_CHARACTERS_CONCAT_BOOK_QUERY, qBookSearch.length());

        //TODO Transition the dynamic query to prepared statements at some point
        //run the join query & print the result set to console
        try(PreparedStatement ps = conn.getChiPubConnectionObj().prepareStatement(qBookSearch.toString())){
            System.out.println("Executing search for book...");
            LOGGER.info("Executing search query with: ".concat(qBookSearch.toString()));
            ResultSet joinedSearchResult = ps.executeQuery();
            printTabulatedResultSet(joinedSearchResult);
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }
    }

    private static void concatSearchQuery(String input, StringBuilder qJoin, String query) {
        if(input !=null){
            qJoin.append(query.replace("$param", input));
            qJoin.append(queries.get(CRUD.READ, "and"));
        }
    }

    public int executeCallableStatementIntParameters(CRUD crudTag, String qTag, int Param1, int Param2, String logMessage){
        String q = queries.get(crudTag, qTag);
        try(CallableStatement cs = conn.getChiPubConnectionObj().prepareCall(q)){
            cs.setInt(1, Param1);
            cs.setInt(2,Param2);
            cs.execute();
            LOGGER.info(logMessage);
            if(cs.getUpdateCount() == 0 ){
                return 0; //update ran successfully but nothing was changed
            }
            return 1;
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }
        return -1;
    }

    public int executeMasterBookIndexTempTableCreation(){
        String q = queries.get(CRUD.CREATE, "book.run.createmasterbookindex");
        try(CallableStatement cs = conn.getChiPubConnectionObj().prepareCall(q)){
            cs.execute();
            LOGGER.info("Created book search index table.");
            if(cs.getUpdateCount() == 0 ){
                return 0; //update ran successfully but nothing was changed
            }
            return 1;
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }
        return -1;
    }
    public int executeCardholderPhoneIndexTempTableCreation(){
        String q = queries.get(CRUD.CREATE, "book.run.createcardholderphoneindex");
        try(CallableStatement cs = conn.getChiPubConnectionObj().prepareCall(q)){
            cs.execute();
            LOGGER.info("Created cardholder phone search index table.");
            if(cs.getUpdateCount() == 0 ){
                return 0; //update ran successfully but nothing was changed
            }
            return 1;
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }
        return -1;
    }
    public int executeDropBookSearchTables(){
        String q = queries.get(CRUD.DELETE, "droptemporarybooksearchtables");
        try(CallableStatement cs = conn.getChiPubConnectionObj().prepareCall(q)){
            cs.execute();
            LOGGER.info("Cleared search tables.");
            if(cs.getUpdateCount() == 0 ){
                return 0; //update ran successfully but nothing was changed
            }
            return 1;
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }
        return -1;
    }
    public int executeCheckIn(int cid, int chid){
        return executeCallableStatementIntParameters(CRUD.UPDATE,"checkin", cid, chid, "Check in occurred");
    }

    public int executeCheckOut(int cid, int chid) {
        return executeCallableStatementIntParameters(CRUD.UPDATE,"checkout", cid, chid, "Check out occurred");
    }
}
