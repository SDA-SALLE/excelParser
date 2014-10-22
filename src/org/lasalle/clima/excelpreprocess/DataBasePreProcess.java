package org.lasalle.clima.excelpreprocess;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;

public class DataBasePreProcess {

	public static void main(String[] args) {
		//String inputFolder = "/Users/acastillo/Dropbox/BD_Movilidad2/RequierenReproceso/EstacionesMinisterio/";//"/Users/acastillo/Desktop/BD_Movilidad/FiltradosMinTransporte/habil/";
		//String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/filtrados_estaciones_principales/habil/";//"/Users/acastillo/Desktop/BD_Movilidad/FiltradosMinTransporte/habil/";
		//String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/filtrados_estaciones_principales/fin_de_semana/Sabado/";
		//String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/filtrados_ministerio_transporte/habil/";
		String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/filtrados_ministerio_transporte/habil/";
		
		boolean noDirection = true;
		if(args!=null && args.length>0){
			inputFolder=args[0];
			if(args[1].compareTo("true")==0||args[1].compareTo("yes")==0||args[1].compareTo("1")==0)
				noDirection=true;
		}
		
		String outputFolder = inputFolder;//"/Users/acastillo/Desktop/BD_Movilidad/ProcFiltradosEstacionesPrincipales/";
		String problemFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/noproc/";
		File output = new File(outputFolder);
		if(!output.exists()){
			if(output.mkdirs())
				System.out.println("Output folder created: "+outputFolder);
		}
		output = new File(problemFolder);
		if(!output.exists()){
			if(output.mkdirs())
				System.out.println("'Problems' folder created: "+outputFolder);
		}
		
		
		File folder = new File(inputFolder);
		File[] listOfFiles = folder.listFiles();
		Arrays.sort(listOfFiles, new FileComparator());
		ArrayList<File> fileBuff=null;
		String name,id,prevId="";
		boolean process = false;
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  name = listOfFiles[i].getName();
	    	  if(!name.startsWith("~$")&&name.endsWith(".xlsm")){
	    		  id = name.substring(0, name.indexOf("_"));
	    		  if(prevId.compareTo(id)==0){
	    			  fileBuff.add(listOfFiles[i]);
	    		  }
	    		  else{
	    			  if(fileBuff!=null){
	    				  Station station = new Station(fileBuff);
	    				  station.process(noDirection);
	    				  try {
							station.average();
							station.aggregate();
							station.dump(new File(outputFolder+prevId+".csv"));
						} catch (JSONException e) {
							e.printStackTrace();
						} 
	    			  }
	    			  prevId=id;
	    			  fileBuff=new ArrayList<File>();
	    			  fileBuff.add(listOfFiles[i]);
	    			  
	    		  }
	    		  
	    	  }
	      } 
	    }
	    //To process the last file if any
	    if(fileBuff!=null){
			  Station station = new Station(fileBuff);
			  station.process(noDirection);
			  try {
				station.average();
				station.aggregate();
				station.dump(new File(outputFolder+prevId+".csv"));
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		  }
		
		
		
	}

}
