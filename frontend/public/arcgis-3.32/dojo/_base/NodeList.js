/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/NodeList",["./kernel","../query","./array","./html","../NodeList-dom"],function(b,a,d){a=a.NodeList;var c=a.prototype;c.connect=a._adaptAsForEach(function(){return b.connect.apply(this,arguments)});c.coords=a._adaptAsMap(b.coords);a.events="blur focus change click error keydown keypress keyup load mousedown mouseenter mouseleave mousemove mouseout mouseover mouseup submit".split(" ");d.forEach(a.events,function(a){var b="on"+a;c[b]=function(a,c){return this.connect(b,a,c)}});return b.NodeList=
a});