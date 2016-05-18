package debugandtest;

import org.voltdb.InProcessVoltDBServer;
import org.voltdb.client.Client;
import org.voltdb.client.ProcCallException;

public class BuggyWorkload {

	public static void main(String[] args) throws Exception {
		InProcessVoltDBServer volt = new InProcessVoltDBServer();
		volt.start();

        volt.runDDLFromPath("./ddl.sql");

        Client client = volt.getClient();

        try {
        	client.callProcedure("BuggyProc");
        }
        catch (ProcCallException e) {
        	e.printStackTrace();
        }

        volt.shutdown();
	}
}
