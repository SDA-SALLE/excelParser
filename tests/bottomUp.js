var inputFolder = ""+"/tests/datos_28112014/";
//console.log(Global.currentDir);
BottomUp.process(inputFolder+"ValoresLongyFlujoCeldas.xlsx",
		inputFolder+"InformacionFlujos.xlsx", 
		inputFolder+"FactoresEmision.xlsx",
		inputFolder+"output.xlsx", ["D"]);