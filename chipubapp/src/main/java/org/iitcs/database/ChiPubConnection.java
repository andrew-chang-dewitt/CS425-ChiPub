package org.iitcs.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class ChiPubConnection {
    private Connection conn;
    private static final Logger LOGGER = LogManager.getLogger(ChiPubConnection.class);

    public ChiPubConnection(String url, String schema, Properties credentials) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, credentials);
            if (conn.isValid(1)) {
                LOGGER.info("Connection to ".concat(url).concat(" successful."));
            }
            PreparedStatement ps = conn.prepareStatement("use ".concat(schema).concat(";"));
            LOGGER.info("Connecting to schema: ".concat(ps.toString()));
            ps.execute();
        } catch (Exception e) {
            System.out.println("Connection to database failed. Check your properties file.");
            LOGGER.error(e.getMessage());
            LOGGER.error("Attempted to connect with:" +
                    "\n- url: " + url +
                    "\n- schema: " + schema +
                    "\n- credentials: " + credentials);
            System.exit(1);
        }
    }

    public Connection getChiPubConnectionObj() {
        return this.conn;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            LOGGER.error("Unable to close connection.");
            LOGGER.error(e.getMessage());
        }
    }
}
