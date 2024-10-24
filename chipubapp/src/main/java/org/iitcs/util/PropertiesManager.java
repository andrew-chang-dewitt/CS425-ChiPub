package org.iitcs.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    private static PropertiesManager instance = null;
    private static final Logger LOGGER = LogManager.getLogger(PropertiesManager.class);
    private Constants c;
    private final Properties properties = new Properties();
    private String dbJdbcUrl;
    private String dbJdbcSchema;
    private String dbAdminUsername;
    private String dbAdminPassword;
    private String name;
    private String description;
    private String version;

    /**
     * PropertiesManager is a singleton.
     * Use getInstance() to access global properties
     */
    public static synchronized PropertiesManager getInstance(){
        if(instance != null){
            return instance;
        }
        return new PropertiesManager();
    }
    private PropertiesManager(){
        /*TODO: Should be able to pass a custom properties file in the JVM args*/
        try{
            this.properties.load(PropertiesManager.class.getResourceAsStream(c.DEFAULT_PROPERTIES_FILE));
            readPropertiesIntoVariables();
        }
        catch(IOException e){
            LOGGER.error(e.getMessage());
            LOGGER.error("Default properties file: "
                    .concat(c.DEFAULT_PROPERTIES_FILE)
                    .concat(" not found in root directory.")
            );
        }
    }

    private void readPropertiesIntoVariables(){
        this.dbJdbcUrl = this.properties.getProperty("db.jdbc.url");
        this.dbJdbcSchema = this.properties.getProperty("db.jdbc.schema");
        this.dbAdminUsername = this.properties.getProperty("db.admin.username");
        this.dbAdminPassword = this.properties.getProperty("db.admin.password");
        this.name = this.properties.getProperty("name");
        this.description = this.properties.getProperty("description");
        this.version = this.properties.getProperty("version");
    }

    public String getDbJdbcUrl() { return dbJdbcUrl;}
    public String getDbJdbcSchema() { return dbJdbcSchema;}
    public String getDbAdminUsername() { return dbAdminUsername; }
    public String getDbAdminPassword() { return dbAdminPassword;}
    public String getName() { return name;}
    public String getDescription() { return description;}
    public String getVersion() { return version;}
}
