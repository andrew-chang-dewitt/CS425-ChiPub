#!/usr/bin/env bash

db=chipub
glob=mock-data/*.csv

cd "$(dirname "$0")"

echo "Setting up database & tables..."
# start in sys database since a database has to
# be selected to execute a sql source
mysql -ppass sys < schema.sql
# verify database created
mysql -ppass -e "show databases;"
# verify tables created
mysql -ppass $db -e "show tables;"
 
# enable loading csv from file
mysql -ppass $db -e "set global local_infile=true;"
echo "Loading data from csv source..."
# loop over data files & load each one
for f in $glob; do
    b=${f##*/} # strip preceding path from file, giving basename (with extension)
    n=${b%.*}  # strip extension from file, giving basename only
    t=${n##*-} # strip numeric prefix, giving table name
    echo "loading data from $f into $t"
    # load data from csv
    l="load data local infile '$f' into table $t fields terminated by ',' ignore 1 lines;"
    mysql --local-infile=1 -ppass $db -e "$l" 
    # select some records to verify data loaded
    mysql -ppass $db -e "select * from $t limit 15;"
done

mysql -ppass sys < util_data.sql
mysql -ppass sys < util_func.sql
mysql -ppass sys < cardholder_copy_nulloutzerodatetimefields.sql