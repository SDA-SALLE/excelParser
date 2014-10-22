package org.lasalle.clima.excel.buttomuphour;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author acastillo
 *
 */
public class TablaCeldaHora extends TreeMap<Integer,double[]>{
	
	private int cellKey;
	private int cellTotal;
	private int cellStd;
	private String[] labels;
	private static String NEWLINE = "\r\n";
	private static String DELIMITER = ",";
	private final static int nHours = 24;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TablaCeldaHora(int cellKey, int cellTotal,int cellStd){
		super();
		this.cellKey=cellKey;
		this.cellTotal=cellTotal;
		this.cellStd=cellStd;
		this.labels=null;
	}
	
	/**
	 * It adds the the values on the given sheet to the total values
	 * stored in the TreeMap 
	 * @param sheet
	 * @param index
	 */
	public void updateTotals(double[][] sheet, int rows, int index, int hour){
		double[] row; 
		double[] buffer;
		for(int k=0;k<rows;k++){
			//System.out.println(k++);
			row = sheet[k];
			int cellID =(int)row[this.cellKey];
			//System.out.println(cellID);
			double total = row[this.cellTotal];
			
			if(this.containsKey(cellID)){
				buffer = this.get(cellID);
			}
			else{
				buffer = new double[nHours];
				for(int i=buffer.length-1;i>=0;i--)
						buffer[i]=0;
				this.put(cellID, buffer);
			}
			buffer[hour]+=total;
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
		writer.append("Celda"+DELIMITER+"Hora"+NEWLINE);
		writer.append("Fid_1");
	    for(int i=0;i<nHours;i++){
	    	writer.append (DELIMITER+i*100);
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
