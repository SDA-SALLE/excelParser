package org.lasalle.clima.processfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
/**
 * 
 * @author acastillo
 *
 */
public class Aggregator {
	private XSSFSheet cells;
	private XSSFSheet infoFlows;
	
	private XSSFWorkbook cellsWorkbook;
	
	private File cellsFile;
	private File infoFlowsFile;
	private File outputModel;
	
	//private OutputSheet[] outputSheets;
	
	private static final int CELLQUERY=6;
	private static final int CELLKEY=8;
	private static final int CELLMATCHQUERY=21;
	private static final int CELLMATCHKEY=2;
	private static final int CELLHOUR = 5;
	private static final int CELLDAYTYPE=3;
	private static final int CELL_FID_GRID=0;
	private static final int CELL_LONGUITUDE=3;
	private static final int CELL_POLLUTANT=0;
	private static final int CELL_CLASS=7;
	private TreeMap<Double,Double> table=null;//It will contain [query,key] pairs
	private TreeMap<String,double[]> tableValues = null;//It will contain the flows info. Key: [newKey+type+hour]
	private TableTotal totalsTable = null;
	private static String[] types ={"F"};
	private static String[] labels = {"AUT","MIB","BT","BUS","AL","AT","BA","ESP","INT","C2P","C2G","C3C4","C5E","SC5E","MOTO"};
	private static String[] tipoVia = {"Secundaria"}; 
	private static int nHours = 24;//52
	
	/**
	 * Constructor.
	 * @param cells
	 * @param infoFlows
	 * @param factos
	 */
	public Aggregator(File cellsFile, File infoFlowsFile, File outputModel){
		this.cellsFile=cellsFile;
		this.infoFlowsFile=infoFlowsFile;
		this.cells = openFirstSheet(this.cellsFile);
		this.cellsWorkbook=cells.getWorkbook();
		this.infoFlows = openFirstSheet(this.infoFlowsFile);
		
		table = fillTable(CELLMATCHQUERY, CELLMATCHKEY, infoFlows);
		tableValues = readFlowInfo(infoFlows);
		
		this.outputModel=outputModel;
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
			row = rowIterator.next();
			//The composed key: [newID+type.firstLetter+hour]
			String key =row.getCell(CELL_POLLUTANT).getStringCellValue();
			System.out.println(key);
			double[] values = new double[30];
			
			for(int i=0;i<30;i++){
				values[i]=row.getCell(i+1).getNumericCellValue();
			}
			
			treeMap.put(key,values);
		}
		return treeMap;
	}

	/**
	 * This function will add the station ID to the flows table, by matching
	 * the columns Total, from both tables at 8:00 am.
	 */
	public void addNewIDtoCells() {
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
			row.createCell(CELLKEY);
			row.getCell(CELLKEY).setCellValue(key);
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
		
	}
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Get the workbook instance for XLS file
		try {
			XSSFWorkbook workbook = new XSSFWorkbook (fileIS);
			return workbook.getSheetAt(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * This function expand the cells in various files.
	 */
	public void expandCells() {
		//Now, for each row, we need to expand the total flow for each 
		//type of day and for each hour of the day.
		OutputSheet outputSheet;
		double[] values = null;
		for(String key:tipoVia){
			for(String type:types){
				totalsTable = new TableTotal(0, 23, 15);
				totalsTable.setLabels(labels);
				for(int i=0;i<nHours;i++){	
					System.out.println(key+" "+type+" "+i);
					//Open the output file
					outputSheet =new OutputSheet(outputModel);
					outputSheet.setPost("_"+key+"_"+type+"_"+(i*100));
					
					//Get iterator to all the rows in current sheet
					Iterator<Row> rowIterator = cells.iterator();
					rowIterator.next();//Ignore the column names. We actually know it.
					Row row = null;//The current cell
					int k=0;
					while(rowIterator.hasNext()){
						System.out.println(k++);
						row = rowIterator.next();
						String routeType = row.getCell(CELL_CLASS).getStringCellValue();
						//Only the rows with the same route type are processed here.
						if(routeType.compareTo(key)==0){
							double sharedKey = row.getCell(CELLKEY).getNumericCellValue();
							int fidGrid = (int)row.getCell(CELL_FID_GRID).getNumericCellValue();
							//double total = row.getCell(CELLQUERY).getNumericCellValue();
							double longitude = row.getCell(CELL_LONGUITUDE).getNumericCellValue();
							
							String fullKey = Math.round(sharedKey)+type+i*100;
							values = null;
							if(tableValues.containsKey(fullKey)){
								values = tableValues.get(fullKey);
								outputSheet.push(values, fidGrid, longitude);
							}
						}
					}
					outputSheet.save();
					//Update the total
					totalsTable.updateTotals(outputSheet.getSheet(), 0);
				}
				try {
					totalsTable.save(new File(outputModel.getAbsolutePath().replace("output.xlsx", "outputTotals_"+type+"_"+key+".csv")));
				} catch (IOException e) {
					System.out.println("The totals file could not be saved");
					e.printStackTrace();
				}
			}
		}
		
	}
}
