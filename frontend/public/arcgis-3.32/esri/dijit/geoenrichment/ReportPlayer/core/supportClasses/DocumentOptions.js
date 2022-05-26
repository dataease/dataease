// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/DocumentOptions",["dojo/_base/lang","esri/dijit/geoenrichment/utils/PageUnitsConverter","../themes/ThemeLibrary"],function(h,d,m){var c={},e={a:[[841,1189],[594,841],[420,594],[297,420],[210,297],[148,210],[105,148],[74,105],[52,74],[37,52],[26,37]],b:[[1E3,1414],[707,1E3],[500,707],[353,500],[250,353],[176,250],[125,176],[88,125],[62,88],[44,62],[31,44]],c:[[917,1297],[648,917],[458,648],[324,458],[229,324],[162,229],[114,162],[81,
114],[57,81],[40,57],[28,40]],letter:[216,279],legal:[216,356],tabloid:[279,432],ledger:[432,279]};c.PAPER_SIZES=e;var f={};"a3 a4 a5 b4 b5 c4 c5 letter legal tabloid ledger".split(" ").forEach(function(a){var b;b=2===a.length?e[a[0]][a[1]]:e[a];f[a]={portrait:{w:d.mmToIn(b[0]),h:d.mmToIn(b[1])},landscape:{w:d.mmToIn(b[1]),h:d.mmToIn(b[0])}}});c.SIZE_TYPE_TO_DIM_HASH=f;c.hasStandardSize=function(a){return f[a]};c.getSupportedPageSizes=function(){var a=[],b;for(b in e)1===b.length?e[b].forEach(function(c,
d){a.push(b+d)}):a.push(b);return a};c.getPageDim=function(a,b,c){f[a]?a=f[a][b||"portrait"]:(a=a.split("x"),a={w:d.ptToIn(Number(a[0])),h:d.ptToIn(Number(a[1]))});c&&(a=h.clone(a),d.roundNDigitsObj(a,c.places));return a};c.getPageBox=function(a){if(!a)return null;var b=c.getPageDim(a.pagesize,a.orientation),n=d.inToPx(b.w),b=d.inToPx(b.h);return{w:n,h:b,contentW:n-(a.left||0)-(a.right||0),contentH:b-(a.top||0)-(a.bottom||0)}};c.combineCustomSizeString=function(a,b,c){return d.convert(a,c,"pt")+"x"+
d.convert(b,c,"pt")};c.getDefaultDocumentOptions=function(a){return h.mixin({pagesize:"letter",orientation:"portrait",left:21.07,right:22.6,top:19.2,bottom:9.6,align:"left",lineSpacing:1.493,fontSize:10,fontFamily:m.DEFAULT_FONT_FAMILY_CLASSIC},a)};c.getDefaultDocumentOptionsGraphicReport=function(a){return h.mixin({pagesize:"letter",orientation:"landscape",left:21.07,right:22.6,top:19.2,bottom:9.6,align:"left",lineSpacing:1.493,fontSize:10,fontFamily:m.DEFAULT_FONT_FAMILY_GRAPHIC},a)};c.getClosestStandrdSizes=
function(a,b){function d(a){var b=a.w/g.w;a=a.h/g.h;return Math.max(1-(1<b?1/b:b),1-(1<a?1/a:a))}b=void 0===b?.4:b;var g=c.getPageDim(a.pagesize,a.orientation);a=[];for(var e in f){var l=f[e];a.push({pagesize:e,orientation:"portrait",score:d(l.portrait)});a.push({pagesize:e,orientation:"landscape",score:d(l.landscape)})}a=a.filter(function(a){return a.score<=b});a.sort(function(a,b){return a.score-b.score});return a};c.tryGetStandardPageSize=function(a,b){function e(a){var c="portrait"===b?1:0;return a["portrait"===
b?0:1]===f&&a[c]===l}if(c.SIZE_TYPE_TO_DIM_HASH[a])return a;var g=a.split("x"),f=d.convert(Number(g[0]),"pt","mm",0),l=d.convert(Number(g[1]),"pt","mm",0),h,k;for(k in c.PAPER_SIZES)if(g=c.PAPER_SIZES[k],1===k.length){if(g.some(function(a,b){if(e(a))return h=k+b,!0}))break}else if(e(g)){h=k;break}return h||a};return c});