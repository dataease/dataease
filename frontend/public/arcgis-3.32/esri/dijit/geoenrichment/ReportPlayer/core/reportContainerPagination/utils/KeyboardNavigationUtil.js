// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/reportContainerPagination/utils/KeyboardNavigationUtil",["dojo/on","dojo/keys","esri/dijit/geoenrichment/utils/MouseUtil"],function(d,b,e){return{setUpKeyboardNavigation:function(a){a.own(d(window,"keyup",function(c){e.isMouseOver(a.domNode)&&(c.keyCode===b.RIGHT_ARROW?a.showNextSlide():c.keyCode===b.LEFT_ARROW&&a.showPreviousSlide())}))}}});