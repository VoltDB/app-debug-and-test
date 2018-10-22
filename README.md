# Example Procedure Debugging and Testing Code

This project was created to show an example of how to unit test and debug VoltDB stored procedures. When it was created, it had to rely on some utility classes to enable a minimal instance of VoltDB to run in the same process as the test class.

This project includes both the utilites (VoltDBProcedureTestUtils.jar) and example tests.

In 2017, the API from these utility classes was integrated into the VoltDB source code, and expanded instructions were written here: https://github.com/VoltDB/voltdb/blob/master/examples/HOWTOs/EclipseUnitTestDebug.md

If you are using v7.2 or later, you can follow the instructions there, and you do not need anything from this app-debug-and-test repository.

If you are using v7.1 or earlier, you shoudl still follow the instructions there. It will include cloning this project and building the VoltDBProcedureTestUtils.jar file, and adding that to Eclipse.

Below are the original instructions:

Goal is to walk through creating an Eclipse project here shortly.

The long and short of it is to make a Java project with client and procedure directories as source folders.

Then add the junit jar from /lib, any jars from $VOLTDB_HOME/lib, and the main VoltDB jar from $VOLTDB_HOME/voltdb to the build path.

Then it may be helpful to add a log4j properties setting in your JVM file and you probably want to set the minimum heap to a gig or so.

To use the utility classes InProcessVoltDBServer and SQLCommandHack (which allow you to run a small VoltDB server instance in process for unit tests or debugging) in your own project, the compile_utils.sh script is provided.  It compiles and outputs VoltDBProcedureTestUtils.jar, which can be added to the build path in your own project.

MORE SOON!!!
