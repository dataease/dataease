// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/form/iso/CodeListElement",["dojo/_base/declare","dojo/_base/lang","dojo/has","../Element","../../../../kernel"],function(a,b,c,d,e){a=a([d],{showHeader:!1,postCreate:function(){this.inherited(arguments)},resolveMinOccurs:function(){return this.parentElement?this.parentElement.resolveMinOccurs():this.minOccurs}});c("extend-esri")&&b.setObject("dijit.metadata.form.iso.CodeListElement",a,e);return a});