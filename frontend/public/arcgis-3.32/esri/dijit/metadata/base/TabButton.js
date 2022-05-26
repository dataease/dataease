// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
require({cache:{"url:esri/dijit/metadata/base/templates/TabButton.html":'\x3cdiv class\x3d"gxeTabButton standard" \r\n  data-dojo-attach-point\x3d"labelNode"\r\n  data-dojo-attach-event\x3d"onclick: _onClick"\x3e\r\n  ${label}\r\n\x3c/div\x3e\r\n'}});
define("esri/dijit/metadata/base/TabButton","dojo/_base/declare dojo/_base/lang dojo/dom-attr dojo/has ./Templated dojo/text!./templates/TabButton.html ../../../kernel".split(" "),function(b,c,h,d,e,f,g){b=b([e],{label:null,templateString:f,postCreate:function(){this.inherited(arguments)},_onClick:function(){this.onClick(this)},onClick:function(a){},setLabel:function(a){"undefined"===typeof a&&(a=null);this.label=a;this.setI18nNodeText(this.labelNode,a)}});d("extend-esri")&&c.setObject("dijit.metadata.base.TabButton",
b,g);return b});