// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/_RefreshMixin","dojo/_base/declare dojo/_base/lang dojo/_base/Deferred dojo/on dojo/has ../kernel".split(" "),function(b,c,f,d,g,h){function e(a){"object"!==typeof a&&(a=Error(a));a.grid=this;d.emit(this.domNode,"dgrid-error",{grid:this,error:a,cancelable:!0,bubbles:!0})&&console.error(a)}b=b(null,{_trackError:function(a){var b;"string"===typeof a&&(a=c.hitch(this,a));try{b=a()}catch(k){e.call(this,k)}return f.when(b,c.hitch(this,function(){d.emit(this.domNode,"refresh",{cancelable:!0,
bubbles:!0})}),c.hitch(this,e))}});g("extend-esri")&&c.setObject("dijit._RefreshMixin",b,h);return b});