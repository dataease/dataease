// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/_Invoke",["esri/declare"],function(d){return d("esri.dijit.geoenrichment._Invoke",null,{_invokeTimeoutIDs:null,invoke:function(a,b){if(!this._invokeTimeoutIDs)this._invokeTimeoutIDs={};else if(this._invokeTimeoutIDs[a]){if(void 0===b)return;clearTimeout(this._invokeTimeoutIDs[a])}var c=this;this._invokeTimeoutIDs[a]=setTimeout(function(){c._invokeTimeoutIDs[a]=0;c[a]()},b||0)},pendingInvoke:function(a){return this._invokeTimeoutIDs?this._invokeTimeoutIDs[a]:!1},cancelInvoke:function(a){if(this._invokeTimeoutIDs){var b=
this._invokeTimeoutIDs[a];b&&(clearTimeout(b),this._invokeTimeoutIDs[a]=0)}}})});