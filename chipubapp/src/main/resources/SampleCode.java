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