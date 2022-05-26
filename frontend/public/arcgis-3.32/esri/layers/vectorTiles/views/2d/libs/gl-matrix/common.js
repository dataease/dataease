// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/2d/libs/gl-matrix/common",[],function(){if(!b)var b=1E-6;if(!c)var c="undefined"!==typeof Float32Array?Float32Array:Array;if(!d)var d=Math.random;var a={GLMAT_EPSILON:b,GLMAT_ARRAY_TYPE:c,GLMAT_RANDOM:d,setMatrixArrayType:function(e){a.GLMAT_ARRAY_TYPE=e}},f=Math.PI/180,g=180/Math.PI;a.toRadian=function(a){return a*f};a.toDegree=function(a){return a*g};a.setMatrixArrayType(Array);return a});