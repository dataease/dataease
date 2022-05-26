// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/VisibleScaleRangeSlider/recommendedScales",["dojo/_base/lang"],function(a){var b={_recommendedScales:{world:1E8,continent:5E7,countriesBig:25E6,countriesSmall:12E6,statesProvinces:6E6,stateProvince:3E6,counties:15E5,county:75E4,metropolitanArea:32E4,cities:16E4,city:8E4,town:4E4,neighborhood:2E4,streets:1E4,street:5E3,buildings:2500,building:1250,smallBuilding:800,rooms:400,room:100},getRecommendedScale:function(a){return b._recommendedScales[a]},all:function(){return a.clone(b._recommendedScales)}};
return b});