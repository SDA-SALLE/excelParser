package org.lasalle.clima.process;

import java.io.File;

public class AggregatorWraper {

	public static void main(String[] args) {
		String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/Desagregacionv2/";
		if(args!=null && args.length>0)
			inputFolder=args[0];
		String outputFolder = inputFolder;//"/Users/acastillo/Desktop/BD_Movilidad/ProcFiltradosEstacionesPrincipales/";
		
		//To create the output folder if different than the input forlder
		File output = new File(outputFolder);
		if(!output.exists()){
			if(output.mkdirs())
				System.out.println("Output folder created: "+outputFolder);
		}
		
		Aggregator processor = new Aggregator(new File(inputFolder+"ValoresLongyFlujoCeldas.xlsx"),
				new File(inputFolder+"InformacionFlujos.xlsx"), 
				new File(inputFolder+"FactoresEmision.xlsx"),
				new File(inputFolder+"output.xlsx"));
		
		processor.addNewIDtoCells();
		processor.expandCells();
		//processor.saveAll();
		
		
		
	}

}
