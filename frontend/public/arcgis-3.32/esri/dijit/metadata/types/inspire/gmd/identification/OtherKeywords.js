// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
require({cache:{"url:esri/dijit/metadata/types/inspire/gmd/identification/templates/OtherKeywords.html":"\x3cdiv data-dojo-attach-point\x3d\"containerNode\"\x3e\r\n\r\n  \x3cdiv class\x3d\"gxeOtherKeywords\" data-dojo-type\x3d\"esri/dijit/metadata/form/iso/ObjectReference\"\r\n    data-dojo-props\x3d\"target:'gmd:descriptiveKeywords',minOccurs:0,maxOccurs:'unbounded',\r\n       label:'${i18nIso.AbstractMD_Identification.descriptiveKeywords}',\r\n       matchTopNode: [\r\n         {\r\n           qPath:'gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString',\r\n          qValue:'ISO 19119 service taxonomy',\r\n          qMode:'mustNot'\r\n        },\r\n         {\r\n           qPath:'gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString',\r\n          qValue:'GEMET - INSPIRE themes, version 1.0',\r\n          qMode:'mustNot'\r\n        },\r\n         {\r\n           qPath:'gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString',\r\n          qValue:'GEMET - Concepts, version 2.4',\r\n          qMode:'mustNot'\r\n        }  \r\n       ]\"\x3e           \r\n    \x3cdiv data-dojo-type\x3d\"esri/dijit/metadata/types/iso/gmd/identification/MD_Keywords\"\x3e\x3c/div\x3e\r\n  \x3c/div\x3e\r\n    \r\n\x3c/div\x3e"}});
define("esri/dijit/metadata/types/inspire/gmd/identification/OtherKeywords","dojo/_base/declare dojo/_base/lang dojo/has ../../../../base/Descriptor ../../../../form/iso/ObjectReference ../../../iso/gmd/identification/MD_Keywords dojo/text!./templates/OtherKeywords.html ../../../../../../kernel".split(" "),function(a,b,c,d,g,h,e,f){a=a(d,{templateString:e});c("extend-esri")&&b.setObject("dijit.metadata.types.inspire.gmd.identification.OtherKeywords",a,f);return a});