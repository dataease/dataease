// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/dataProvider/supportClasses/stdGeographies/StdGeographiesModel",["dojo/_base/declare","dojo/i18n!esri/nls/jsapi"],function(c,b){b=b.geoenrichment.dijit.ReportPlayer.ReportPlayer;return c(null,{countryID:null,hierarchyID:null,_levelsCache:null,_levels:null,_countryLevel:null,constructor:function(a){this.countryID=a.countryID;this.hierarchyID=a.hierarchyID;this._initWithLevels(a.levels)},_initWithLevels:function(a){this._levels=a;this._levelsCache={};this._levels.forEach(function(a){this._levelsCache[a.id]=
a;a.adminLevel&&(this._levelsCache[a.adminLevel]=a);a.isWholeCountry&&(this._countryLevel=a)},this)},getLevels:function(a){return a?this._levels.filter(function(a){return!a.isWholeCountry}):this._levels.slice()},getLevel:function(a){return this._levelsCache[a]},getLevelPluralName:function(a){return(a=this.getLevel(a))?a.isWholeCountry?b.wholeCountry:a.name||a.pluralName:null},getCountryLevel:function(){return this._countryLevel},toJson:function(){return{countryID:this.countryID,hierarchyID:this.hierarchyID,
levels:this._levels}}})});