package org.lasalle.clima.excel.bottomuphour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LonguitudeAndFlows {

	private static final int CELL_KEY=7;
	private static final int CELL_FID_GRID=0;
	private static final int CELL_LONGUITUDE=3;
	private static final int CELL_POLLUTANT=0;
	private static final int CELL_TOTAL_VALOR=87;
	private static final int CELL_TOTAL_INCER=88;
	private static final int CELL_TIPO_VIA=8;
	private int nRows = 0;
	
	private XSSFSheet sheet;
	public String[] tiposVias;
	public double[][] values;
	File file;
	
	public LonguitudeAndFlows(XSSFSheet sheet){
		this.sheet=sheet;
		readInfo(sheet);
	}
	
	public LonguitudeAndFlows(File file){
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
			sheet = workbook.getSheetAt(0);
			readInfo(sheet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sheet=null;
		}
	}
	
	private void readInfo(XSSFSheet sheet){
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = sheet.iterator();
		rowIterator.next();//Ignore the column names. We actually know it.
		Row row = null;//The current cell
		//Only to count the rows
		nRows = 0;
		while(rowIterator.hasNext()){
			row = rowIterator.next();
			nRows++;
		}
		
		tiposVias = new String[nRows];
		values = new double[3][nRows];
		rowIterator = sheet.iterator();
		rowIterator.next();//Ignore the column names. We actually know it.
		//Only to count the rows
		int k=0;
		while(rowIterator.hasNext()){
			row = rowIterator.next();
			tiposVias[k]=row.getCell(CELL_TIPO_VIA).getStringCellValue();
			values[0][k] = row.getCell(CELL_KEY).getNumericCellValue();
			values[1][k] = row.getCell(CELL_FID_GRID).getNumericCellValue();
			values[2][k] = row.getCell(CELL_LONGUITUDE).getNumericCellValue();
			k++;
		}
	}

	public int getnRows() {
		return nRows;
	}

	public void setnRows(int nRows) {
		this.nRows = nRows;
	}

}
