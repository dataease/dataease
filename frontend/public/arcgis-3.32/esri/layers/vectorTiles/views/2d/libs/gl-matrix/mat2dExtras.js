// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/2d/libs/gl-matrix/mat2dExtras",["require","exports","./common","./mat2d","./vec2"],function(m,g,l,b,h){Object.defineProperty(g,"__esModule",{value:!0});var k=function(){var a=b.create(),c=h.create();return function(d,f,e){b.fromTranslation(a,e);b.multiply(d,a,f);h.negate(c,e);b.translate(d,d,c);return d}}();g.rotategAt=function(){var a=b.create();return function(c,d,f,e){b.fromRotation(a,l.toRadian(f));k(a,a,e);b.multiply(c,a,d);return c}}();g.scaleAt=function(){var a=
b.create();return function(c,d,f,e){b.fromScaling(a,f);k(a,a,e);b.multiply(c,a,d);return c}}()});