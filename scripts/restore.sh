#!/bin/sh

bin/asadmin stop-domain exmatrikulator
javadb/bin/stopNetworkServer
rm -rf Exmatrikulator/*
cp -R backups/$1/Exmatrikulator/* Exmatrikulator/
javadb/bin/startNetworkServer -h localhost -p 1527 &
bin/asadmin start-domain exmatrikulator
