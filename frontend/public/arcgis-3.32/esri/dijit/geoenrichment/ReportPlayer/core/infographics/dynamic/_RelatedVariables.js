// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/infographics/dynamic/_RelatedVariables",["dojo/_base/declare","esri/dijit/geoenrichment/infographicPanels/RelatedVariables","./_SelectedFeatureControlMixin","esri/dijit/geoenrichment/utils/DynamicStyleUtil"],function(d,e,f,c){return d([e,f],{infographicStyleProvider:null,_themeAddedFlag:!1,updateUI:function(){this.inherited(arguments);if(!this._themeAddedFlag){var a=this.infographicStyleProvider&&this.infographicStyleProvider.agePyramid;if(a){var b=
this.parent.id;c.addStyle(["#"+b+" .RelatedVariables_PositiveBar { background-color:"+a.male.backgroundColor+"; } #"+b+" .RelatedVariables_NegativeBar { background-color:"+a.female.backgroundColor+"; } #"+b+" .RelatedVariables_DifferenceColumn_Positive { color:"+a.male.backgroundColor+"; } #"+b+" .RelatedVariables_DifferenceColumn_Negative { color:"+a.female.backgroundColor+"; }"],b);this._themeAddedFlag=!0}}},destroy:function(){c.removeStyle(this.parent.id);this.inherited(arguments)}})});