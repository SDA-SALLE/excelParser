package org.lasalle.clima.excel.buttomuphour;

import java.io.File;

public class AggregatorWraper {

	public static void main(String[] args) {
		//String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/Desagregacion14052014/";
		//String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/DesagregacionNoEmision/";
		//String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/datosGerman/";
		String inputFolder = "/Users/acastillo/Documents/BD_Movilidad/BD_new/INPUTSPARAOUTPUTS_S/";
		if(args!=null && args.length>0)
			inputFolder=args[0];
		
		String outputFolder = inputFolder;//"/Users/acastillo/Desktop/BD_Movilidad/ProcFiltradosEstacionesPrincipales/";
		String[] types = {"S"};
		//Second parameter is the type of days to process. "H" for habil, "D" for domingo and "S" for sabado
		if(args.length>1){
			int index = 0;
			types = new String[args[1].length()];
			for(int i=0;i<args[1].length();i++){
				types[i]=args[1].charAt(i)+"";
			}
		}
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
		processor.setTypes(types);
		//processor.addNewIDtoCells();
		processor.expandCells();
		//processor.saveAll();
		
		
		
	}

}
