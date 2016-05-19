/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
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

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

import com.google_voltpatches.common.base.Charsets;

public class Unpack extends VoltProcedure {

	final SQLStmt insertFull =
			new SQLStmt("insert into demo values (?, ?, ?);");
	final SQLStmt insertSubset =
			new SQLStmt("insert into demo values (?, ?, field(?, 'glossary.GlossDiv'));");

	static public String gunzipBytes(byte[] value) throws IOException {
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

	    while ((len = gzip.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

	    byte[] bytesOut = out.toByteArray();
	    String retval = new String(bytesOut, Charsets.UTF_8);

	    return retval;
	}

    public VoltTable[] run(byte[] compressedJSON) {

    	String jsonStr = null;

    	try {
			jsonStr = gunzipBytes(compressedJSON);
		} catch (IOException e) {
			throw new VoltAbortException(e);
		}

    	voltQueueSQL(insertFull, EXPECT_SCALAR_MATCH(1), 1, 1, jsonStr);
    	voltQueueSQL(insertSubset, EXPECT_SCALAR_MATCH(1), 2, 2, jsonStr);
    	return voltExecuteSQL();
    }
}
