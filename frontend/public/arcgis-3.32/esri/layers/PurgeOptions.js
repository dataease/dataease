// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/PurgeOptions",["dojo/_base/declare","dojo/_base/lang","dojo/Stateful","dojo/has","../kernel"],function(a,d,e,f,g){a=a([e],{declaredClass:"esri.layers.PurgeOptions",constructor:function(a,b){this.parent=a;for(var c in b)this[c]=b[c]},_displayCountSetter:function(a){this.displayCount=a;this.parent.refresh()}});f("extend-esri")&&d.setObject("layers.PurgeOptions",a,g);return a});