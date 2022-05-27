// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../core/maybe","./locale"],function(c,k,e){function g(b){const a=b||h;if(!d.has(a)){var f=e.getLocale();f=l[e.getLocale()]||f;d.set(a,new Intl.NumberFormat(f,b))}return k.assumeNonNull(d.get(a))}const l={ar:"ar-u-nu-latn"};let d=new WeakMap,h={};e.beforeLocaleChange(()=>{d=new WeakMap;h={}});c.convertNumberFormatToIntlOptions=function(b={}){const a={};null!=b.digitSeparator&&(a.useGrouping=b.digitSeparator);null!=b.places&&(a.minimumFractionDigits=a.maximumFractionDigits=b.places);
return a};c.formatNumber=function(b,a){return g(a).format(b)};c.getFormatter=g;Object.defineProperty(c,"__esModule",{value:!0})});