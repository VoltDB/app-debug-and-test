/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package debugandtest;

import org.voltdb.InProcessVoltDBServer;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;

import junit.framework.TestCase;

public class UnitTestVoltDB extends TestCase {

	public void testProcedureReturn() throws Exception {
		InProcessVoltDBServer volt = new InProcessVoltDBServer();
		volt.start();

        volt.runDDLFromPath("./ddl.sql");

        // prime the database with two rows, one with an even value
        // and one with an odd value
        volt.loadRow("demo", 1, 1, "foo");
        volt.loadRow("demo", 4, 4, "bar");

        Client client = volt.getClient();

        // increment all even rows
        ClientResponse response = client.callProcedure("@AdHoc",
        		"UPDATE demo SET othernum = othernum + 1 " +
        		"  WHERE MOD(othernum, 2) = 0;");
        assertEquals(response.getStatus(), ClientResponse.SUCCESS);
        assertEquals(response.getResults().length, 1);
        assertEquals(response.getResults()[0].asScalarLong(), 1);

        // check that the rows have the values we expect
        response = client.callProcedure("@AdHoc",
        		"SELECT * FROM demo ORDER BY othernum ASC;");
        assertEquals(response.getStatus(), ClientResponse.SUCCESS);
        assertEquals(response.getResults().length, 1);
        VoltTable results = response.getResults()[0];

        assertEquals(results.getRowCount(), 2);

        assertEquals(results.fetchRow(0).getLong("othernum"), 1);
        assertEquals(results.fetchRow(1).getLong("othernum"), 5);

        volt.shutdown();
	}

}
