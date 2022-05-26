// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/_PageButtons",["esri/declare","dojo/on","dojo/dom-construct","dijit/layout/ContentPane"],function(g,h,d,k){return g(null,{buttonsNode:null,addButtons:function(e){if(!this.buttonsNode){for(var b,a=this.layoutGrid.getChildren(),c=0;c<a.length;c++)2==a[c].row&&(b=a[c]);b||(b=new k({row:2,"class":"Wizard_BottomPane"}),this.layoutGrid.addChild(b));this.buttonsNode=d.create("div",{"class":"Wizard_Buttons"},b.domNode)}b={};for(c=0;c<e.length;c++){var a=e[c],f=d.create("button",
{"class":"Wizard_Button",innerHTML:a.label||""},this.buttonsNode);a.id&&(b[a.id]=f);a.onClick&&h(f,"click",a.onClick)}return b}})});