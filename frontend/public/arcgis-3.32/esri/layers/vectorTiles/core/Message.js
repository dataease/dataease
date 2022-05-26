// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/Message",["require","exports","dojo/string"],function(f,g,d){return function(){function a(e,b,c){this instanceof a&&(this.name=e,this.message=b&&d.substitute(b,c,function(a){return null==a?"":a})||"",this.details=c)}a.prototype.toString=function(){return"["+this.name+"]: "+this.message};return a}()});