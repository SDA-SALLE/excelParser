# excelParser
BottomUp preprocess

Para instalar el proyecto debe tener Eclipse y J2EE

Hacer check out de este proyecto en su workspace: [function project](https://github.com/cheminfo/function)

Hacer check out de las librerias para los proyectos: [mis librerias](https://github.com/andcastillo/library)

Configurar el path de eclipse y correr el ejemplo: org.lasalle.clima.scripting.tests.TestScripting

Esto ejecutará el análisis para los datos almacenados en la carpeta /tests/datos_28112014/

``` js
var inputFolder = ""+"/tests/datos_28112014/";
BottomUp.process(inputFolder+"ValoresLongyFlujoCeldas.xlsx",
		inputFolder+"InformacionFlujos.xlsx", 
		inputFolder+"FactoresEmision.xlsx",
		inputFolder+"output.xlsx", ["D"]);
```
