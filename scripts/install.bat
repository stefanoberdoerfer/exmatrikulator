start javadb\bin\startNetworkServer -h localhost -p 1527
call bin\asadmin create-domain exmatrikulator
call bin\asadmin start-domain exmatrikulator
call bin\asadmin enable-secure-admin
call bin\asadmin restart-domain exmatrikulator
call bin\asadmin create-jdbc-connection-pool --datasourceclassname "org.apache.derby.jdbc.ClientDataSource" --restype "javax.sql.DataSource" --property User=exmatrikulator:Password=%1:ConnectionAttributes=\;create\^=true:dataBaseName=Exmatrikulator:ServerName=localhost:PortNumber=1527 exmatrikulator
call bin\asadmin create-jdbc-resource --connectionpoolid exmatrikulator jdbc/exmatrikulator
call bin\asadmin deploy exmatrikulator.war
