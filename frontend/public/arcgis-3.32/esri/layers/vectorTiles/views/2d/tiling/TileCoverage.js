// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/2d/tiling/TileCoverage",["require","exports","../../../core/ArrayPool","../../../core/ObjectPool","./TileSpan"],function(l,m,d,e,f){return function(){function a(b){this.lodInfo=b;this.spans=d.acquire()}a.prototype.release=function(){for(var b=0,a=this.spans;b<a.length;b++)f.pool.release(a[b]);d.release(this.spans)};a.prototype.forEach=function(a,d){var b=this.spans,g=this.lodInfo,e=g.level;if(0!==b.length)for(var h=0;h<b.length;h++)for(var c=b[h],f=c.row,k=c.colTo,
c=c.colFrom;c<=k;c++)a.call(d,e,f,g.normalizeCol(c),g.getWorldForColumn(c))};a.pool=new e(a,!0);return a}()});