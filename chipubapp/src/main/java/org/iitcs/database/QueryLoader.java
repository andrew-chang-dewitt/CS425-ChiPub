package org.iitcs.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iitcs.util.PropertiesLoader;

import java.io.IOException;
import java.util.Properties;

import org.iitcs.util.Constants;
import org.iitcs.util.Constants.CRUD;

import static org.iitcs.util.Constants.QUERY_KEYS;

public class QueryLoader {
    private static final Logger LOGGER = LogManager.getLogger(QueryLoader.class);
    private Constants c;
    private final Properties queries = new Properties();
    private static QueryLoader instance;

    /**
     * QueriesManager is a singleton.
     * Use getInstance() to access global properties
     */
    public static synchronized QueryLoader getInstance(){
        if(instance != null){
            return instance;
        }
        return new QueryLoader();
    }
    private QueryLoader(){
        try{
            this.queries.load(PropertiesLoader.class.getResourceAsStream(c.QUERY_PROPERTIES_FILE));
        }
        catch(IOException e){
            LOGGER.error(e.getMessage());
            LOGGER.error("Query properties file: "
                    .concat(c.QUERY_PROPERTIES_FILE)
                    .concat(" not found in root directory.")
            );
        }
    }

    public String get(CRUD crud, String tag){
        String q = queries.getProperty(QUERY_KEYS.get(crud).concat(tag));
        if(q != null){
            return q;
        }
        throw new RuntimeException("No query with tag ".concat(tag).concat(" was found."));
    }
}
