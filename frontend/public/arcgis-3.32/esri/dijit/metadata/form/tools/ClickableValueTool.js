// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/form/tools/ClickableValueTool",["dojo/_base/declare","dojo/_base/lang","dojo/has","./ClickableTool","../../../../kernel"],function(a,c,d,e,f){a=a([e],{value:null,postCreate:function(){this.inherited(arguments)},whenToolClicked:function(a,b){b&&b.setInputValue(this.value)}});d("extend-esri")&&c.setObject("dijit.metadata.form.tools.ClickableValueTool",a,f);return a});