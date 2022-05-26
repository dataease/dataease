// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/editing/tools/Edit",["dojo/_base/declare","dojo/_base/lang","dojo/has","./MenuItemBase","../../../kernel"],function(a,b,c,d,e){a=a([d],{declaredClass:"esri.dijit.editing.tools.Edit",enable:function(a){this._enabled=a===this._geomType;this.inherited(arguments)}});c("extend-esri")&&b.setObject("dijit.editing.tools.Edit",a,e);return a});