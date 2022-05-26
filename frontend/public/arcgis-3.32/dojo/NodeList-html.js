/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/NodeList-html",["./query","./_base/lang","./html"],function(a,b,d){a=a.NodeList;b.extend(a,{html:function(a,b){var c=new d._ContentSetter(b||{});this.forEach(function(b){c.node=b;c.set(a);c.tearDown()});return this}});return a});