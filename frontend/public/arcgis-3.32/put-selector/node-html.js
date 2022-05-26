"use strict";
var put;
function Element(tag){
	this.tag = tag;
}
// create set of elements with an empty content model, so we now to skip their closing tag
var emptyElements = {};
["base", "link", "meta", "hr", "br", "wbr", "img", "embed", "param", "source", "track", "area", "col", "input", "keygen", "command"].forEach(function(tag){
	emptyElements[tag] = true;
});
var prototype = Element.prototype;
var currentIndentation = '';
prototype.nodeType = 1;
prototype.put = function(){
	var args = [this];
	args.push.apply(args, arguments);
	return put.apply(null, args);
}
prototype.toString = function(noClose){
	var tag = this.tag;
	var emptyElement = emptyElements[tag];
	if(put.indentation && !noClose){
		// using pretty printing with indentation
		var lastIndentation = currentIndentation;
		currentIndentation += put.indentation;
		var html = (tag == 'html' ? '<!DOCTYPE html>\n<html' : '\n' + lastIndentation + '<' + tag) +
			(this.attributes ? this.attributes.join('') : '') + 
			(this.className ? ' class="' + this.className + '"' : '') + '>' +  
			(this.children ? this.children.join('') : '') +  
			(!this.mixed && !emptyElement  && this.children ? '\n' +lastIndentation : '') +
			(emptyElement ? '' : ('</' + tag + '>'));
		
		currentIndentation = lastIndentation;
		return html;
	}
	return (this.tag == 'html' ? '<!DOCTYPE html>\n<html' : '<' + this.tag) +
		(this.attributes ? this.attributes.join('') : '') + 
		(this.className ? ' class="' + this.className + '"' : '') + '>' +  
		(this.children ? this.children.join('') : '') +  
		((noClose || emptyElement) ? '' : ('</' + tag + '>')); 
};
prototype.sendTo = function(stream){
	if(typeof stream == 'function'){ // write function itself
		stream = {write: stream, end: stream};
	}
	var active = this;
	var streamIndentation = '';
	function pipe(element){
		// TODO: Perhaps consider buffering if it is any faster and having a non-indentation version that is faster
		var closing = returnTo(this);
		if(closing){
			stream.write(closing);
		}
		var tag = element.tag;
		if(element.tag){
			if(put.indentation){
				stream.write('\n' + streamIndentation + element.toString(true));
				streamIndentation += put.indentation;
			}else{
				stream.write(element.toString(true));
			}
			this.children = true;
			active = element;
			element.pipe = pipe;
		}else{
			stream.write(element.toString());
		}
	}
	function returnTo(element){
		var output = '';
		while(active != element){
			if(!active){
				throw new Error("Can not add to an element that has already been streamed");
			}
			var tag = active.tag;
			var emptyElement = emptyElements[tag];
			if(put.indentation){
				streamIndentation = streamIndentation.slice(put.indentation.length);
				if(!emptyElement){
					output += ((active.mixed || !active.children) ? '' : '\n' + streamIndentation) + '</' + tag + '>';	
				}
			}else if(!emptyElement){
				output += '</' + tag + '>';
			}
			active = active.parentNode;
		}
		return output;		
	}
	pipe.call(this, this);
	// add on end() function to close all the tags and close the stream
	this.end = function(leaveStreamOpen){
		stream[leaveStreamOpen ? 'write' : 'end'](returnTo(this) + '\n</' + this.tag + '>');
	}
	return this;
};
prototype.children = false;
prototype.attributes = false;
prototype.insertBefore = function(child, reference){
	child.parentNode = this;
	if(this.pipe){
		return this.pipe(child);
		//return this.s(child);
	}
	var children = this.children;
	if(!children){
		children = this.children = [];
	}
	if(reference){
		for(var i = 0, l = children.length; i < l; i++){
			if(reference == children[i]){
				child.nextSibling = reference;
				if(i > 0){
					children[i-1].nextSibling = child;
				}
				return children.splice(i, 0, child);
			}
		}
	}
	if(children.length > 0){
		children[children.length-1].nextSibling = child;
	}
	children.push(child);
};
prototype.appendChild = function(child){
	if(typeof child == "string"){
		this.mixed = true;
	}
	if(this.pipe){
		return this.pipe(child);
	}
	var children = this.children;
	if(!children){
		children = this.children = [];
	}
	children.push(child);
};
prototype.setAttribute = function(name, value, escape){
	var attributes = this.attributes; 
	if(!attributes){
		attributes = this.attributes = [];
	}
	attributes.push(' ' + name + '="' + value + '"');
};
prototype.removeAttribute = function(name, value){
	var attributes = this.attributes; 
	if(!attributes){
		return;
	}
	var match = ' ' + name + '=', matchLength = match.length;
	for(var i = 0, l = attributes.length; i < l; i++){
		if(attributes[i].slice(0, matchLength) == match){
			return attributes.splice(i, 1);
		}
	}
};
Object.defineProperties(prototype, {
	innerHTML: {
		get: function(){
			return this.children.join('');
		},
		set: function(value){
			this.mixed = true;
			if(this.pipe){
				return this.pipe(value);
			}
			this.children = [value];
		}
	}
});
function DocumentFragment(){
}
DocumentFragment.prototype = new Element();
DocumentFragment.prototype.toString = function(){
	return this.children ? this.children.join('') : '';
};

var lessThanRegex = /</g, ampersandRegex = /&/g;
module.exports = function(putModule, putFactory){
	put = putModule.exports = putFactory({
	// setup a document for string-based HTML creation, using our classes 
		createElement: function(tag){
			return new Element(tag);
		},
		createElementNS: function(uri, tag){
			return new Element(namespacePrefixes[uri] + ':' + tag);
		},
		createTextNode: function(value){
			return (typeof value == 'string' ? value : ('' + value)).replace(lessThanRegex, "&lt;").replace(ampersandRegex, "&amp;");
		},
		createDocumentFragment: function(){
			return new DocumentFragment(); 
		}
	}, { // fragment heuristic, don't use this fragments here, it only slows things down
		test: function(){
			return false;
		}
	});
	put.indentation = '  ';
	put.Page = function(stream){
		return put('html').sendTo(stream);
	};
	var namespacePrefixes = {};
	var addNamespace = put.addNamespace;
	put.addNamespace = function(name, uri){
		namespacePrefixes[uri] = name;
		addNamespace(name, uri);
	}
};