// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/infographics/simpleInfographic/dataDrilling/panels/US/Mortgage",["../../ChartBuilder","../../../../../charts/utils/ChartTypes"],function(b,c){var a={};a.dwellingMortgage={states:"n,a,i",defaultState:"a",stateSettings:{n:{isCurrency:!0},a:{yAxisTitle:"Average dollars spent per household",isCurrency:!0}},fieldInfo:{isChart:!0,chartJson:b.createChart({type:c.COLUMN,title:"Owner Dwellings: Mortgage Payment",points:[{label:"Interest",fullName:"HousingHousehold.X3005_X"},
{label:"Principal",fullName:"HousingHousehold.X3006_X"}]})}};return a});