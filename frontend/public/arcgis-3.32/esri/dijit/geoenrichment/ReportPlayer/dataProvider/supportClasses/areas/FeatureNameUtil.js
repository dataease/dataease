// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/dataProvider/supportClasses/areas/FeatureNameUtil",[],function(){return{_nameScores:[/^name$/i,/^title$/i,/(^name)|(name$)/i,/(^title)|(title$)/i],guessFeatureName:function(a){var d=Object.keys(a.attributes),b;this._nameScores.some(function(a){return d.some(function(c){if(a.test(c))return b=c,!0})});return a.attributes[b]||""}}});