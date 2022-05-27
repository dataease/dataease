// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../core/maybe ../../core/arrayUtils ../../renderers/visualVariables/support/sizeVariableUtils ../../core/MD5 ./utils ../popup/support/utils".split(" "),function(d,n,u,v,w,x,y){function p(b,a){if(b){var {field:c,valueExpression:e,normalizationField:f,normalizationType:k,normalizationTotal:l}=b;var h=null;if(e)h=e;else if(c){var g=x.getNormalizationType({normalizationType:k,normalizationField:f,normalizationTotal:l});g&&(g=g.toLowerCase(),h=c.toLowerCase()+",norm:"+g,f?h+=","+f.toLowerCase():
"percent-of-total"===g&&(g=l,v.isValidNumber(g)&&0!==g||(g=null),h+=","+g))}}else h=null;b=n.isSome(h)?w.createMD5Hash(h):b.field;return`${a}_${b}`}function q(b,a){return(b=a.getField(b))&&b.type}function r(b,a){const c="field"in a&&a.field;b=c?q(c,b):null;return{field:c,fieldType:n.isSome(b)?b:null,valueExpression:"valueExpression"in a?a.valueExpression:null,valueExpressionTitle:"valueExpressionTitle"in a?a.valueExpressionTitle:null,normalizationField:"normalizationField"in a?a.normalizationField:
null,normalizationType:"normalizationType"in a?a.normalizationType:null,normalizationTotal:"normalizationTotal"in a?a.normalizationTotal:null}}function t(b,a){const c="rotation"===a.type?a.rotationType:null,e=a.legendOptions&&a.legendOptions.title,f=a.field;b=f?q(f,b):null;return{field:f,fieldType:n.isSome(b)?b:null,rotationType:c,valueExpression:a.valueExpression,valueExpressionTitle:a.valueExpressionTitle||a.valueExpression&&e,normalizationField:"normalizationField"in a?a.normalizationField:null,
vvType:a.type}}function z(b){return b.map(a=>{var c=String(a.value);a=(a=String(a.label))?a.replace(/"/g,'\\"'):"";return`{
        "value": "${c}",
        "label": "${a}"
      }`})}function m(b,a){var c=[p(b,a)];"date"===b.fieldType&&c.push(b.fieldType.toLowerCase());b.rotationType&&c.push(b.rotationType.toLowerCase());c=c.join("_");return{statisticHash:c,attributeInfo:b,statisticType:a}}d.clusterCountField="cluster_count";d.getClusterField=function(b,a){return`cluster_${p(b,a)}`};d.getClusterFieldHash=function(b,a){return b.split(`cluster_${a}_`).pop()};d.getPredominantTypeExpression=function(b,a,c){return`
  var uvInfos = [${z(b).join(", ")}];
  var predominantType = Text($feature["${a}"]);
  var label = "${c?c.replace(/"/g,'\\"'):""}";

  for (var i = 0; i < Count(uvInfos); i++) {
    if (uvInfos[i].value == predominantType) {
      label = uvInfos[i].label;
      break;
    }
  }

  return label;
  `};d.getRendererAttributeInfo=r;d.getStatisticId=p;d.getStatisticInfo=m;d.getStatisticInfos=function(b,a,c=!0){const e=[],f=r(b,a);"class-breaks"===a.type?e.push(m(f,"avg")):"unique-value"===a.type&&e.push(m(f,"type"));a=y.getPrimaryVisualVariables(a);for(const k of a)a=t(b,k),e.push(m(a,"avg"));return c?u.unique(e,(k,l)=>k.statisticHash===l.statisticHash):e};d.getVariableAttributeInfo=t;Object.defineProperty(d,"__esModule",{value:!0})});