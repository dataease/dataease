// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./Tile"],function(d,g){function f(a,b,c){a=a.clone();const e=1<<a.level;b=a.col+b;c=a.row+c;0>b&&b!==a.col?(a.col=b+e,--a.world):b>=e?(a.col=b-e,a.world+=1):a.col=b;a.row=c;return a}d.getPow2NeighborKey=f;d.getPow2NeighborTile=function(a,b,c){b=f(a.key,b,c);return new g.Tile(a.tileInfoView,b)};Object.defineProperty(d,"__esModule",{value:!0})});