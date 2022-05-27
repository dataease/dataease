// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../chunks/vec3f64"],function(b,a){b.AmbientLight=function(c=a.create()){this.intensity=c};b.FillLight=function(c=a.create(),d=a.fromValues(.57735,.57735,.57735)){this.intensity=a.create();this.direction=a.create();this.intensity=c;this.direction=d};b.MainLight=function(c=a.create(),d=a.fromValues(.57735,.57735,.57735),e=!0){this.intensity=c;this.direction=d;this.castShadows=e};b.SphericalHarmonicsLight=function(){this.sh={r:[0],g:[0],b:[0]}};Object.defineProperty(b,"__esModule",
{value:!0})});