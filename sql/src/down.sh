#!/usr/bin/env bash

db=chipub

cd "$(dirname "$0")"

echo ""
echo "Tearing down database & tables..."
mysql -ppass sys < down.sql
mysql -ppass -e "show databases;"
