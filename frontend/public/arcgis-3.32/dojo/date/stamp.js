/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/date/stamp",["../_base/lang","../_base/array"],function(k,h){var g={};k.setObject("dojo.date.stamp",g);g.fromISOString=function(b,c){g._isoRegExp||(g._isoRegExp=/^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/);var a=g._isoRegExp.exec(b);b=null;if(a){a.shift();a[1]&&a[1]--;a[6]&&(a[6]*=1E3);c&&(c=new Date(c),h.forEach(h.map("FullYear Month Date Hours Minutes Seconds Milliseconds".split(" "),function(a){return c["get"+a]()}),
function(b,c){a[c]=a[c]||b}));b=new Date(a[0]||1970,a[1]||0,a[2]||1,a[3]||0,a[4]||0,a[5]||0,a[6]||0);100>a[0]&&b.setFullYear(a[0]||1970);var f=0,d=a[7]&&a[7].charAt(0);"Z"!=d&&(f=60*(a[8]||0)+(Number(a[9])||0),"-"!=d&&(f*=-1));d&&(f-=b.getTimezoneOffset());f&&b.setTime(b.getTime()+6E4*f)}return b};g.toISOString=function(b,c){var a=function(a){return 10>a?"0"+a:a};c=c||{};var f=[],d=c.zulu?"getUTC":"get",e="";"time"!=c.selector&&(e=b[d+"FullYear"](),e=["0000".substr((e+"").length)+e,a(b[d+"Month"]()+
1),a(b[d+"Date"]())].join("-"));f.push(e);"date"!=c.selector&&(e=[a(b[d+"Hours"]()),a(b[d+"Minutes"]()),a(b[d+"Seconds"]())].join(":"),d=b[d+"Milliseconds"](),c.milliseconds&&(e+="."+(100>d?"0":"")+a(d)),c.zulu?e+="Z":"time"!=c.selector&&(b=b.getTimezoneOffset(),c=Math.abs(b),e+=(0<b?"-":"+")+a(Math.floor(c/60))+":"+a(c%60)),f.push(e));return f.join("T")};return g});