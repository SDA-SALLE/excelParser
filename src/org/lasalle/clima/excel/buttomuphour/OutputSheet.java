package org.lasalle.clima.excel.buttomuphour;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TreeMap;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class OutputSheet {
	private static final int MAXROWS = 120000;
	private static final int COLS = 90;
	private static final String NEWLINE = "\r\n"; 
	private int rows = 0;
	private File file;
	private double[][] data;
	private double[] factorsRow;
	private double[] loads;
	private String post="";
	private DecimalFormat df ;
	private int currentRow;//Pointing to the current line
	private TreeMap<String,Integer> cellsTipoVia;
	String[] head ={"Celda","IDEstacion","Flujo Total","AUT","MIB","BT","BUS","AL","AT","BA","ESP","INT","C2P","C2G","C3C4",
			"C5E","SC5E","MOTO","Arterial","Intermedia","LocalResidencial","Local","Rural","Transmilenio","Valor",
			"Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre",
			"Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre",
			"Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre",
			"Suma pesada 1","Suma pesada 2","ksec","ksecf","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre",
			"Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre",
			"Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre",
			"Valor","Incertidumbre","Valor","Incertidumbre","Valor","Incertidumbre"};
	
	public OutputSheet(File file){
		this.file = file;
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
			XSSFSheet sheet = workbook.getSheetAt(0);
			loads = new double[4];
			XSSFRow row = sheet.getRow(0);
			//If it has to be in the same row
			loads[0]=row.getCell(19).getNumericCellValue();
			loads[1]=row.getCell(20).getNumericCellValue();
			loads[2]=row.getCell(21).getNumericCellValue();
			loads[3]=row.getCell(22).getNumericCellValue();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data = new double[MAXROWS][COLS];
		currentRow=-1;
		cellsTipoVia = new TreeMap<String,Integer>();
		cellsTipoVia.put("Arterial", 18);
		cellsTipoVia.put("Intermedia", 19);
		cellsTipoVia.put("LocalResidencial", 20);
		cellsTipoVia.put("Local", 21);
		cellsTipoVia.put("Rural", 22);
		cellsTipoVia.put("Transmilenio", 23);
		
		df = new DecimalFormat();
		df.setMaximumFractionDigits(5);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);
		
	}
	/**
	 * Save the content of object to a file
	 * @return
	 * @throws IOException
	 */
	public boolean save() throws IOException{
		BufferedWriter dataFileOut = new BufferedWriter(new FileWriter(file.getAbsolutePath().replace(".xlsx", post+".csv")));

		this.evaluateAllFormulaCells();
		//Save headers
		for(int k=0;k<COLS-1;k++){
			dataFileOut.write(head[k]+",");
		}
		dataFileOut.write(head[COLS-1]+NEWLINE);
		//Save data
		for(int i=0;i<rows;i++){
			for(int k=0;k<COLS-1;k++){
				dataFileOut.write(df.format(data[i][k])+",");
			}
			dataFileOut.write(df.format(data[i][COLS-1])+NEWLINE);
		}
	   
		dataFileOut.flush();  
		dataFileOut.close(); 
		
		return true;
	}
	
	public void push(double[] values, double sharedKey, int fidGrid, double longuite,String tipoVia){
		//System.out.println(fidGrid);
		if(values!=null&&values.length==16){
			double total = values[15];
			//XSSFRow row = sheet.getRow(currentRow);
			double[] row = data[0];
			//If it has to be in the same row
			if(currentRow!=-1)
				row = data[currentRow];
			if(currentRow!=-1&&row[0]==fidGrid&& row[1]==sharedKey){
					if(cellsTipoVia.containsKey(tipoVia))
						row[(cellsTipoVia.get(tipoVia))]=(longuite/1000+row[cellsTipoVia.get(tipoVia)]);
			}
			else{//A new row
				currentRow++;
				//System.out.println(currentRow);
				row = data[currentRow];
				
				row[0]=fidGrid;
				row[1]=sharedKey;
				row[2]=total;
				for(int i=0;i<values.length-1;i++){
					row[i+3]=values[i];
				}
				for(int i=18;i<24;i++)
					row[i]=0;
				if(cellsTipoVia.containsKey(tipoVia))
					row[cellsTipoVia.get(tipoVia)]=longuite/1000;
			}
		}
		rows = currentRow+1;
		//System.out.println(rows);
	}
	
	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public void replaceFactors(double[] factorsRow) {
		double[] row;
		for(int k=rows-1;k>=0;k--){
			row = data[k];
			for(int i=factorsRow.length-1;i>=0;i--){
				row[i+24]=factorsRow[i];
			}
		}
	}
	

	//Calculate all the formulas to complete the matrix
	public void evaluateAllFormulaCells(){
		double ksec,ksecf, sumaPesada1, sumaPesada2,factor;
		for(int i=0;i<rows;i++){
			factor = (data[i][19]*loads[0]+data[i][20]*loads[1]+data[i][21]*loads[2]+data[i][22]*loads[3]);
			ksec = 1;
			if(factor!=0&&data[i][18]!=0)
				ksec = data[i][18]/factor;
			ksecf = 1;
			if(ksec<1.0)
				ksecf=ksec;//=SI(BE4<1,BE4,1)
			//System.out.println(ksecf);
			sumaPesada1 = data[i][18]+factor*ksecf*2.0;
			sumaPesada2 = data[i][18]+(factor-data[i][20]*loads[1])*ksecf*2.0;
			data[i][54]=sumaPesada1;data[i][55]=sumaPesada2;data[i][56]=ksec;data[i][57]=ksecf;
			data[i][88]=0;
			data[i][89]=0;
			
			int j=0;
			for(j=0;j<3;j++){
				data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada1;
				data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada1;
			}
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada2;
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada2;
			j++;
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada1;
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada1;
			j++;
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*data[i][23];
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*data[i][23];
			j++;
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*data[i][23];
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*data[i][23];
			j++;
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada1;
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada1;
			j++;
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada2;
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada2;
			j++;
			data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada1;
			data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada1;
			j++;
			for(j=10;j<15;j++){
				data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada2;
				data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada2;
			}
			//data[i][58+j*2]=data[i][j+3]*data[i][24+j*2]*sumaPesada1;
			//data[i][58+j*2+1]=data[i][j+3]*data[i][25+j*2]*sumaPesada1;
			for(j=0;j<15;j++){
				//Totals
				data[i][88]+=data[i][58+j*2];
				data[i][89]+=data[i][58+j*2+1];
			}
		}
	}

	public double[] getFactorsRow() {
		return factorsRow;
	}

	public void setFactorsRow(double[] factorsRow) {
		this.factorsRow = factorsRow;
	}

	public double[][] getData() {
		return data;
	}

	public void setData(double[][] data) {
		this.data = data;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
}
