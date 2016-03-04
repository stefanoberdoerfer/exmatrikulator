call bin\asadmin stop-domain exmatrikulator
call javadb\bin\stopNetworkServer
call timeout 10
del Exmatrikulator /F /Q
call timeout 10
xcopy backups\%1 /E /H /R /Y /I
call timeout 10
start javadb\bin\startNetworkServer -h localhost -p 1527
call timeout 10
call bin\asadmin start-domain exmatrikulator