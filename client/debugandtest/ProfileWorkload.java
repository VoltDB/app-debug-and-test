package debugandtest;

import java.util.Random;

import org.voltdb.InProcessVoltDBServer;
import org.voltdb.client.Client;
import org.voltdb.client.NullCallback;

public class ProfileWorkload {

	public static void main(String[] args) throws Exception {
		InProcessVoltDBServer volt = new InProcessVoltDBServer();
		volt.start();

        volt.runDDLFromPath("./ddl.sql");

        Client client = volt.getClient();

        Random r = new Random();
        while (true) {
        	if (r.nextInt(1000) == 0) {
        		client.callProcedure(new NullCallback(), "ProcA", r.nextInt());
        	}
        	else {
        		client.callProcedure(new NullCallback(), "ProcB", r.nextInt());
        	}
        }
	}
}
