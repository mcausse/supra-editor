#!/bin/sh

# dos2unix run.sh
# /home/mhoms/java/workospace/sukablyat-s/dist


java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n -cp supra-edit-0.0.2-SNAPSHOT.jar org.homs.supraedit.SupraEditor
