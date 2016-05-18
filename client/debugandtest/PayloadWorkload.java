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
