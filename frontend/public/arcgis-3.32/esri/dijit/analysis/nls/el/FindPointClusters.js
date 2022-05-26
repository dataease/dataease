// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/analysis/nls/el/FindPointClusters",{clustersLabel:"\u0393\u03b9\u03b1 \u03c4\u03bf \u03b8\u03b5\u03bc\u03b1\u03c4\u03b9\u03ba\u03cc \u03b5\u03c0\u03af\u03c0\u03b5\u03b4\u03bf ${inputLayerName} \u03b2\u03c1\u03b5\u03af\u03c4\u03b5 \u03c4\u03b9\u03c2 \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03b5\u03c2.",chooseLayerLabel:"\u0395\u03c0\u03b9\u03bb\u03ad\u03be\u03c4\u03b5 \u03c4\u03bf \u03b8\u03b5\u03bc\u03b1\u03c4\u03b9\u03ba\u03cc \u03b5\u03c0\u03af\u03c0\u03b5\u03b4\u03bf \u03b3\u03b9\u03b1 \u03c4\u03bf \u03bf\u03c0\u03bf\u03af\u03bf \u03b8\u03b1 \u03b2\u03c1\u03b5\u03b8\u03bf\u03cd\u03bd \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03b5\u03c2.",
minClusterPtsLabel:"\u0395\u03bb\u03ac\u03c7\u03b9\u03c3\u03c4\u03bf\u03c2 \u03b1\u03c1\u03b9\u03b8\u03bc\u03cc\u03c2 \u03c3\u03b7\u03bc\u03b5\u03af\u03c9\u03bd \u03c0\u03bf\u03c5 \u03bc\u03c0\u03bf\u03c1\u03b5\u03af \u03bd\u03b1 \u03b8\u03b5\u03c9\u03c1\u03b7\u03b8\u03b5\u03af \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03b1",minClustersPtsGAXLbl:"\u0395\u03bb\u03ac\u03c7\u03b9\u03c3\u03c4\u03bf\u03c2 \u03b1\u03c1\u03b9\u03b8\u03bc\u03cc\u03c2 \u03c3\u03b7\u03bc\u03b5\u03af\u03c9\u03bd \u03c0\u03bf\u03c5 \u03bc\u03c0\u03bf\u03c1\u03bf\u03cd\u03bd \u03bd\u03b1 \u03c3\u03c5\u03bd\u03b8\u03ad\u03c3\u03bf\u03c5\u03bd \u03bc\u03b9\u03b1 \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03b1",
limitSearchLabel:"\u03a0\u03b5\u03c1\u03b9\u03bf\u03c1\u03b9\u03c3\u03bc\u03cc\u03c2 \u03c4\u03bf\u03c5 \u03b5\u03cd\u03c1\u03bf\u03c5\u03c2 \u03b1\u03bd\u03b1\u03b6\u03ae\u03c4\u03b7\u03c3\u03b7\u03c2 \u03c3\u03b5 (\u03c0\u03c1\u03bf\u03b1\u03b9\u03c1\u03b5\u03c4\u03b9\u03ba\u03ac)",limitSearchReqLabel:"\u03a0\u03b5\u03c1\u03b9\u03bf\u03c1\u03b9\u03c3\u03bc\u03cc\u03c2 \u03c4\u03bf\u03c5 \u03b5\u03cd\u03c1\u03bf\u03c5\u03c2 \u03b1\u03bd\u03b1\u03b6\u03ae\u03c4\u03b7\u03c3\u03b7\u03c2 \u03c3\u03b5",
outputLayerName:"\u0395\u03cd\u03c1\u03b5\u03c3\u03b7 \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03c9\u03bd \u03b3\u03b9\u03b1 \u03c4\u03bf \u03b8\u03b5\u03bc\u03b1\u03c4\u03b9\u03ba\u03cc \u03b5\u03c0\u03af\u03c0\u03b5\u03b4\u03bf ${inputLayerName}",itemDescription:"\u0394\u03b7\u03bc\u03b9\u03bf\u03c5\u03c1\u03b3\u03ae\u03b8\u03b7\u03ba\u03b5 feature layer \u03b1\u03c0\u03cc \u03c4\u03b7\u03bd \u03b5\u03ba\u03c4\u03ad\u03bb\u03b5\u03c3\u03b7 \u03c4\u03b7\u03c2 \u03bb\u03cd\u03c3\u03b7\u03c2 \u00ab\u0395\u03cd\u03c1\u03b5\u03c3\u03b7 \u03a3\u03c5\u03c3\u03c4\u03ac\u03b4\u03c9\u03bd \u03a3\u03b7\u03bc\u03b5\u03af\u03c9\u03bd\u00bb \u03b3\u03b9\u03b1 \u03c4\u03bf \u03b8\u03b5\u03bc\u03b1\u03c4\u03b9\u03ba\u03cc \u03b5\u03c0\u03af\u03c0\u03b5\u03b4\u03bf ${inputLayerName}.",
itemTags:"\u0391\u03c0\u03bf\u03c4\u03ad\u03bb\u03b5\u03c3\u03bc\u03b1 \u0391\u03bd\u03ac\u03bb\u03c5\u03c3\u03b7\u03c2, \u0395\u03cd\u03c1\u03b5\u03c3\u03b7 \u03a3\u03c5\u03c3\u03c4\u03ac\u03b4\u03c9\u03bd \u03a3\u03b7\u03bc\u03b5\u03af\u03c9\u03bd, \u0398\u03b5\u03bc\u03b1\u03c4\u03b9\u03ba\u03cc \u03b5\u03c0\u03af\u03c0\u03b5\u03b4\u03bf ${inputLayerName}",itemSnippet:"\u0395\u03c0\u03b9\u03c4\u03c5\u03c7\u03ae\u03c2 \u03b4\u03b7\u03bc\u03b9\u03bf\u03c5\u03c1\u03b3\u03af\u03b1 feature layer \u03b1\u03c0\u03cc \u03c4\u03b7 \u03bb\u03cd\u03c3\u03b7 \u00ab\u0395\u03cd\u03c1\u03b5\u03c3\u03b7 \u03a3\u03c5\u03c3\u03c4\u03ac\u03b4\u03c9\u03bd \u03a3\u03b7\u03bc\u03b5\u03af\u03c9\u03bd\u00bb",
chooseClusteringMethod:"\u0395\u03c0\u03b9\u03bb\u03bf\u03b3\u03ae \u03bc\u03b5\u03b8\u03cc\u03b4\u03bf\u03c5 \u03b4\u03b7\u03bc\u03b9\u03bf\u03c5\u03c1\u03b3\u03af\u03b1\u03c2 \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03c9\u03bd \u03c0\u03c1\u03bf\u03c2 \u03c7\u03c1\u03ae\u03c3\u03b7",dbscanLabel:"\u039f\u03c1\u03b9\u03c3\u03bc\u03ad\u03bd\u03b7\u03c2 \u03b1\u03c0\u03cc\u03c3\u03c4\u03b1\u03c3\u03b7\u03c2 (DBSCAN)",hdbscanLabel:"\u0391\u03c5\u03c4\u03bf\u03c1\u03c5\u03b8\u03bc\u03b9\u03b6\u03cc\u03bc\u03b5\u03bd\u03b7 (HDBSCAN)",
useTimeToFindCLusters:"\u03a7\u03c1\u03ae\u03c3\u03b7 \u03c7\u03c1\u03bf\u03bd\u03b9\u03ba\u03ce\u03bd \u03c0\u03bb\u03b7\u03c1\u03bf\u03c6\u03bf\u03c1\u03b9\u03ce\u03bd \u03b3\u03b9\u03b1 \u03c4\u03b7\u03bd \u03b5\u03cd\u03c1\u03b5\u03c3\u03b7 \u03c3\u03c5\u03c3\u03c4\u03ac\u03b4\u03c9\u03bd",limitSearchDurLabel:"\u03a0\u03b5\u03c1\u03b9\u03bf\u03c1\u03b9\u03c3\u03bc\u03cc\u03c2 \u03c4\u03b7\u03c2 \u03b4\u03b9\u03ac\u03c1\u03ba\u03b5\u03b9\u03b1\u03c2 \u03c4\u03b7\u03c2 \u03b1\u03bd\u03b1\u03b6\u03ae\u03c4\u03b7\u03c3\u03b7\u03c2 \u03c3\u03b5"});