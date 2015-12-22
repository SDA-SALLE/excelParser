package org.lasalle.clima.scripting.tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
/**
 * 
 * @author acastillo
 *
 */
public class TestScripting {

	@Test
	public void test() throws JSONException {
		
		ScriptingInstance interpreter = new ScriptingInstance();
		JSONObject result=null;
		
		String script="var pi=3.14159265;jexport('pi',pi)";
		result = interpreter.runScript(script);
		assertTrue((result.getJSONArray("pi")).getDouble(0)==3.14159265);
		
		script="pi+=3.14159265;jexport('pi',pi)";
		result = interpreter.runScript(script);
		System.out.println((result.getJSONArray("pi")).getDouble(0));
		assertTrue((result.getJSONArray("pi")).getDouble(0)==2*3.14159265);
		
	}
	
	
	public static void main(String[] args) {
		ScriptingInstance interpreter = new ScriptingInstance("/usr/local/script/plugins/");
		interpreter.setSafePath("./");
		//interpreter.setSafePath("./");
		//String script = getContents(new File("jsexamples/autoAssignment.js"));
		//String script = getContents(new File("jsexamples/resurrect.js"));

		//String script = getContents(new File("../dataMining/jsexamples/plsr.js"));
		//String script = getContents(new File("jsexamples/structuredDataMining.js"));
		//String script = getContents(new File("../dataMining/jsexamples/console.js"));
		//String script = getContents(new File("../imageJPlugin/tests/processBio.js"));
		//String script = getContents(new File("jsexamples/spectraDataFromMolfile.js"));
		//String script = getContents(new File("jsexamples/console.js"));
		String script = getContents(new File("tests/bottomUp.js"));

		//interpreter.runScript(script);
		
		System.out.println(interpreter.runScript(script));
		
	}
	
	static public String getContents(File aFile) {
		try {
			System.out.println(aFile.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //...checks on aFile are elided
	    StringBuilder contents = new StringBuilder();
	    
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; //not declared within while loop
	        /*
	        * readLine is a bit quirky :
	        * it returns the content of a line MINUS the newline.
	        * it returns null only for the END of the stream.
	        * it returns an empty String if two newlines appear in a row.
	        */
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    return contents.toString();
	}
	
}
