// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/editing/tools/ToggleToolBase","dojo/_base/declare dojo/_base/lang dojo/has dijit/form/ToggleButton ./ToolBase ../../../kernel".split(" "),function(a,d,e,b,c,f){a=a([b,c],{declaredClass:"esri.dijit.editing.tools.ToggleToolBase",postCreate:function(){this.inherited(arguments);this._setShowLabelAttr&&this._setShowLabelAttr(!1)},destroy:function(){b.prototype.destroy.apply(this,arguments);c.prototype.destroy.apply(this,arguments)},setChecked:function(a){b.prototype.setChecked.apply(this,
arguments)}});e("extend-esri")&&d.setObject("dijit.editing.tools.ToggleToolBase",a,f);return a});