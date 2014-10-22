package org.lasalle.clima.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
/**
 * 
 * @author acastillo
 *
 */
public class TableTotal extends TreeMap<Integer,double[]>{
	
	private int cellKey;
	private int cellTotal;
	private int cellStd;
	private String[] labels;
	private static String NEWLINE = "\r\n";
	private static String DELIMITER = ",";
	private final static int nPairs = 6;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TableTotal(int cellKey, int cellTotal,int cellStd){
		super();
		this.cellKey=cellKey;
		this.cellTotal=cellTotal;
		this.cellStd=cellStd;
		this.labels=null;
	}
	
	
	public void setLabels(String[] labels){
		this.labels=labels;
	} 
	/**
	 * It adds the the values on the given sheet to the total values
	 * stored in the TreeMap 
	 * @param sheet
	 * @param index
	 */
	public void updateTotals(XSSFSheet sheet, int index){
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = sheet.iterator();
		rowIterator.next();//Ignore the column names. We actually know it.
		rowIterator.next();//Ignore the column names. We actually know it.
		rowIterator.next();//Ignore the column names. We actually know it.
		Row row = null;//The current cell
		double[] buffer = null; 
		int cellIDBef = -1;
		double longuitudeTotal = 0;
		double longuitude;
		while(rowIterator.hasNext()){
			//System.out.println(k++);
			row = rowIterator.next();
			int cellID =(int)row.getCell(this.cellKey).getNumericCellValue();
			double total = row.getCell(this.cellTotal).getNumericCellValue();
			double std = row.getCell(this.cellStd).getNumericCellValue();
			longuitude = row.getCell(17).getNumericCellValue();
			if(cellIDBef==cellID)
				longuitudeTotal+=longuitude;
			else
				longuitudeTotal=longuitude;
			
			if(this.containsKey(cellID)){
				buffer = this.get(cellID);
			}
			else{
				buffer = new double[nPairs*2+1];
				for(int i=buffer.length-2;i>=0;i--)
						buffer[i]=0;
				this.put(cellID, buffer);
			}
			buffer[index*2]+=total*longuitude;
			buffer[index*2+1]+=std*longuitude;
			buffer[buffer.length-1]+=longuitude;
			
			cellIDBef=cellID;
		}
	}
	/**
	 * To save the content of this tree map on a CSV file
	 * @param file
	 * @throws IOException
	 */
	public void save(File file) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    //Write the header of the file
		writer.append("Celda");
	    for(String label:labels){
	    	writer.append (DELIMITER+label+"_Valor"+DELIMITER+label+"_Incertidumbre");
	    }
	    writer.append(NEWLINE);
		
	    //Now, write the content of the tree
	    Set<Integer> keys = this.keySet();
	    for(Integer key:keys){
	    	writer.append(key+"");
	    	double[] datas = this.get(key);
	    	double longuitude = datas[datas.length-1];
	    	for(int i=0;i<datas.length-1;i++){
	    		writer.append(DELIMITER+datas[i]/longuitude);
	    	}
	    	writer.append(NEWLINE);
	    }
	    
	    writer.flush();
	    writer.close();
	}

}
