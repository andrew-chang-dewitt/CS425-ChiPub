package org.iitcs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iitcs.cli.Cli;
import org.iitcs.database.ChiPubConnection;
import org.iitcs.database.QueriesManager;
import org.iitcs.util.Constants.CRUD;
import org.iitcs.util.PropertiesManager;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static PropertiesManager props = PropertiesManager.getInstance();
    private static QueriesManager queries = QueriesManager.getInstance();

    public static void main(String... args) {
        Cli cli = new Cli(args);
        int exitCode = cli.run();
        System.exit(exitCode);
        // sampleCode();
    }

    public static void sampleCode(){
        LOGGER.info("Hello world!");

        /*** connect to DB ***/
        Properties dbCredentials = new Properties();
        dbCredentials.put("user", props.getDbAdminUsername());
        dbCredentials.put("password", props.getDbAdminPassword());

        ChiPubConnection cpc = new ChiPubConnection(props.getDbJdbcUrl(), props.getDbJdbcSchema(), dbCredentials);

        /*** querying ***/
        String q = queries
                .get(CRUD.READ, "selectall")
                .replace("$tname", "mytablename");

        /*** Always wrap initializing PreparedStatements in the try with resources block
         * I.E. try(in here!)
         * This prevents memory leaks and automatically closes the PreparedStatement.***/
        try(PreparedStatement ps = cpc.getChiPubConnectionObj().prepareStatement(q);){
            LOGGER.info("Execute query: ".concat(ps.toString()));
            ResultSet result = ps.executeQuery();
            while(result.next()){
                result.getString("column name x");
                result.getInt("column name y");
                //do something with each row of database output, call resultset.getx for each column
            }
            result.close(); //close result sets every time!!!!
        }catch(SQLException e){
            LOGGER.error(e.getMessage());
        }

        //Always remember to close your connection when you're done-- again, prevents memory leaks
        cpc.closeConnection();
    }
}
