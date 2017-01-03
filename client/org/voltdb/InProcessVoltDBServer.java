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

package org.voltdb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.voltdb.client.Client;
import org.voltdb.client.ClientConfig;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ProcCallException;
import org.voltdb.compiler.DeploymentBuilder;
import org.voltdb.utils.SQLCommandHack;

public class InProcessVoltDBServer {
    ServerThread server = null;
    String pathToLicense = null;
    Client loaderClient = null;
    int sitesPerHost = 8; // default

    List<Client> trackedClients = new ArrayList<>();

    public InProcessVoltDBServer configPartitionCount(int partitionCount) {
        sitesPerHost = partitionCount;
        return this;
    }

    public InProcessVoltDBServer configPathToLicense(String path) {
        pathToLicense = path;
        return this;
    }

    public InProcessVoltDBServer start() {
        DeploymentBuilder depBuilder = new DeploymentBuilder(sitesPerHost, 1, 0);
        depBuilder.setEnableCommandLogging(false);
        depBuilder.setUseDDLSchema(true);
        depBuilder.setHTTPDPort(8080);
        depBuilder.setJSONAPIEnabled(true);

        VoltDB.Configuration config = new VoltDB.Configuration();
        if (pathToLicense != null) {
            config.m_pathToLicense = pathToLicense;
        }
        else {
            config.m_pathToLicense = "./license.xml";
        }

        File tempDeployment = null;
        try {
            tempDeployment = File.createTempFile("volt_deployment_", ".xml");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        depBuilder.writeXML(tempDeployment.getAbsolutePath());
        config.m_pathToDeployment = tempDeployment.getAbsolutePath();

        server = new ServerThread(config);

        server.start();
        server.waitForInitialization();

        return this;
    }

    public InProcessVoltDBServer runDDLFromPath(String path) {
        int ret = SQLCommandHack.mainWithReturnCode(new String[] { String.format("--ddl-file=%s", path) });
        assert(ret == 0);
        return this;
    }

    public InProcessVoltDBServer runDDLFromString(String ddl) {
        try {
            File tempDDLFile = File.createTempFile("volt_ddl_", ".sql");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempDDLFile));
            writer.write(ddl + "\n");
            writer.close();
            runDDLFromPath(tempDDLFile.getAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return this;
    }

    public void shutdown() {
        for (Client client : trackedClients) {
            // best effort closing -- ignores many problems
            try {
                client.drain();
                client.close();
            }
            catch (Exception e) {}
        }
        loaderClient = null;

        try {
            server.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Client getClient() throws UnknownHostException, IOException {
        ClientConfig config = new ClientConfig();
        // turn off the timeout for debugging
        config.setProcedureCallTimeout(0);
        Client client = ClientFactory.createClient(config);
        // track this client so it can be closed at shutdown
        trackedClients.add(client);
        client.createConnection("localhost");
        return client;
    }

    public void loadRow(String tableName, Object... row) throws UnknownHostException, IOException, ProcCallException {
        if (loaderClient == null) {
            loaderClient = getClient();
        }
        String procName = tableName.trim().toUpperCase() + ".insert";
        loaderClient.callProcedure(procName, row);
    }

    public static void main(String args[]) {
        InProcessVoltDBServer volt = new InProcessVoltDBServer();
        volt.runDDLFromString("create table foo (vz bigint);");
        volt.start();
    }
}
