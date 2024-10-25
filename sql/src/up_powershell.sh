$db="chipub"
$glob = Get-ChildItem "PATH\TO\CS425-ChiPub\sql\src\mock-data"

cd "$(dirname "$0")"

echo "Setting up database & tables..."
# start in sys database since a database has to
# be selected to execute a sql source
& cmd.exe /c "mysql -u root -pPASS < schema.sql"
# verify database created
mysql -u root -pPASS -e "show databases;"
# verify tables created
mysql -u root -pPASS $db -e "show tables;"
 
# enable loading csv from file
mysql -u root -pPASS $db -e "set global local_infile=true;"
echo "Loading data from csv source..."
# loop over data files & load each one
foreach ($f in $glob){
    $n=[io.path]::GetFileNameWithoutExtension($f)  # strip extension from file, giving basename only
    $t=$n
    $t=$t -replace '\d+-',''
    echo "loading data from $f into $t"
    # load data from csv
    $l="load data local infile 'mock-data/$f' into table $t fields terminated by ',' ignore 1 lines;"
    mysql --local-infile=1 -u root -pPASS $db -e "$l" 
    # select some records to verify data loaded
    mysql -u root -pPASS $db -e "select * from $t limit 15;"
}

& cmd.exe /c "mysql -u root -pPASS < util_data.sql"
& cmd.exe /c "mysql -u root -pPASS < util_func.sql"
& cmd.exe /c "mysql -u root -pPASS < cardholder_copy_nulloutzerodatetimefields.sql"