@echo off

rem java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n -cp cfg;bin/* org.homs.supraedit.SupraEditor

java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n -cp supra-edit-0.0.1-SNAPSHOT.jar org.homs.supraedit.SupraEditor

