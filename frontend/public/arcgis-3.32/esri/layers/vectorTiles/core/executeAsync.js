// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/executeAsync","require exports dojo/has ./nextTick ./now ./promiseUtils".split(" "),function(m,n,p,g,h,d){function k(){var b=0===a.length;if(!b){for(var l=h();!b&&6>h()-l;){var e=a[c];if(e){if(!0===e()){var d=f.get(e);a[c]=null;f.delete(e);d()}c=(c+1)%a.length}else a.splice(c,1),c=(b=0===a.length)?0:c%a.length}b||g(k)}}var a=[],f=new Map,c=0;return function(b){return b?d.create(function(c){a.push(b);f.set(b,c);1===a.length&&g(k)},function(){a[a.indexOf(b)]=null;
f.delete(b)}):d.reject(new TypeError("callback is not a function"))}});