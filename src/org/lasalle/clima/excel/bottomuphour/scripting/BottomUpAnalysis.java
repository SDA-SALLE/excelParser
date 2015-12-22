package org.lasalle.clima.excel.bottomuphour.scripting;

import java.io.File;

import org.cheminfo.function.Function;
import org.cheminfo.function.scripting.SecureFileManager;
import org.lasalle.clima.excel.bottomuphour.Aggregator;
/**
 * 
 * @author acastillo
 *
 */

public class BottomUpAnalysis extends Function{
	
	public boolean process(String file1, String file2, String file3, String outputFolder, String[] types, String basedir, String basedirkey){
		//this.appendError("BottomUp::process", file1);
		//To create the output folder if different than the input folder
		String file1Full = SecureFileManager.getValidatedFilename(
				basedir, basedirkey, file1);
		if (file1Full == null){
			this.appendError("BottomUpAnalysis::process","Could not get the path to "+file1);
			return false;
		}
		String file2Full = SecureFileManager.getValidatedFilename(
				basedir, basedirkey, file2);
		if (file2Full == null){
			this.appendError("BottomUpAnalysis::process","Could not get the path to "+file2);
			return false;
		}
		String file3Full = SecureFileManager.getValidatedFilename(
				basedir, basedirkey, file3);
		if (file3Full == null){
			this.appendError("BottomUpAnalysis::process","Could not get the path to "+file3);
			return false;
		}
		String outputFolderFull = SecureFileManager.getValidatedFilename(
				basedir, basedirkey, outputFolder);
		if (outputFolderFull == null){
			this.appendError("BottomUpAnalysis::process","Could not get the path to "+outputFolder);
			return false;
		}
		File output = new File(outputFolderFull);
		if(!output.exists()){
			if(output.mkdirs())
				this.appendInfo("BottomUpAnalysis::process","Output folder created: "+outputFolder);
		}
		
		Aggregator processor = new Aggregator(new File(file1Full),
				new File(file2Full), 
				new File(file3Full),
				new File(outputFolderFull),this);
		processor.setTypes(types);
		processor.expandCells();
		return true;
	}
}
