package org.lasalle.clima.excel.bottomuphour;

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
public class TableTotalHora extends TreeMap<Integer,double[]>{
	
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

	public TableTotalHora(int cellKey, int cellTotal,int cellStd){
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
	public void updateTotals(double[][] sheet, int rows, int index, int hour){
		double[] row; 
		double[] buffer;
		int cellID = hour;//(int)row[this.cellKey];
		for(int k=0;k<rows;k++){
			//System.out.println(k++);
			row = sheet[k];
			
			//System.out.println(cellID);
			double total = row[this.cellTotal];
			double std = row[this.cellStd];
			
			if(this.containsKey(cellID)){
				buffer = this.get(cellID);
			}
			else{
				buffer = new double[nPairs*2];
				for(int i=buffer.length-1;i>=0;i--)
						buffer[i]=0;
				this.put(cellID, buffer);
			}
			buffer[index*2]+=total;
			buffer[index*2+1]+=std;
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
		writer.append("Hora");
	    for(String label:labels){
	    	writer.append (DELIMITER+label+"_Valor"+DELIMITER+label+"_Incertidumbre");
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
