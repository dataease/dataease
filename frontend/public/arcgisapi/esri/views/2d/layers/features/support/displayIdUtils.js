// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(a){let f=function(){function b(){this._freeIds=[];this._idCounter=1}var c=b.prototype;c.createId=function(d=!1){var e=this._getFreeId();return((d?2147483648:0)|e)>>>0};c.releaseId=function(d){this._freeIds.push(d)};c._getFreeId=function(){return this._freeIds.length?this._freeIds.pop():this._idCounter++};return b}();a.DISPLAY_ID_TYPE_AGGREGATE=1;a.DISPLAY_ID_TYPE_FEATURE=0;a.DisplayIdGenerator=f;a.getDisplayIdIndex=function(b){return b&2147483647};a.getDisplayIdType=function(b){return(b&
2147483648)>>>31};Object.defineProperty(a,"__esModule",{value:!0})});