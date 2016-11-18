#!/usr/bin/env bash

# set CLASSPATH
if [ -d "$(dirname $(which voltdb))" ]; then
    source $(dirname "$(which voltdb)")/voltenv
else
    echo "VoltDB client library not found.  You need to add the VoltDB bin directory to your PATH"
    exit
fi

mkdir -p obj
javac -classpath $APPCLASSPATH -d obj client/org/voltdb/InProcessVoltDBServer.java client/org/voltdb/utils/SQLCommandHack.java
# stop if compilation fails
if [ $? != 0 ]; then exit; fi

jar cf VoltDBProcedureTestUtils.jar -C obj .
rm -rf obj

echo "created VoltDBProcedureTestUtils.jar"
