#!/bin/sh

javadb/bin/startNetworkServer -h localhost -p 1527 &
bin/asadmin create-domain exmatrikulator
bin/asadmin start-domain exmatrikulator
bin/asadmin enable-secure-admin
bin/asadmin restart-domain exmatrikulator
bin/asadmin create-jdbc-connection-pool \
        --datasourceclassname "org.apache.derby.jdbc.ClientDataSource" \
        --restype "javax.sql.DataSource" \
        --property User=exmatrikulator:Password=$1:ConnectionAttributes=\;create\\=true:dataBaseName=Exmatrikulator:ServerName=localhost:PortNumber=1527 exmatrikulator
bin/asadmin create-jdbc-resource --connectionpoolid exmatrikulator jdbc/exmatrikulator
bin/asadmin deploy exmatrikulator.war
