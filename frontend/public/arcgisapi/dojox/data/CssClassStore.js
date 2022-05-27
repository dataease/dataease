//>>built
define(["dojo/_base/declare","dojox/data/CssRuleStore"],function(h,k){return h("dojox.data.CssClassStore",k,{_labelAttribute:"class",_idAttribute:"class",_cName:"dojox.data.CssClassStore",getFeatures:function(){return{"dojo.data.api.Read":!0,"dojo.data.api.Identity":!0}},getAttributes:function(a){this._assertIsItem(a);return["class","classSans"]},getValue:function(a,b,c){return(a=this.getValues(a,b))&&0<a.length?a[0]:c},getValues:function(a,b){this._assertIsItem(a);this._assertIsAttribute(b);var c=
[];"class"===b?c=[a.className]:"classSans"===b&&(c=[a.className.replace(/\./g,"")]);return c},_handleRule:function(a,b,c){b={};a=a.selectorText.split(" ");for(c=0;c<a.length;c++){var d=a[c],e=d.indexOf(".");if(d&&0<d.length&&-1!==e){var f=d.indexOf(",")||d.indexOf("[");d=d.substring(e,-1!==f&&f>e?f:d.length);b[d]=!0}}for(var g in b)this._allItems[g]||(b={},b.className=g,b[this._storeRef]=this,this._allItems[g]=b)},_handleReturn:function(){var a=[],b={};for(c in this._allItems)b[c]=this._allItems[c];
for(;this._pending.length;){var c=this._pending.pop();c.request._items=b;a.push(c)}for(;a.length;)c=a.pop(),c.fetch?this._handleFetchReturn(c.request):this._handleFetchByIdentityReturn(c.request)},_handleFetchByIdentityReturn:function(a){var b=a._items[a.identity];this.isItem(b)||(b=null);a.onItem&&a.onItem.call(a.scope||dojo.global,b)},getIdentity:function(a){this._assertIsItem(a);return this.getValue(a,this._idAttribute)},getIdentityAttributes:function(a){this._assertIsItem(a);return[this._idAttribute]},
fetchItemByIdentity:function(a){a=a||{};a.store||(a.store=this);this._pending&&0<this._pending.length?this._pending.push({request:a}):(this._pending=[{request:a}],this._fetch(a));return a}})});