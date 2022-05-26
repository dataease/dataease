// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/parsers/Parsers",["esri/dijit/geoenrichment/Deferred","require"],function(d,e){var b={},a={},c;b.initialize=function(){if(c)return c.promise;c=new d;e("./DocumentParser ./ChartConverterPtoE ./InfographicConverterPtoE ./SectionParser ./FieldParser ./FilterParser ./SortingParser ../../../supportClasses/templateJsonUtils/fieldInfo/utils".split(" "),function(b,d,e,f,g,h,k,l){a.document=b;a.chart=d;a.infographic=e;a.section=
f;a.field=g;a.filter=h;a.sorting=k;l.init().then(c.resolve)});return c.promise};b.getParser=function(b){return a[b]};return b});