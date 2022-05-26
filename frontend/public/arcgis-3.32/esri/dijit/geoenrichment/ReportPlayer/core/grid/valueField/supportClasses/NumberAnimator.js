// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/grid/valueField/supportClasses/NumberAnimator",["esri/dijit/geoenrichment/utils/animation/Animator","esri/dijit/geoenrichment/ReportPlayer/ReportPlayerState"],function(b,c){return{animateNumber:function(a,d,e){c.isAnimationSuspended||(a.set("value","0"),a.__numberAnimation&&a.__numberAnimation.stop(),a.__numberAnimation=b.animateProperty({duration:375,properties:{p:{start:0,end:d,easing:"quadOut"}},progressFunction:function(b,c,f){a.domNode&&a.set("value",
e(d*f))},endFunction:function(){delete a.__numberAnimation}}))}}});