// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
require({cache:{"url:esri/dijit/metadata/base/templates/MandatoryLabel.html":'\x3cdiv class\x3d"gxeLabelContainer gxeMandatoryLabel"\x3e\r\n  \x3clabel data-dojo-attach-point\x3d"labelNode"\x3e${label}\x3c/label\x3e\r\n\x3c/div\x3e'}});
define("esri/dijit/metadata/base/MandatoryLabel","dojo/_base/declare dojo/_base/lang dojo/has ./Templated dojo/text!./templates/MandatoryLabel.html ../../../kernel".split(" "),function(a,b,c,d,e,f){a=a([d],{label:null,templateString:e,postCreate:function(){this.inherited(arguments)}});c("extend-esri")&&b.setObject("dijit.metadata.base.MandatoryLabel",a,f);return a});