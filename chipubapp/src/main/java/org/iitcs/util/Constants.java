package org.iitcs.util;

import java.util.HashMap;

public class Constants {
    public static final String QUERY_PROPERTIES_FILE = "/queries.properties";
    public static final String DEFAULT_PROPERTIES_FILE = "/default_properties.properties";
    public static final String TABULATE_FORMATTING="%-20s";
    public static final String CARRIAGE_RETURN="\\r";
    public static final String EMPTY_STRING="";
    public static final String ELLIPSES="...";
    public static final String SEPERATOR="--------------------";
    public static final int TRAILING_CHARACTERS_CONCAT_BOOK_QUERY = 4;
    public enum CRUD{
        CREATE, READ, UPDATE, DELETE
    }
    public static final HashMap<CRUD, String> QUERY_KEYS = new HashMap<>(){{
        put(CRUD.CREATE, "sql.create.");
        put(CRUD.READ, "sql.read.");
        put(CRUD.UPDATE, "sql.update.");
        put(CRUD.DELETE, "sql.delete.");
    }};
}
