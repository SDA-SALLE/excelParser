package org.lasalle.clima.processfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class OutputSheet {
	private XSSFSheet sheet;
	private File file;
	private String post="";
	private int currentRow;//Pointing to the current line
	
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
			sheet = workbook.getSheetAt(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sheet=null;
		}
		currentRow=2;
	}
	
	public boolean save(){
		FileOutputStream dataFileOut;
		try {
			dataFileOut = new FileOutputStream(
					file.getAbsolutePath().replace(".xlsx", post+".xlsx"));
			XSSFFormulaEvaluator.evaluateAllFormulaCells(sheet.getWorkbook());
			sheet.getWorkbook().write(dataFileOut);
			dataFileOut.flush();
			dataFileOut.close();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void push(double[] values, int fidGrid, double longuite){
		//System.out.println(post+" "+values[0]);
		if(values!=null&&values.length==16){
			double total = values[15];
			XSSFRow row = sheet.getRow(currentRow);
			//If it has to be in the same row
			if(currentRow!=2 && row.getCell(0).getNumericCellValue()==fidGrid
					&& row.getCell(1).getNumericCellValue()==total){
				row.getCell(17).setCellValue(longuite/1000+row.getCell(17).getNumericCellValue());
			}
			else{//A new row
				currentRow++;
				//System.out.println(currentRow);
				row = sheet.getRow(currentRow);
				if(row==null){
					//System.out.println("Here");
					copyRow(sheet,3,currentRow);
					row = sheet.getRow(currentRow);
				}
				
				row.getCell(0).setCellValue(fidGrid);
				row.getCell(1).setCellValue(total);
				for(int i=0;i<values.length-1;i++){
					row.getCell(i+2).setCellValue(values[i]);
				}
				row.getCell(17).setCellValue(longuite/1000);
			}
		}
	}

	private void copyRow(XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {
		  // Get the source / new row
		  Row newRow = worksheet.getRow(destinationRowNum);
		  Row sourceRow = worksheet.getRow(sourceRowNum);

		  // If the row exist in destination, push down all rows by 1 else create a new row
		  if (newRow != null) {
		    worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
		  } else {
		    newRow = worksheet.createRow(destinationRowNum);
		  }

		  // Loop through source columns to add to new row
		  for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
		    // Grab a copy of the old/new cell
		    Cell oldCell = sourceRow.getCell(i);
		    Cell newCell = newRow.createCell(i);

		    // If the old cell is null jump to next cell
		    if (oldCell == null) {
		      newCell = null;
		      continue;
		    }

		    // Use old cell style
		    newCell.setCellStyle(oldCell.getCellStyle());

		    // If there is a cell comment, copy
		    if (newCell.getCellComment() != null) {
		      newCell.setCellComment(oldCell.getCellComment());
		    }

		    // If there is a cell hyperlink, copy
		    if (oldCell.getHyperlink() != null) {
		      newCell.setHyperlink(oldCell.getHyperlink());
		    }

		    // Set the cell data type
		    newCell.setCellType(oldCell.getCellType());

		    // Set the cell data value
		    switch (oldCell.getCellType()) {
		    case Cell.CELL_TYPE_BLANK:
		      break;
		    case Cell.CELL_TYPE_BOOLEAN:
		      newCell.setCellValue(oldCell.getBooleanCellValue());
		      break;
		    case Cell.CELL_TYPE_ERROR:
		      newCell.setCellErrorValue(oldCell.getErrorCellValue());
		      break;
		    case Cell.CELL_TYPE_FORMULA:
		    	//Here we replace the sourceRowNum+1 by the destinationRowNum+1
		      newCell.setCellFormula(oldCell.getCellFormula().replace((sourceRowNum+1)+"", (destinationRowNum+1)+""));
		      break;
		    case Cell.CELL_TYPE_NUMERIC:
		      newCell.setCellValue(oldCell.getNumericCellValue());
		      break;
		    case Cell.CELL_TYPE_STRING:
		      newCell.setCellValue(oldCell.getRichStringCellValue());
		      break;
		    }
		  }
		}
	
	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public void replaceFactors(double[] factorsRow) {
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = sheet.iterator();
		rowIterator.next();//Ignore the column names. We actually know it.
		rowIterator.next();//Ignore the column names. We actually know it.
		rowIterator.next();//Ignore the column names. We actually know it.
		Row row = null;//The current cell
		while(rowIterator.hasNext()){
			//System.out.println(k++);
			row = rowIterator.next();
			for(int i=factorsRow.length-1;i>=0;i--){
				row.getCell(i+22).setCellValue(factorsRow[i]);
			}
		}
		
	}

	public XSSFSheet getSheet() {
		return sheet;
	}

	public void setSheet(XSSFSheet sheet) {
		this.sheet = sheet;
	}
}
