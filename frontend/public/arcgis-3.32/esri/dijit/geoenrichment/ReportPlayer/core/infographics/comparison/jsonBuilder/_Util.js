// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/infographics/comparison/jsonBuilder/_Util",["../../../grid/coreUtils/GridDataUtil","../../../comparison/ComparisonUtil","../../../supportClasses/templateJsonUtils/fieldInfo/FieldInfoFormatUtil"],function(f,g,h){var e={};e.valueFormatFunction=g.valueFormatFunction;e.setValueToGridData=function(a,b,c){var d=b.fieldInfos[c];d.isMissing||delete b.fieldInfos[c];a=a[d.name];void 0===a||"string"===typeof a?b[c]=a||"":(b[c]=h.formatNumber(a,d),f.setNumericDataValue(a,
b,c));return{value:a,formattedValue:b[c],decimals:d.decimals}};return e});