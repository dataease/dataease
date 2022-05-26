/* A minimal version of put-selector, to be used for standalone */
	var selectorParse = /(?:\s*([-+ ,<>]))?\s*(\.|!\.?|#)?([-\w%$|]+)?(?:\[([^\]=]+)=?['"]?([^\]'"]*)['"]?\])?/g,
		ieCreateElement = typeof doc.createElement == "object"; // telltale sign of the old IE behavior with createElement that does not support later addition of name 
	function insertTextNode(element, text){
		element.appendChild(doc.createTextNode(text));
	}
	function put(referenceElement, selector, text){
		var fragment, lastSelectorArg, nextSibling, current = referenceElement,
			args = arguments,
			returnValue = args[0]; // use the first argument as the default return value in case only an element is passed in
		function insertLastElement(){
			// we perform insertBefore actions after the element is fully created to work properly with 
			// <input> tags in older versions of IE that require type attributes
			//	to be set before it is attached to a parent.
			// We also handle top level as a document fragment actions in a complex creation 
			// are done on a detached DOM which is much faster
			// Also if there is a parse error, we generally error out before doing any DOM operations (more atomic) 
			if(current && referenceElement && current != referenceElement){
				referenceElement.
								insertBefore(current, nextSibling || null); // do the actual insertion
			}
		}
				lastSelectorArg = true;
				var leftoverCharacters = selector.replace(selectorParse, function(t, combinator, prefix, value, attrName, attrValue){
					if(combinator){
						// insert the last current object
						insertLastElement();
						if(combinator == '-' || combinator == '+'){
							// + or - combinator, 
							// TODO: add support for >- as a means of indicating before the first child?
							referenceElement = (nextSibling = (current || referenceElement)).parentNode;
							current = null;
							if(combinator == "+"){
								nextSibling = nextSibling.nextSibling;
							}// else a - operator, again not in CSS, but obvious in it's meaning (create next element before the current/referenceElement)
						}else{
							if(combinator == "<"){
								// parent combinator (not really in CSS, but theorized, and obvious in it's meaning)
								referenceElement = current = (current || referenceElement).parentNode;
							}else{
								if(combinator == ","){
									// comma combinator, start a new selector
									referenceElement = topReferenceElement;
								}else if(current){
									// else descendent or child selector (doesn't matter, treated the same),
									referenceElement = current;
								}
								current = null;
							}
							nextSibling = 0;
						}
						if(current){
							referenceElement = current;
						}
					}
					var tag = !prefix && value;
					if(tag || (!current && (prefix || attrName))){
						// Need to create an element
						tag = tag || put.defaultTag;
						var ieInputName = ieCreateElement && args[i +1] && args[i +1].name;
						if(ieInputName){
							// in IE, we have to use the crazy non-standard createElement to create input's that have a name 
							tag = '<' + tag + ' name="' + ieInputName + '">';
						}
						// we swtich between creation methods based on namespace usage
						current = doc.createElement(tag);
					}
					if(prefix){
						if(prefix == "#"){
							// #id was specified
							current.id = value;
						}else{
							// we are in the className addition and removal branch
							var currentClassName = current.className;
							// remove the className (needed for addition or removal)
							// see http://jsperf.com/remove-class-name-algorithm/2 for some tests on this
							var removed = currentClassName && (" " + currentClassName + " ").replace(" " + value + " ", " ");
							if(prefix == "."){
								// addition, add the className
								current.className = currentClassName ? (removed + value).substring(1) : value;
							}
						}
					}
					if(attrName){
						var method = attrName.charAt(0) == "!" ? (attrName = attrName.substring(1)) && 'removeAttribute' : 'setAttribute';
						attrValue = attrValue === '' ? attrName : attrValue;
						// determine if we need to use a namespace
						current[method](attrName, attrValue);
					}
					return '';
				});
				insertLastElement();
				if(text){
					insertTextNode(current, text);
				}
				return current;
			}	
