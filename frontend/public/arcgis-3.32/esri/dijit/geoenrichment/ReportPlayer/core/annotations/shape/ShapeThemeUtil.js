// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/annotations/shape/ShapeThemeUtil",["esri/dijit/geoenrichment/ReportPlayer/core/infographics/utils/InfographicThemeUtil","esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/ElementUsageTypes"],function(c,d){return{getThemeStylesFromShapeJson:function(b,a){return b.themeStyle?b.themeStyle:a?(a=a.parentWidget.elementUsageType===d.INFOGRAPHIC_SECTION?a.viewModel.getStaticInfographicDefaultStyles(a.theme):a.viewModel.getCurrentTheme(a.theme),c.applyThemeSettingsToShapeJson(b,
a),a=b.themeStyle,delete b.themeStyle,a):null}}});