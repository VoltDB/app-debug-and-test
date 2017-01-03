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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.voltdb.InProcessVoltDBServer;
import org.voltdb.client.Client;

import com.google_voltpatches.common.base.Charsets;

public class PayloadWorkload {

	static String exampleJSON =
			"{\n" +
			"    \"glossary\": {\n" +
			"        \"title\": \"example glossary\",\n" +
			"		\"GlossDiv\": {\n" +
			"            \"title\": \"S\",\n" +
			"			\"GlossList\": {\n" +
			"                \"GlossEntry\": {\n" +
			"                    \"ID\": \"SGML\",\n" +
			"					\"SortAs\": \"SGML\",\n" +
			"					\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
			"					\"Acronym\": \"SGML\",\n" +
			"					\"Abbrev\": \"ISO 8879:1986\",\n" +
			"					\"GlossDef\": {\n" +
			"                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
			"						\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
			"                    },\n" +
			"					\"GlossSee\": \"markup\"\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n";

	static public byte[] gzipString(String value) throws IOException {
		if (value == null) {
			return null;
		}
		if (value.length() == 0) {
			return new byte[] {};
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    GZIPOutputStream gzip = new GZIPOutputStream(out);
	    gzip.write(value.getBytes(Charsets.UTF_8));
	    gzip.close();

	    return out.toByteArray();
	}

	static public String gunzipString(byte[] value) throws IOException {
		if (value == null) {
			return null;
		}
		if (value.length == 0) {
			return "";
		}

		ByteArrayInputStream in = new ByteArrayInputStream(value);
	    GZIPInputStream gzip = new GZIPInputStream(in);

	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    int len;

	    while((len = gzip.read(buffer)) != -1){
            out.write(buffer, 0, len);
        }

	    byte[] bytesOut = out.toByteArray();
	    String retval = new String(bytesOut, Charsets.UTF_8);

	    return retval;
	}

	public static void main(String[] args) throws Exception {
		InProcessVoltDBServer volt = new InProcessVoltDBServer();
		volt.start();

        volt.runDDLFromPath("./ddl.sql");

        Client client = volt.getClient();

        byte[] payload = gzipString(exampleJSON);

        // double check it's sane
        String copy = gunzipString(payload);
        assert(copy.equals(exampleJSON));

        client.callProcedure("Unpack", payload);

        volt.shutdown();
	}
}
