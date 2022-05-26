// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/opsdashboard/core/ExtensionConfigurationBase",["dojo/_base/declare","./messageHandler","./errorMessages","./ExtensionBase"],function(c,b,d,e){return c([e],{config:null,_setConfig:function(a){this.config=a||{}},__messageReceived:function(a){if("updateconfig"===a.functionName.toLowerCase())return a.args={configuration:this.config},b._sendMessage(a);this.inherited(arguments)},readyToPersistConfig:function(a){if(!this._isHostInitialized())throw Error(d.hostNotReady);b._sendMessage({functionName:"readyToPersistConfig",
args:{canAccept:a}})}})});