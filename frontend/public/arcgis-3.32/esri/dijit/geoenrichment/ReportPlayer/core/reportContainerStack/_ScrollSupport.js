// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/reportContainerStack/_ScrollSupport",["dojo/_base/declare"],function(a){return a(null,{getScrollableContainer:function(){return this.domNode},isScrollShown:function(){return this.domNode.clientHeight<this.domNode.scrollHeight},scrollToTop:function(){this.domNode.scrollTop=0;this._updateLabelsPosition()}})});