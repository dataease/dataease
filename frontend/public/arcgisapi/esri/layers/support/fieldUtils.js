// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("require exports ../../core/object ../../core/maybe ../../core/Error ../../core/promiseUtils ./domains ../../support/arcadeOnDemand".split(" "),function(O,d,v,k,P,C,m,Q){function D(a,b,c){if(a)for(const e of a)(a=(a=v.getDeepValue(e,b))&&"function"!==typeof a&&g(c,a))&&v.setDeepValue(e,a.name,b)}function l(a,b){if(!a||!b)return[];w.clear();n(w,a,b);return Array.from(w).sort()}function n(a,b,c){if(c)if(b&&b.length)if(c.includes("*"))for(const {name:e}of b)a.add(e);else for(const e of c)h(a,
b,e);else if(c.includes("*"))a.clear(),a.add("*");else for(const e of c)a.add(e)}function h(a,b,c){b&&b.length?(b=g(b,c))&&a.add(b.name):"string"===typeof c&&a.add(c)}function g(a,b){if("string"!==typeof b)return null;if(null!=a){b=b.toLowerCase();for(const c of a)if(c&&c.name.toLowerCase()===b)return c}return null}async function p(a,b,c){if(c){var {arcadeUtils:e}=await Q.loadArcade();c=e.extractFieldNames(c);for(const f of c)h(a,b,f)}}function x(a,b){for(const c of a)if(c&&c.valueType&&c.valueType===
b)return c.name;return null}async function E(a,b){if(b){var {fields:c}=b;if(b=v.getDeepValue("elevationInfo.featureExpressionInfo",b))return b.collectRequiredFields(a,c)}}async function R(a,b,c){c.outStatistic.onStatisticValueExpression?p(a,b,c.outStatistic.onStatisticValueExpression):a.add(c.outStatistic.onStatisticField)}async function F(a,b){const {labelingInfo:c,fields:e}=b;c&&c.length&&await C.all(c.map(f=>S(a,e,f)))}async function S(a,b,c){if(c){var e=c.getLabelExpression();c=c.where;"arcade"===
e.type?await p(a,b,e.expression):(e=e.expression.match(/{[^}]*}/g))&&e.forEach(f=>{h(a,b,f.slice(1,-1))});e=/['"]+/g;c&&(c=c.split(" "),3===c.length&&h(a,b,c[0].replace(e,"")),7===c.length&&(h(a,b,c[0].replace(e,"")),h(a,b,c[4].replace(e,""))))}}function y(a){return"number"===typeof a&&!isNaN(a)&&isFinite(a)}function T(a){return null===a||y(a)}function U(a){return null===a||q(a)}function G(a){return null!=a&&"string"===typeof a}function V(a){return null===a||G(a)}function W(){return!0}function z(a,
b){let c;switch(a.type){case "date":case "integer":case "long":case "small-integer":case "esriFieldTypeDate":case "esriFieldTypeInteger":case "esriFieldTypeLong":case "esriFieldTypeSmallInteger":c=a.nullable?U:q;break;case "double":case "single":case "esriFieldTypeSingle":case "esriFieldTypeDouble":c=a.nullable?T:y;break;case "string":case "esriFieldTypeString":c=a.nullable?V:G;break;default:c=W}return 1===arguments.length?c:c(b)}function A(a){return null!=a&&X.has(a.type)}function H(a,b){return a.nullable&&
null===b?null:A(a)&&!I(a.type,Number(b))?d.NumericRangeValidationError.OUT_OF_RANGE:z(a,b)?a.domain?m.validateDomainValue(a.domain,b):null:d.TypeValidationError.INVALID_TYPE}function I(a,b){return(a="string"===typeof a?B(a):a)?a.isInteger?q(b)&&b>=a.min&&b<=a.max:b>=a.min&&b<=a.max:!1}function B(a){switch(a){case "esriFieldTypeSmallInteger":case "small-integer":return r;case "esriFieldTypeInteger":case "integer":return t;case "esriFieldTypeSingle":case "single":return u;case "esriFieldTypeDouble":case "double":return J}}
function K(a,b,c){if(!b||!b.attributes||!a){if(k.isSome(c))for(var e of a)c.add(e);return!0}b=b.attributes;e=!1;for(const f of a)if(!(f in b))if(e=!0,k.isSome(c))c.add(f);else break;return e}const L="field field2 field3 normalizationField rotationInfo.field proportionalSymbolInfo.field proportionalSymbolInfo.normalizationField colorInfo.field colorInfo.normalizationField".split(" "),M=["field","normalizationField"],w=new Set,q=(()=>"isInteger"in Number?Number.isInteger:a=>"number"===typeof a&&isFinite(a)&&
Math.floor(a)===a)(),N=["integer","small-integer","single","double"],X=new Set([...N,"esriFieldTypeInteger","esriFieldTypeSmallInteger","esriFieldTypeSingle","esriFieldTypeDouble"]);(d.NumericRangeValidationError||(d.NumericRangeValidationError={})).OUT_OF_RANGE="numeric-range-validation-error::out-of-range";(d.TypeValidationError||(d.TypeValidationError={})).INVALID_TYPE="type-validation-error::invalid-type";const r={min:-32768,max:32767,isInteger:!0},t={min:-2147483648,max:2147483647,isInteger:!0},
u={min:-3.4E38,max:1.2E38,isInteger:!1},J={min:-Number.MAX_VALUE,max:Number.MAX_VALUE,isInteger:!1};d.collectArcadeFieldNames=p;d.collectElevationFields=E;d.collectFeatureReductionFields=async function(a,b,c){b&&c&&"cluster"===c.type&&c.fields&&await C.all(c.fields.map(e=>R(a,b.fields,e)))};d.collectField=h;d.collectFields=n;d.collectFilterFields=async function(a,b,c){if(b&&c&&(c.where&&"1\x3d1"!==c.where||c.timeExtent)&&(b.timeInfo&&c.timeExtent&&n(a,b.fields,[b.timeInfo.startField,b.timeInfo.endField]),
k.isSome(c.where)&&c.where)){c=(await new Promise(function(e,f){O(["../../core/sql/WhereClause"],e,f)})).WhereClause.create(c.where,b.fieldsIndex);if(!c.isStandardized)throw new P("fieldUtils:collectFilterFields","Where clause is not standardized");n(a,b.fields,c.fieldNames)}};d.collectLabelingFields=F;d.doubleRange=J;d.featureHasFields=function(a,b){return!K(a,b,null)};d.fixFields=l;d.fixRendererFields=function(a,b){if(null!=a&&null!=b)for(const c of Array.isArray(a)?a:[a])if(D(L,c,b),"visualVariables"in
c&&c.visualVariables)for(const e of c.visualVariables)D(M,e,b)};d.fixTimeInfoFields=function(a,b){if(null!=a&&null!=b)if("startField"in a){var c=g(b,a.startField);b=g(b,a.endField);a.startField=c&&c.name||null;a.endField=b&&b.name||null}else c=g(b,a.startTimeField),b=g(b,a.endTimeField),a.startTimeField=c&&c.name||null,a.endTimeField=b&&b.name||null};d.getDisplayFieldName=function({displayField:a,fields:b}){if(a)return a;if(!b||!b.length)return null;if(!(a=x(b,"name-or-title")||x(b,"unique-identifier")||
x(b,"type-or-category")))a:{for(const c of b)if(c&&c.name&&(b=c.name.toLowerCase(),-1<b.indexOf("name")||-1<b.indexOf("title"))){a=c.name;break a}a=null}return a};d.getElevationFields=async function(a){if(!a)return[];const b=new Set;await E(b,a);return Array.from(b).sort()};d.getExpressionFields=async function(a,b){const c=new Set;for(const e of b)await p(c,a.fields,e);return Array.from(c).sort()};d.getFeatureEditFields=function(a){if(!a)return[];const b="editFieldsInfo"in a&&a.editFieldsInfo;return b?
l(a.fields,[b&&b.creatorField,b&&b.creationDateField,b&&b.editorField,b&&b.editDateField]):[]};d.getFeatureGeometryFields=function(a){if(!a)return[];const b="geometryProperties"in a&&a.geometryProperties;return b?l(a.fields,[b&&b.shapeAreaFieldName,b&&b.shapeLengthFieldName]):[]};d.getField=g;d.getFieldDefaultValue=function(a){const b=a.defaultValue;if(void 0!==b&&z(a,b))return b;if(a.nullable)return null};d.getFieldRange=function(a){const b=m.getDomainRange(a.domain);if(b)return b;if(A(a))return B(a.type)};
d.getLabelingFields=async function(a){if(!a)return[];const b=new Set;await F(b,a);return Array.from(b).sort()};d.getNumericTypeForValue=function(a){if(!y(a))return null;if(q(a)){if(a>=r.min&&a<=r.max)return"esriFieldTypeSmallInteger";if(a>=t.min&&a<=t.max)return"esriFieldTypeInteger"}return a>=u.min&&a<=u.max?"esriFieldTypeSingle":"esriFieldTypeDouble"};d.getTimeFields=async function(a){if(!a)return[];const b="timeInfo"in a&&a.timeInfo;return b?l(a.fields,[a.trackIdField,b.startField,b.endField]):
[]};d.hasField=function(a,b){if(!a||!b||"string"!==typeof b)return!1;b=b.toLowerCase();for(const c of a)if(c&&c.name.toLowerCase()===b)return!0;return!1};d.integerRange=t;d.isDateField=function(a){return null!=a&&("date"===a.type||"esriFieldTypeDate"===a.type)};d.isNumberInRange=I;d.isNumericField=A;d.isStringField=function(a){return null!=a&&("string"===a.type||"esriFieldTypeString"===a.type)};d.isValidFieldValue=function(a,b){return null===H(a,b)};d.isValueMatchingFieldType=z;d.numericTypes=N;d.packFields=
function(a,b,c=1){if(!b||!a)return[];if(b.includes("*"))return["*"];b=l(a,b);return b.length/a.length>=c?["*"]:b};d.populateMissingFields=K;d.rendererFields=L;d.sanitizeNullFieldValue=function(a){return null==a||"number"===typeof a&&isNaN(a)?null:a};d.singleRange=u;d.smallIntegerRange=r;d.unpackFieldNames=function(a,b){return k.isNone(b)||k.isNone(a)?[]:b.includes("*")?a.map(c=>c.name):b};d.validateFieldValue=H;d.validationErrorToString=function(a,b,c){switch(a){case m.DomainValidationError.INVALID_CODED_VALUE:return`Value ${c} is not in the coded domain - field: ${b.name}, domain: ${JSON.stringify(b.domain)}`;
case m.DomainValidationError.VALUE_OUT_OF_RANGE:return`Value ${c} is out of the range of valid values - field: ${b.name}, domain: ${JSON.stringify(b.domain)}`;case d.TypeValidationError.INVALID_TYPE:return`Value ${c} is not a valid value for the field type - field: ${b.name}, type: ${b.type}, nullable: ${b.nullable}`;case d.NumericRangeValidationError.OUT_OF_RANGE:{const {min:e,max:f}=B(b.type);return`Value ${c} is out of range for the number type - field: ${b.name}, type: ${b.type}, value range is ${e} to ${f}`}}};
d.visualVariableFields=M;Object.defineProperty(d,"__esModule",{value:!0})});