// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/dummyData/DummyGE",["esri/dijit/geoenrichment/ReportPlayer/dataProvider/supportClasses/ge/LocalGEChart","dojo/i18n!esri/nls/jsapi"],function(b,a){a=a.geoenrichment.dijit.ReportPlayer.DummyGE;return{_ge:null,getInstance:function(){this._ge||(this._ge=this._createGE());return this._ge},_createGE:function(){return new b([],"dummyCalc",[{dummyCalc:{data:{StdGeographyName:null,StdGeographyID:null},comparisonLevels:[{StdGeographyName:a.sampleData,
StdGeographyID:"sampleData"}]}}])}}});