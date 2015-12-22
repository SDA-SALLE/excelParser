/**
 * @object BottomUp
 * Bottom up analysis.
 */
var BottomUp = { 
	/**
	* @function process(row, value)
	* This function process the given input files to produce the result in the output folder. Each
	* output file corresponds to a pollutant in an specific hour.
	* @param file1:String The path to the excel file containing the information about length and flows on each cell of map.
	* @param file2:String The path to the excel file containing the information about vehicular flows by hour.
	* @param file3:String The path to the excel file containing the information about pollutant emission factors.
	* @param outputFolder:String The path to store the excel outputs.
	* @param types:Object An array containing the type of days to process. "H" for habil, "D" for domingo and "S" for sabado.
	*/
	process : function(file1, file2, file3, outputFolder, types){
		 file1 = File.checkGlobal(file1);
		 file2 = File.checkGlobal(file2);
		 file3 = File.checkGlobal(file3);
		 outputFolder = File.checkGlobal(outputFolder);
		  
		 BottomUpAPI.process(file1, file2, file3, outputFolder, types, Global.basedir, Global.basedirkey);
	}
}