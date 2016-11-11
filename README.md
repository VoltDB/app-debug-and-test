# Example Procedure Debugging and Testing Code

Goal is to walk through creating an Eclipse project here shortly.

The long and short of it is to make a Java project with client and procedure directories as source folders.

Then add the junit jar from /lib, any jars from $VOLTDB_HOME/lib, and the main VoltDB jar from $VOLTDB_HOME/voltdb to the build path.

Then it may be helpful to add a log4j properties setting in your JVM file and you probably want to set the minimum heap to a gig or so.

To use the utility classes InProcessVoltDBServer and SQLCommandHack (which allow you to run a small VoltDB server instance in process for unit tests or debugging) in your own project, the compile_utils.sh script is provided.  It compiles and outputs VoltDBProcedureTestUtils.jar, which can be added to the build path in your own project.

MORE SOON!!!
