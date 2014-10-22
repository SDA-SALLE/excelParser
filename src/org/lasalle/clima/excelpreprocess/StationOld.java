package org.lasalle.clima.excelpreprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author acastillo
 *
 */
public class StationOld {
	private boolean noDirection = true;
	private static boolean DEBUG = false;
	private static String[] columnNames= new String[]{"FECHA DE TOMA DE INFORMACI?N EN FORMATO DD/MM/AAA",
					"VIA  ESPECIFICA DONDE SE EFECTUO LA TOMA DE INFORMACI?N",
					"LOCALIZACION ESPECIFICA DONDE SE EFECTUO LA TOMA DE INFORMACI?N",
					"PER?ODO DE CONTEO DE 15 MINUTOS IDENTIFICADO CON LA HORA HORA INICIAL DEL FORMATO GENERAL",
					"ACCESOS A LOS FLUJOS VEHICULARES",
					"AUTOMOVILES",
					"COLECTIVOS",
					"",
					"BUSETA/BUSETON",
					"BUSES",
					"",
					"ALIMENTADOR",
					"",
					"ARTICULADO",
					"",
					"BIARTICULADOS",
					"BUS ESPECIAL",
					"BUS INTERMUNICIPAL",
					"CAMIONES DE 2 EJES PEQUE?O",
					"CAMIONES DE 2 EJES GRANDE",
					"CAMIONES DE 3 Y 4 EJES",
					"",
					"CAMIONES DE 5 EJES",
					"CAMIONES DE MAS DE 5 EJES",
					"MOTOS",
					"BICICLETAS",
					"OBSERAVCIONES REFERIDAS SOLAMENTE A LA TOMA DE INFORMACI?N",
					"OBSERVACIONES REFERIDAS AL AN?LISIS DEL COMPORTAMIENTO"};
	
	
	
	private JSONObject sheet;
	private String place = "";
	private String latitude="";
	private String longitude="";
	private static final String NEWLINE = "\r\n"; 
	
	
	private ArrayList<File> inputFiles;
	/**
	 * 
	 * @param inputFiles
	 */
	public StationOld(ArrayList<File> inputFiles){
		this.inputFiles=inputFiles;
		sheet = new JSONObject();
	}
	
	public boolean process(boolean noDirection){
		//System.out.println("On process");
		this.noDirection=noDirection;
		for(File file:inputFiles){
			//System.out.println("file: "+file.getName());
			FileInputStream fileIS=null;
			try {
				fileIS = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Get the workbook instance for XLS file 
			XSSFWorkbook workbook=null;
			try {
				workbook = new XSSFWorkbook (fileIS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Get first sheet from the workbook
			if(place.length()==0&&latitude.length()==0){
				XSSFSheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				for(int i=0;i<12;i++)
					rowIterator.next();
				Row row = rowIterator.next();
				//For each row we will read the info
				Iterator<Cell> cellIterator = row.cellIterator();
				for(int i=0;i<21;i++)
					cellIterator.next();
				longitude = cellIterator.next().getStringCellValue()
						.replace(",", ".");//.replaceAll("[^\\x00-\\x7F]", "$");
				row = rowIterator.next();
				//For each row we will read the info
				cellIterator = row.cellIterator();
				for(int i=0;i<21;i++)
					cellIterator.next();
				latitude = cellIterator.next().getStringCellValue()
						.replace(",", ".");//.replaceAll("[^\\x00-\\x7F]", "$");
			}
			//System.out.println(longitude+" "+latitude);
			//Get second sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(1);
			//Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = sheet.iterator();
			
			//Check if this sheet has the standard format
			if(checkFormat(rowIterator)){
				try{
					//rowIterator.next();
					addSheet(rowIterator);
				}
				catch(Exception e){
					System.out.println("Problems importing the sheet. "+file.getName());
					e.printStackTrace();
				}
				
			}else{
				//Let's inform that this file was not processed.
				System.out.println("Fiel was not processed: "+file.getName());
			}
			
		}	
		return true;
	}
	private void addSheet(Iterator<Row> rowIterator) throws IllegalStateException{
		
		rowIterator.next();//Ignore the column names. We actually know it.
		Row row = null;

		//System.out.println("On addSheet "+sheet.length());
		//For each row we will read the info. 
		while((row = rowIterator.next())!=null){
			Iterator<Cell> cellIterator = row.cellIterator();
			cellIterator.next();//FECHA
			if(place.length()==0){
				place=cellIterator.next().getStringCellValue();//VIA
				place+="_X_"+cellIterator.next().getStringCellValue();//LOCALIZACION
			}
			else{
				cellIterator.next();//VIA
				cellIterator.next();//LOCALIZACION
			}

			//We start at PERIODO field.
			int period = 0;
			//try{
				//String val = cellIterator.next().getStringCellValue();
				//System.out.println(val);
				period = (int)cellIterator.next().getNumericCellValue();//PERIODO
			//}catch(IllegalStateException e){
			//	e.printStackTrace();
			//}

			String direction = null;
			Cell dir = cellIterator.next();
			try{
				direction = ""+(int)dir.getNumericCellValue();//SENTIDO
			}
			catch(Exception e){
				direction=dir.getStringCellValue();
			}
			//We recognize that the data has end because the direction gives 0
			if(direction.compareTo("0")==0)
				return;
			if(noDirection)
				direction="0";
			JSONArray rowData = new JSONArray();
			int index=0;
			while(cellIterator.hasNext()){
				try {
					rowData.put(index, cellIterator.next().getNumericCellValue());
				} catch (Exception e) {
					try {rowData.put(index,0);} catch (JSONException e1) {}
				}
				index++;
			}
			try {
				if(sheet.has(direction)){
						JSONObject infoRow = sheet.getJSONObject(direction);
						infoRow.accumulate(period+"", rowData);
				}
				else{
					JSONObject infoRow = new JSONObject();
					infoRow.append(period+"", rowData);
					sheet.put(direction, infoRow);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * This function will check for the names in the 3rd row. It will compare it with the standar format. 
	 * Actually I only know how to process those cases. 
	 * @param rowIterator
	 * @return
	 */
	private boolean checkFormat(Iterator<Row> rowIterator) {
		//Get iterator to all cells of current row
		rowIterator.next();
		Iterator<Cell> cellIterator = rowIterator.next().cellIterator();
		int index = 0;
		while(cellIterator.hasNext()){
			String value = cellIterator.next().getStringCellValue();
			if(value.replaceAll("[^\\x00-\\x7F]", "?").compareTo(columnNames[index++])!=0){
				return false;
			}
		}
		//rowIterator.next();
		//rowIterator.next();
		return true;
	}
	/**
	 * This function will aggregate all the rows with the same identifiers: PERIODO and SENTIDO
	 * @throws JSONException 
	 */
	public void average() throws JSONException{
		Iterator<String> keysDirection = sheet.keys();
		String keyD = null;
		while(keysDirection.hasNext()){
			keyD=keysDirection.next();
			
			//if(DEBUG)System.out.println(keyD);
			
			JSONObject direction = sheet.getJSONObject(keyD);
			Iterator<String> keyPeriod = direction.keys();
			String keyP = null;
			while(keyPeriod.hasNext()){
				keyP= keyPeriod.next();
				
				//if(DEBUG)System.out.println(keyP);
				
				JSONArray dataPeriod = direction.getJSONArray(keyP);
				JSONArray row0 = dataPeriod.getJSONArray(0);
				//Sum on the first element
				int n = 1,diff=0;
				for(int i=1;i<dataPeriod.length();i++){
					JSONArray rowi = dataPeriod.getJSONArray(i);
					n++;
					diff=row0.length()-rowi.length();
					if(diff>=0){
						for(int k=row0.length()-1-diff;k>=0;k--){
							row0.put(k, row0.getDouble(k)+rowi.getDouble(k));
						}
					}
					else{
						for(int k=row0.length()-1;k>=0;k--){
							row0.put(k, row0.getDouble(k)+rowi.getDouble(k));
						}
						for(int k=0;k<diff;k++){
							row0.put(row0.length()+k,rowi.getDouble(row0.length()+k));
						}
					}
				}
				//Average on the first element
				for(int k=row0.length()-1;k>=0;k--){
					row0.put(k, row0.getDouble(k)/n);
				}
				//Remove the others rows
				for(int i=dataPeriod.length()-1;i>0;i--){
					dataPeriod.remove(i);
				}
			}
			
			
		}
	}
	/**
	 * This function add all the rows within the same hour.
	 * @throws JSONException
	 */
	public void aggregate() throws JSONException{
		Iterator<String> keysDirection = sheet.keys();
		String keyD = null;
		while(keysDirection.hasNext()){
			keyD=keysDirection.next();
			
			if(DEBUG)System.out.println(keyD);
			
			JSONObject direction = sheet.getJSONObject(keyD);
			Iterator<String> keyPeriod = direction.keys();
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			while(keyPeriod.hasNext())
				tmp.add(Integer.parseInt(keyPeriod.next()));
			Integer[] keyArray=new Integer[tmp.size()];
			keyArray = tmp.toArray(keyArray);
			Arrays.sort(keyArray);
			//We need to sort the keys before to aggregate it
			int prevInt = -1;
			int thisInt = 0;
			int diff;
			JSONArray hourData = null;
			JSONArray tmpData = null;
			for(Integer keyP:keyArray){
				thisInt = keyP/100;
				if(thisInt!=prevInt){
					prevInt=thisInt;
					hourData=direction.getJSONArray(keyP.toString()).getJSONArray(0);
				}else{
					tmpData=direction.getJSONArray(keyP.toString()).getJSONArray(0);
					
					diff=hourData.length()-tmpData.length();
					if(diff>=0){
						for(int k=hourData.length()-1-diff;k>=0;k--){
							hourData.put(k, hourData.getDouble(k)+tmpData.getDouble(k));
						}
					}
					else{
						for(int k=hourData.length()-1;k>=0;k--){
							hourData.put(k, hourData.getDouble(k)+tmpData.getDouble(k));
						}
						for(int k=0;k<diff;k++){
							hourData.put(hourData.length()+k,hourData.getDouble(tmpData.length()+k));
						}
					}
					//We remove the data we already summarized.
					direction.remove(keyP.toString());
				}
			}
		}
		
	}
	
	/**
	 * This function save the the workBook in a file.
	 * @param output
	 * @throws JSONException
	 * @throws FileNotFoundException 
	 */
	public void dump(File output) throws JSONException{
		StringBuffer fileContent = new StringBuffer();
		Iterator<String> keysDirection = sheet.keys();
		String keyD = null;
		//System.out.println("KK "+ keysDirection.hasNext());
		while(keysDirection.hasNext()){
			keyD=keysDirection.next();

			JSONObject direction = sheet.getJSONObject(keyD);
			Iterator<String> keyPeriod = direction.keys();
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			while(keyPeriod.hasNext())
				tmp.add(Integer.parseInt(keyPeriod.next()));
			Integer[] keyArray=new Integer[tmp.size()];
			keyArray = tmp.toArray(keyArray);
			Arrays.sort(keyArray);
			//We need to sort the keys before to aggregate it
			//System.out.println("tmp "+tmp);
			JSONArray hourData = null;
			for(Integer keyP:keyArray){
				hourData=direction.getJSONArray(keyP.toString()).getJSONArray(0);
				fileContent.append(place+","+latitude+","+longitude+","+keyD+","+keyP+","+hourData.join(",").replace("[","").replace("]","")+NEWLINE);
			}
		}
		FileWriter writer;
		try {
			writer = new FileWriter(output);
			writer.write(fileContent.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
