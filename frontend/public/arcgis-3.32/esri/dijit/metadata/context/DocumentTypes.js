// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/context/DocumentTypes","dojo/_base/declare dojo/_base/lang dojo/_base/array dojo/has ../../../kernel dijit/_WidgetBase ../types/arcgis/base/DocumentType".split(" "),function(a,b,h,d,e,f,g){a=a([f],{index:null,list:null,postCreate:function(){this.inherited(arguments);this._initializeTypes()},_initializeTypes:function(){var a=this.list=[],b=this.index={},c=new g({interrogationRules:[{path:"/metadata/Esri/ArcGISFormat",must:!0}]});b[c.key]=c;a.push(c)}});d("extend-esri")&&
b.setObject("dijit.metadata.context.DocumentTypes",a,e);return a});