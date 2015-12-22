package org.lasalle.clima.excel.bottomuphour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lasalle.clima.excel.bottomuphour.scripting.BottomUpAnalysis;
/**
 * 
 * @author acastillo
 *
 */
public class Aggregator {
	private XSSFSheet cells;
	private XSSFSheet infoFlows;
	private XSSFSheet factors;
	
	private XSSFWorkbook cellsWorkbook;
	
	private File cellsFile;
	private File infoFlowsFile;
	private File factorsFile;
	private File outputModel;
	
	//private OutputSheet[] outputSheets;
	
	private static final int CELLQUERY=11;
	private static final int CELL_KEY=7;
	private static final int CELLMATCHQUERY=21;
	private static final int CELLMATCHKEY=2;
	private static final int CELLHOUR = 5;
	private static final int CELLDAYTYPE=3;
	private static final int CELL_FID_GRID=0;
	private static final int CELL_LONGUITUDE=3;
	private static final int CELL_POLLUTANT=0;
	private static final int CELL_TOTAL_VALOR=88;
	private static final int CELL_TOTAL_INCER=89;
	private static final int CELL_TIPO_VIA=8;
	private TreeMap<Double,Double> table=null;//It will contain [query,key] pairs
	private TreeMap<String,double[]> tableValues = null;//It will contain the flows info. Key: [newKey+type+hour]
	private TreeMap<String,double[]> factorsTable = null;//It will contain the flows info. Key: [newKey+type+hour]
	private TableTotal totalsTable = null;
	private TableTotalHora totalsTableHora = null;
	private TablaCeldaHora[] totalsCeldaHora = null;
	private LonguitudeAndFlows mainTable = null;
	private String[] types = null;
	private BottomUpAnalysis logger;
	private static int nHours = 24;//52
	
	/**
	 * Constructor.
	 * @param cells
	 * @param infoFlows
	 * @param factos
	 */
	public Aggregator(File cellsFile, File infoFlowsFile, File factorsFile, File outputModel){
		//this.logger.appendError("dsdsd", "kkkkkkkkk");
		this.cellsFile=cellsFile;
		this.infoFlowsFile=infoFlowsFile;
		this.factorsFile=factorsFile;
		cells = openFirstSheet(cellsFile);
		mainTable=new LonguitudeAndFlows(cells);
		this.cellsWorkbook=cells.getWorkbook();
		infoFlows = openFirstSheet(this.infoFlowsFile);
		factors = openFirstSheet(this.factorsFile);
		table = fillTable(CELLMATCHQUERY, CELLMATCHKEY, infoFlows);
		tableValues = readFlowInfo(infoFlows);
		
		this.outputModel=outputModel;
		
		//Read and store the emission factors
		factorsTable = readFactorsInfo(factors);
	}
	public Aggregator(File cellsFile, File infoFlowsFile, File factorsFile, File outputModel,
			BottomUpAnalysis bottomUpAnalysis) {
		//System.out.println("Hereee");
		this.logger = bottomUpAnalysis;
		//System.out.println("Hereee 2");
		//this.logger.appendError("dsdsd", "kkkkkkkkk");
		this.cellsFile=cellsFile;
		this.infoFlowsFile=infoFlowsFile;
		this.factorsFile=factorsFile;
		cells = openFirstSheet(cellsFile);
		mainTable=new LonguitudeAndFlows(cells);
		this.cellsWorkbook=cells.getWorkbook();
		infoFlows = openFirstSheet(this.infoFlowsFile);
		factors = openFirstSheet(this.factorsFile);
		table = fillTable(CELLMATCHQUERY, CELLMATCHKEY, infoFlows);
		tableValues = readFlowInfo(infoFlows);
		
		this.outputModel=outputModel;
		
		//Read and store the emission factors
		factorsTable = readFactorsInfo(factors);
	}
	/**
	 * This function read the information stored in the flow information excel file.
	 * @param sheet
	 * @return A TreeMap<String,double[16]>
	 */
	private TreeMap<String, double[]> readFlowInfo(XSSFSheet sheet) {
		Iterator<Row> rowIterator = sheet.iterator();
		
		rowIterator.next();//Ignore the column names. We actually know it.
		Row row = null;
		TreeMap<String, double[]> treeMap = new TreeMap<String, double[]>();

		while(rowIterator.hasNext()){
			row = rowIterator.next();
			//The composed key: [newID+type.firstLetter+hour]
			String key =Math.round(row.getCell(CELLMATCHKEY).getNumericCellValue())+
					row.getCell(CELLDAYTYPE).getStringCellValue().substring(0, 1)+
					Math.round(row.getCell(CELLHOUR).getNumericCellValue());
			//System.out.println(key);
			double[] values = new double[16];
			
			for(int i=0;i<16;i++){
				values[i]=row.getCell(i+6).getNumericCellValue();
			}
			
			treeMap.put(key,values);
		}
		return treeMap;
	}
	/**
	 * This function reads the pollutant info and store it in a treeMap
	 * @param sheet
	 * @return
	 */
	private TreeMap<String, double[]> readFactorsInfo(XSSFSheet sheet) {
		Iterator<Row> rowIterator = sheet.iterator();
		
		rowIterator.next();//Ignore the column names. We actually know it.
		rowIterator.next();//Ignore second column
		Row row = null;
		TreeMap<String, double[]> treeMap = new TreeMap<String, double[]>();

		while(rowIterator.hasNext()){
			try{
				row = rowIterator.next();
				//The composed key: [newID+type.firstLetter+hour]
				String key =row.getCell(CELL_POLLUTANT).getStringCellValue();
				//System.out.println(key);
				double[] values = new double[30];
				
				for(int i=0;i<30;i++){
					values[i]=row.getCell(i+1).getNumericCellValue();
					//System.out.println(values[i]);
				}
				
				treeMap.put(key,values);
			}
			catch(Exception e){
				break;
			}
		}
		return treeMap;
	}

	/**
	 * This function will add the station ID to the flows table, by matching
	 * the columns Total, from both tables at 8:00 am.
	 */
	/*public void addNewIDtoCells() {
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIteratorA = cells.iterator();
		
		rowIteratorA.next();//Ignore the column names. We actually know it.
		Row row = null;
		int previousValue = -1;
		double key = -1;
		//For each row we will read the info. 12
		while(rowIteratorA.hasNext()){
			row = rowIteratorA.next();
			double currentValue = Math.round(row.getCell(CELLQUERY).getNumericCellValue()*100)/100;
			//System.out.println(i++);
			if(currentValue!=previousValue){
				if(table.containsKey(currentValue))
					key=table.get(currentValue);
				else
					key=-1;
			}
			row.createCell(CELL_KEY);
			row.getCell(CELL_KEY).setCellValue(key);
			if(key==-1){
				System.err.println("Error: Value "+currentValue+" was not found in the matching table");
			}
		}
		FileOutputStream dataFileOut;
		try {
			dataFileOut = new FileOutputStream(
					cellsFile.getAbsolutePath().replace(".xlsx", "out.xlsx"));
			//System.out.println(dataFileOut+" "+cellsWorkbook);
			
			cellsWorkbook.write(dataFileOut);
			dataFileOut.flush();
			dataFileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	
	/**
	 * This function stores all the pairs [newID,total] in a treeMap.
	 * @param columnIndex
	 * @param keyIndex
	 * @param sheet
	 * @return TreeMap<Double,Double>
	 */
	private TreeMap<Double,Double> fillTable(int columnIndex, int keyIndex, XSSFSheet sheet){
		Iterator<Row> rowIterator = sheet.iterator();
		
		rowIterator.next();//Ignore the column names. We actually know it.
		Row row = null;
		TreeMap<Double,Double> treeMap = new TreeMap<Double,Double>();
		
		while(rowIterator.hasNext()){
			row = rowIterator.next();
			//System.out.println(i++);
			if(row.getCell(CELLDAYTYPE).getStringCellValue().startsWith("H")
					&&row.getCell(CELLHOUR).getNumericCellValue()==800){
				treeMap.put((double)(Math.round(row.getCell(columnIndex).getNumericCellValue()*100)/100),
						row.getCell(keyIndex).getNumericCellValue());
			}
		}
		return treeMap;
	}
	
	/**
	 * This function open the excel file and return its first sheet.
	 * @param file
	 * @return XSSFSheet
	 */
	private XSSFSheet openFirstSheet(File file){
		
		FileInputStream fileIS=null;
		try {
			fileIS = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.appendError("Aggregator:openFirstSheet", e.toString());
			e.printStackTrace();
		}
		//Get the workbook instance for XLS file
		try {
			//System.out.println("Hereee 6"+fileIS.toString());
			XSSFWorkbook workbook = new XSSFWorkbook (fileIS);

			//System.out.println("Hereee 4");
			return workbook.getSheetAt(0);
		} catch (IOException e) {
			logger.appendError("Aggregator:openFirstSheet", e.toString());
			e.printStackTrace();

			//System.out.println("Hereee 5");
			return null;
		}
	}
	
	/**
	 * This function expand the cells in various files.
	 */
	public void expandCells() {
		//this.logger.appendError("sheeeeeet", "Aqui");
		//For the first pollutant create a new set of files
		String[] pollutants = factorsTable.keySet().toArray(new String[1]);
		
		totalsCeldaHora = new TablaCeldaHora[pollutants.length];
		for(int k=pollutants.length-1;k>=0;k--){
			totalsCeldaHora[k]=new TablaCeldaHora(0, CELL_TOTAL_VALOR, CELL_TOTAL_INCER);
		}
		//this.logger.appendError("sheeeeeet2", "Aqui");
		//Now, for each row, we need to expand the total flow for each 
		//type of day and for each hour of the day.
		double[] factorsRow = null;
		String key = null;
		for(String type:types){
			totalsTable = new TableTotal(0, CELL_TOTAL_VALOR, CELL_TOTAL_INCER);
			totalsTable.setLabels(pollutants);
			totalsTableHora = new TableTotalHora(0, CELL_TOTAL_VALOR, CELL_TOTAL_INCER);
			totalsTableHora.setLabels(pollutants);
			for(int i=0;i<nHours;i++){	
				key = pollutants[0];
				factorsRow = factorsTable.get(key);
				//Open the output file
				OutputSheet outputSheet =new OutputSheet(outputModel);
				outputSheet.setPost("_"+key+"_"+type+"_"+(i*100));
				
				int nRows = mainTable.getnRows();
				//System.out.println(nRows);
				for(int k=0;k<nRows;k++){
					//System.out.println(k+"/"+nRows);
					double sharedKey = mainTable.values[0][k];//row.getCell(CELL_KEY).getNumericCellValue();
					int fidGrid = (int)mainTable.values[1][k];//(int)row.getCell(CELL_FID_GRID).getNumericCellValue();
					//double total = row.getCell(CELLQUERY).getNumericCellValue();
					double longitude = mainTable.values[2][k];//row.getCell(CELL_LONGUITUDE).getNumericCellValue();
					String tipoVia =  mainTable.tiposVias[k];//row.getCell(CELL_TIPO_VIA).getStringCellValue();
					
					String fullKey = Math.round(sharedKey)+type+i*100;
					double[] values = null;
					if(tableValues.containsKey(fullKey)){
						//System.out.println("Contains "+fullKey);
						values = tableValues.get(fullKey);
						outputSheet.push(values, sharedKey, fidGrid, longitude, tipoVia);
					}
					/*else{
						System.out.println("x "+fullKey);
					}*/
				}

				outputSheet.replaceFactors(factorsRow);
				try {
					outputSheet.save();
				} catch (IOException e1) {
					logger.appendError("Aggregator:expandCells","Failed saving the file "+key);
					e1.printStackTrace();
				}
				//Update the total
				//System.out.println("Rows "+outputSheet.getRows()+" "+key);
				totalsTable.updateTotals(outputSheet.getData(), outputSheet.getRows(), 0);
				totalsTableHora.updateTotals(outputSheet.getData(), outputSheet.getRows(), 0,i);
				totalsCeldaHora[0].updateTotals(outputSheet.getData(), outputSheet.getRows(), 0,i);
				
				//Now, for each other pollutants a new set of files have to be created
				for(int k=pollutants.length-1;k>0;k--){
					key=pollutants[k];
					factorsRow = factorsTable.get(key);
					outputSheet.setPost("_"+key+"_"+type+"_"+(i*100));
					outputSheet.replaceFactors(factorsRow);
					try {
						outputSheet.save();
					} catch (IOException e) {
						logger.appendError("Aggregator:expandCells","Failed saving the file "+key);
						e.printStackTrace();
					}
					
					//Update the total
					totalsTable.updateTotals(outputSheet.getData(), outputSheet.getRows(), k);
					totalsTableHora.updateTotals(outputSheet.getData(), outputSheet.getRows(), k,i);
					totalsCeldaHora[k].updateTotals(outputSheet.getData(), outputSheet.getRows(), k,i);
				}
			}
			try {
				totalsTable.save(new File(outputModel.getAbsolutePath().replace("output.xlsx", "outputTotals_"+type+".csv")));
				totalsTableHora.save(new File(outputModel.getAbsolutePath().replace("output.xlsx", "outputTotalsHora_"+type+".csv")));
				
			} catch (IOException e) {
				logger.appendError("Aggregator:expandCells","The totals file could not be saved");
				e.printStackTrace();
			}
		}
		try {
			for(int k=pollutants.length-1;k>=0;k--){
				totalsCeldaHora[k].save(new File(outputModel.getAbsolutePath().replace("output.xlsx", "gridCeldaHora"+pollutants[k]+".csv")));
			}	
		} catch (IOException e) {
			this.logger.appendError("sheeeeeet2", "Error saving the gridXCellXHour totals");
			System.out.println("Error saving the gridXCellXHour totals");
			e.printStackTrace();
		}
		
	}
	public String[] getTypes() {
		return types;
	}
	public void setTypes(String[] types) {
		this.types = types;
	}
	public void setLogger(BottomUpAnalysis bottomUpAnalysis) {
		this.logger = bottomUpAnalysis;
		
	}
}
