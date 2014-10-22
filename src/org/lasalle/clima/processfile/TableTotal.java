package org.lasalle.clima.processfile;

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
	private int cellStartCopy;
	private int nbCells;
	private String[] labels;
	private static String NEWLINE = "\r\n";
	private static String DELIMITER = ",";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TableTotal(int cellKey, int cellStartCopy,int nbCells){
		super();
		this.cellKey=cellKey;
		this.cellStartCopy=cellStartCopy;
		this.nbCells=nbCells;
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
		while(rowIterator.hasNext()){
			//System.out.println(k++);
			row = rowIterator.next();
			int cellID =(int)row.getCell(this.cellKey).getNumericCellValue();
			
			if(this.containsKey(cellID)){
				buffer = this.get(cellID);
			}
			else{
				buffer = new double[nbCells];
				for(int i=buffer.length-1;i>=0;i--)
						buffer[i]=0;
				this.put(cellID, buffer);
			}
			for(int i=nbCells-1;i>=0;i--){
				buffer[i]+=row.getCell(cellStartCopy+i).getNumericCellValue();
			}
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
	    	writer.append (DELIMITER+label);
	    }
	    writer.append(NEWLINE);
		
	    //Now, write the content of the tree
	    Set<Integer> keys = this.keySet();
	    for(Integer key:keys){
	    	writer.append(key+"");
	    	double[] datas = this.get(key);
	    	for(int i=0;i<datas.length;i++){
	    		writer.append(DELIMITER+datas[i]);
	    	}
	    	writer.append(NEWLINE);
	    }
	    
	    writer.flush();
	    writer.close();
	}

}
