// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../support/utils"],function(f,p){function q(a){return`(${a.map(b=>`${b} >= 0`).join(" OR ")})`}function r(a,b){const {returnFieldName:d,defaultValue:g,layer:k}=b;b=[];if(d&&g){var e=a.map(c=>`${c} <= 0`).join(" AND ");b.push(`WHEN ${e} THEN ${g}`)}for(const c of a){e=a.reduce((m,t)=>{c!==t&&m.push(`${c} > ${t}`);return m},[]).join(" AND ");var h=k&&p.isIntegerField(k,c);h=d&&`'${c}'`?`'${c}'`:h?p.castIntegerFieldToFloat(c):c;b.push(`WHEN ${e} THEN ${h}`)}return`CASE ${b.join(" ")} ELSE ${g||
"0"} END`}function l(a){return a&&a.map(b=>`$feature["${b}"];`).join("\n")+"\n"||""}function n(a,b=!1){a=a.map(d=>`"${d}"`);return`
  var fieldNames = [ ${a.join(", ")} ];
  var numFields = ${a.length};
  var maxValueField = null;
  var maxValue = -Infinity;
  var value, i, totalValue = null;

  for(i = 0; i < numFields; i++) {
    value = $feature[fieldNames[i]];

    if(value > 0) {
      if(value > maxValue) {
        maxValue = value;
        maxValueField = fieldNames[i];
      }
      else if (value == maxValue) {
        maxValueField = null;
      }
    }
    ${b?"\n  if(value !\x3d null \x26\x26 value \x3e\x3d 0) {\n    if (totalValue \x3d\x3d null) { totalValue \x3d 0; }\n    totalValue \x3d totalValue + value;\n  }\n  ":""}
  }
  `}function u(a){const b=n(a);return`
  ${l(a)}
  ${b}
  return maxValueField;
  `}function v(a){const b=l(a);a=a.map(d=>`"${d}"`);return`
  ${b}
  var fieldNames = [ ${a.join(", ")} ];
  var numFields = ${a.length};
  var value, i, totalValue = null;

  for(i = 0; i < numFields; i++) {
    value = $feature[fieldNames[i]];

    if(value != null && value >= 0) {
      if (totalValue == null) { totalValue = 0; }
      totalValue = totalValue + value;
    }
  }

  return totalValue;
  `}function w(a){const b=n(a,!0);return`
  ${l(a)}
  ${b}

  var strength = null;

  if (maxValueField != null && totalValue > 0) {
    strength = (maxValue / totalValue) * 100;
  }

  return strength;
  `}f.getArcadeForOrderedFields=function(a,b){var d=a.map(e=>e.fieldName);d=l(d);const g=a.map(e=>`{
    value: Number($feature["${e.fieldName}"]),
    alias: "${e.label||e.fieldName}"
    }`),k=[];b&&b.forEach((e,h)=>{k.push(`function getValueForExpr${h}() {\n  ${e.expression} \n}`);g.push(`{
          value: Number(getValueForExpr${h}()),
          alias: "${e.title||e.name}"
          }`)});return`
  ${d}

  ${k.length?k.join("\n"):""}

  var groups = [ ${g.join(", ")} ];
  var numTopValues = Count(groups);

  function getValuesArray(arr){
    var valuesArray = []
    for(var i in arr){
      valuesArray[i] = arr[i].value;
    }
    return valuesArray;
  }

  function findAliases(top5Array, fullArray){
    var aliases = [];
    var found = "";
    for(var i in top5Array){
      for(var k in fullArray){
        if(top5Array[i] == fullArray[k].value && Find(fullArray[k].alias, found) == -1){
          found += fullArray[k].alias;
          aliases[Count(aliases)] = {
            alias: fullArray[k].alias,
            value: top5Array[i]
          };
        }
      }
    }
    return aliases;
  }

  function getTopGroups(groupsArray){
    var values = getValuesArray(groupsArray);
    var top5Values = IIF(Max(values) > 0, Top(Reverse(Sort(values)),numTopValues), "no data");
    var top5Aliases = findAliases(top5Values,groupsArray);

    if(TypeOf(top5Values) == "String"){
      return top5Values;
    } else {
      var content = "";
      for(var i in top5Aliases){
        if(top5Aliases[i].value > 0){
          content += (i+1) + ". " + top5Aliases[i].alias + " (" + Text(top5Aliases[i].value, "#,###") + ")";
          content += IIF(i < numTopValues-1, TextFormatting.NewLine, "");
        }
      }
    }

    return content;
  }

  getTopGroups(groups);
  `};f.getArcadeForPredominanceMargin=function(a){const b=l(a);a=a.map(d=>`$feature["${d}"]`);return`
  ${b}
  var fieldValues = [ ${a.join(", ")} ];
  var totalValue = Sum(fieldValues);
  var order = Reverse(Sort(fieldValues));
  return IIF(totalValue > 0, Round(((order[0] - order[1]) / totalValue) * 100, 2), null);
  `};f.getArcadeForPredominantCategory=u;f.getArcadeForPredominantCategoryAlias=function(a,b){const d=a.map(c=>c.fieldName),g=l(d),k=a.map(c=>c.label?`"${c.label}"`:`"${c.fieldName}"`),e=d.map(c=>`Number($feature["${c}"])`),h=[];b&&b.forEach((c,m)=>{h.push(`function getValueForExpr${m}() {\n  ${c.expression} \n}`);e.push(`Number(getValueForExpr${m}())`);k.push(`"${c.title||c.name}"`)});return`
  ${g}

  ${h.length?h.join("\n"):""}

  var values = [ ${e.join(", ")} ];
  var aliases = [ ${k.join(", ")} ];
  var numValues = ${e.length};
  var maxValueAlias = null;
  var maxValue = -Infinity;
  var value, i, totalValue = null;

  for(i = 0; i < numValues; i++) {
    value = values[i];

    if(value > 0) {
      if(value > maxValue) {
        maxValue = value;
        maxValueAlias = aliases[i];
      }
      else if (value == maxValue) {
        maxValueAlias = "Tie";
      }
    }
  }
  return maxValueAlias;
  `};f.getArcadeForPredominantCategoryValue=function(a){const b=n(a);return`
  ${l(a)}
  ${b}
  return maxValue;
  `};f.getArcadeForStrengthOfPredominance=w;f.getArcadeForSumOfFields=v;f.getPredominanceExpressions=function(a,b){const d=b.join(" + "),g={sqlExpression:`(${d})`,sqlWhere:q(b)};a={sqlExpression:`(( (${r(b,{layer:a})}) / (${d}) ) * 100)`,sqlWhere:`${q(b)} AND ((${d}) > 0)`};return{predominantCategory:{valueExpression:u(b)},size:{valueExpression:v(b),statisticsQuery:g,histogramQuery:g},opacity:{valueExpression:w(b),statisticsQuery:a,histogramQuery:a}}};f.getSQLForPredominantCategoryName=function(a){return{expression:r(a,
{returnFieldName:!0,defaultValue:"'no_dominant_category'"})}};f.noDominantCategoryField="no_dominant_category";Object.defineProperty(f,"__esModule",{value:!0})});