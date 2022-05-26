// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/opsdashboard/core/MessageReceiver",["dojo/_base/declare","dojo/_base/lang","dojo/Evented","./messageHandler"],function(c,b,d,e){return c([d],{constructor:function(a){this._setConfig(a);e.on("message-received",b.hitch(this,this.__messageReceived))},_setConfig:function(a){a&&b.mixin(this,a)},__messageReceived:function(a){this._messageReceived(a)},_messageReceived:function(a){}})});