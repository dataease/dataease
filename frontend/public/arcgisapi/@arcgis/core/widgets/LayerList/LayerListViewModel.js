/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as s}from"../../chunks/tslib.es6.js";import"../../chunks/ArrayPool.js";import"../../chunks/object.js";import"../../chunks/deprecate.js";import"../../core/lang.js";import"../../config.js";import"../../chunks/Logger.js";import"../../chunks/string.js";import"../../chunks/metadata.js";import{property as t}from"../../core/accessorSupport/decorators/property.js";import"../../core/Accessor.js";import"../../chunks/PropertyOrigin.js";import"../../core/scheduling.js";import"../../core/promiseUtils.js";import"../../chunks/Message.js";import"../../core/Error.js";import"../../chunks/compilerUtils.js";import"../../chunks/ensureType.js";import{subclass as o}from"../../core/accessorSupport/decorators/subclass.js";import{E as e}from"../../chunks/Evented.js";import r from"../../core/Collection.js";import"../../chunks/collectionUtils.js";import"../../chunks/JSONSupport.js";import"../../chunks/Promise.js";import"../../chunks/Loadable.js";import"../../core/urlUtils.js";import"../../core/accessorSupport/decorators/aliasOf.js";import"../../core/accessorSupport/decorators/cast.js";import"../../chunks/jsonMap.js";import"../../chunks/enumeration.js";import"../../chunks/reader.js";import"../../chunks/writer.js";import"../../chunks/resourceExtension.js";import"../../chunks/persistableUrlUtils.js";import"../../geometry/SpatialReference.js";import"../../chunks/locale.js";import"../../chunks/number.js";import"../../intl.js";import"../../kernel.js";import"../../request.js";import"../../chunks/assets.js";import"../../geometry/Geometry.js";import"../../geometry/Point.js";import"../../chunks/Ellipsoid.js";import"../../geometry/support/webMercatorUtils.js";import"../../geometry/Extent.js";import"../../portal/PortalQueryParams.js";import"../../portal/PortalQueryResult.js";import"../../portal/PortalFolder.js";import"../../portal/PortalGroup.js";import"../../portal/PortalUser.js";import"../../portal/Portal.js";import"../../chunks/mathUtils2.js";import"../../chunks/colorUtils.js";import"../../Color.js";import"../../chunks/zmUtils.js";import"../../geometry/Multipoint.js";import"../../geometry/Polygon.js";import"../../chunks/extentUtils.js";import"../../geometry/Polyline.js";import"../../chunks/typeUtils.js";import"../../geometry/support/jsonUtils.js";import"../../geometry.js";import"../../layers/support/CodedValueDomain.js";import"../../layers/support/Domain.js";import"../../layers/support/InheritedDomain.js";import"../../layers/support/RangeDomain.js";import"../../chunks/domains.js";import"../../chunks/arcadeOnDemand.js";import"../../layers/support/fieldUtils.js";import"../../popup/content/Content.js";import"../../popup/content/AttachmentsContent.js";import"../../popup/content/CustomContent.js";import"../../chunks/date.js";import"../../popup/support/FieldInfoFormat.js";import"../../popup/FieldInfo.js";import"../../popup/content/FieldsContent.js";import"../../chunks/MediaInfo.js";import"../../popup/content/support/ChartMediaInfoValueSeries.js";import"../../popup/content/support/ChartMediaInfoValue.js";import"../../chunks/chartMediaInfoUtils.js";import"../../popup/content/BarChartMediaInfo.js";import"../../popup/content/ColumnChartMediaInfo.js";import"../../popup/content/support/ImageMediaInfoValue.js";import"../../popup/content/ImageMediaInfo.js";import"../../popup/content/LineChartMediaInfo.js";import"../../popup/content/PieChartMediaInfo.js";import"../../popup/content/MediaContent.js";import"../../popup/content/TextContent.js";import"../../popup/content.js";import"../../popup/ExpressionInfo.js";import"../../popup/LayerOptions.js";import"../../popup/support/RelatedRecordsInfoFieldOrder.js";import"../../popup/RelatedRecordsInfo.js";import"../../chunks/Identifiable.js";import"../../support/actions/ActionBase.js";import"../../support/actions/ActionButton.js";import"../../support/actions/ActionToggle.js";import"../../PopupTemplate.js";import"../../symbols/Symbol.js";import"../../symbols/CIMSymbol.js";import"../../symbols/Symbol3DLayer.js";import"../../chunks/screenUtils.js";import"../../chunks/opacityUtils.js";import"../../chunks/materialUtils.js";import"../../symbols/edges/Edges3D.js";import"../../symbols/edges/SketchEdges3D.js";import"../../symbols/edges/SolidEdges3D.js";import"../../chunks/utils.js";import"../../chunks/Symbol3DMaterial.js";import"../../symbols/ExtrudeSymbol3DLayer.js";import"../../symbols/LineSymbol.js";import"../../symbols/LineSymbolMarker.js";import"../../symbols/SimpleLineSymbol.js";import"../../symbols/FillSymbol.js";import"../../symbols/patterns/StylePattern3D.js";import"../../symbols/FillSymbol3DLayer.js";import"../../chunks/colors.js";import"../../chunks/Symbol3DOutline.js";import"../../symbols/Font.js";import"../../symbols/IconSymbol3DLayer.js";import"../../symbols/LineSymbol3DLayer.js";import"../../symbols/ObjectSymbol3DLayer.js";import"../../symbols/PathSymbol3DLayer.js";import"../../symbols/TextSymbol3DLayer.js";import"../../symbols/WaterSymbol3DLayer.js";import"../../symbols/Symbol3D.js";import"../../chunks/Thumbnail.js";import"../../symbols/callouts/Callout3D.js";import"../../symbols/callouts/LineCallout3D.js";import"../../chunks/Symbol3DVerticalOffset.js";import"../../symbols/LabelSymbol3D.js";import"../../symbols/LineSymbol3D.js";import"../../symbols/MarkerSymbol.js";import"../../symbols/MeshSymbol3D.js";import"../../chunks/urlUtils.js";import"../../symbols/PictureFillSymbol.js";import"../../symbols/PictureMarkerSymbol.js";import"../../symbols/PointSymbol3D.js";import"../../symbols/PolygonSymbol3D.js";import"../../symbols/SimpleFillSymbol.js";import"../../symbols/SimpleMarkerSymbol.js";import"../../symbols/TextSymbol.js";import"../../symbols/WebStyleSymbol.js";import"../../symbols/support/jsonUtils.js";import"../../chunks/uid.js";import"../../Graphic.js";import i from"../../core/Handles.js";import"../../chunks/LegendOptions.js";import"../../renderers/support/AuthoringInfo.js";import"../../renderers/support/AuthoringInfoVisualVariable.js";import"../../tasks/support/ColorRamp.js";import"../../tasks/support/AlgorithmicColorRamp.js";import"../../tasks/support/MultipartColorRamp.js";import"../../chunks/colorRamps.js";import"../../renderers/Renderer.js";import"../../renderers/visualVariables/VisualVariable.js";import"../../renderers/visualVariables/support/ColorStop.js";import"../../renderers/visualVariables/ColorVariable.js";import"../../renderers/visualVariables/support/OpacityStop.js";import"../../renderers/visualVariables/OpacityVariable.js";import"../../renderers/visualVariables/RotationVariable.js";import"../../renderers/visualVariables/support/SizeStop.js";import"../../renderers/visualVariables/SizeVariable.js";import"../../chunks/sizeVariableUtils.js";import"../../chunks/unitUtils.js";import"../../chunks/lengthUtils.js";import"../../chunks/visualVariableUtils.js";import"../../chunks/VisualVariablesMixin.js";import"../../renderers/support/ClassBreakInfo.js";import"../../chunks/commonProperties.js";import"../../renderers/ClassBreaksRenderer.js";import"../../chunks/diffUtils.js";import"../../renderers/support/UniqueValueInfo.js";import"../../chunks/devEnvironmentUtils.js";import"../../chunks/styleUtils.js";import"../../renderers/UniqueValueRenderer.js";import"../../chunks/MemCache.js";import"../../chunks/LRUCache.js";import"../../renderers/DictionaryRenderer.js";import"../../renderers/support/AttributeColorInfo.js";import"../../renderers/DotDensityRenderer.js";import"../../renderers/support/HeatmapColorStop.js";import"../../renderers/HeatmapRenderer.js";import"../../renderers/SimpleRenderer.js";import"../../renderers/support/jsonUtils.js";import"../../chunks/timeUtils.js";import"../../TimeExtent.js";import"../../chunks/ReadOnlyMultiOriginJSONSupport.js";import"../../chunks/MultiOriginJSONSupport.js";import{init as p,watch as m}from"../../core/watchUtils.js";import"../../chunks/fieldType.js";import"../../core/HandleOwner.js";import"../../chunks/domUtils.js";import"../../chunks/widgetUtils.js";import"../../chunks/projector.js";import"../../chunks/accessibleHandler.js";import"../../chunks/messageBundle.js";import"../../chunks/renderable.js";import"../../chunks/vmEvent.js";import"../../chunks/index.js";import"../support/widget.js";import"../Widget.js";import"../../layers/support/Field.js";import"../../chunks/labelUtils.js";import"../../layers/support/LabelClass.js";import"../../chunks/defaultsJSON.js";import"../../chunks/defaults.js";import"../../layers/support/FeatureTemplate.js";import"../../layers/support/FeatureType.js";import"../../layers/support/FieldsIndex.js";import"../../chunks/DataLayerSource.js";import"../../support/popupUtils.js";import"../../tasks/support/Query.js";import"../../tasks/support/StatisticDefinition.js";import"../../layers/support/Sublayer.js";import"../../chunks/ActionSlider.js";import"./ListItemPanel.js";import{h as l,b as n}from"../../chunks/layerListUtils.js";import a from"./ListItem.js";const c="view",u="view-layers",j="map-layers",d="layer-views",h="layer-list-mode",y=r.ofType(a);let b=class extends e.EventedAccessor{constructor(s){super(s),this._handles=new i,this.listItemCreatedFunction=null,this.operationalItems=new y,this.view=null}initialize(){this._handles.add(p(this,["view","view.ready"],(()=>this._viewHandles())),c)}destroy(){this._handles.destroy(),this._handles=null,this.view=null,this.operationalItems.removeAll()}get state(){const s=this.get("view");return this.get("view.ready")?"ready":s?"loading":"disabled"}triggerAction(s,t){s&&!s.disabled&&this.emit("trigger-action",{action:s,item:t})}moveListItem(s,t,o,e){var r,i;const p=null==s?void 0:s.layer;if(!p)return;const m=null==(r=this.view)||null==(i=r.map)?void 0:i.layers,n=t?l(t):m,a=o?l(o):m;if(!n||!a)return;const{operationalItems:c}=this,u=(null==t?void 0:t.children)||c,j=(null==o?void 0:o.children)||c,d=a.length-e;s.parent=o||null,u.includes(s)&&u.remove(s),n.includes(p)&&n.remove(p),j.includes(s)||j.add(s,d),a.includes(p)||a.add(p,d)}_createLayerViewHandles(s){const{_handles:t}=this;t.remove(d),this._compileList(),s&&t.add(s.on("change",(()=>this._compileList())),d)}_createMapLayerHandles(s){const{_handles:t}=this;t.remove(j),this._compileList(),s&&t.add(s.on("change",(()=>this._compileList())),j)}_watchItemProperties(s){this._handles.add([s.children.on("change",(()=>{this._modifyListItemChildren(s.children)}))],`children-change-${s.uid}`)}_modifyListItemChildren(s){s.forEach((s=>this._modifyListItem(s)))}_modifyListItem(s){if("function"==typeof this.listItemCreatedFunction){const t={item:s};this.listItemCreatedFunction.call(null,t)}this._modifyListItemChildren(s.children)}_createListItem(s){const{view:t}=this,o=new a({layer:s,view:t});return this._watchItemProperties(o),o}_removeAllItems(){const{_handles:s,operationalItems:t}=this;t.forEach((t=>{s.remove(`children-change-${t.uid}`)})),t.removeAll()}_getViewableLayers(s){if(s)return s.filter((s=>"hide"!==n(s)))}_watchLayersListMode(s){const{_handles:t}=this;t.remove(h),s&&s.forEach((s=>{t.add(m(s,"listMode",(()=>this._compileList())),h)}))}_compileList(){const s=this.get("view.map.layers");this._watchLayersListMode(s);const t=this._getViewableLayers(s);t&&t.length?(this._createNewItems(t),this._modifyOrRemoveItems(t),this._sortItems(t)):this._removeAllItems()}_createNewItems(s){const{operationalItems:t}=this;s.forEach((s=>{t.find((t=>t.layer===s))||t.add(this._createListItem(s))}))}_modifyOrRemoveItems(s){const{_handles:t,operationalItems:o}=this,e=[];o.forEach((o=>{o&&s&&s.find((s=>o.layer===s))?this._modifyListItem(o):(t.remove(`children-change-${o.uid}`),e.push(o))})),o.removeMany(e)}_sortItems(s){const{operationalItems:t}=this;t.sort(((t,o)=>{const e=s.indexOf(t.layer),r=s.indexOf(o.layer);return e>r?-1:e<r?1:0}))}_viewHandles(){const{_handles:s,view:t}=this;s.remove([j,d,u]),this._compileList(),t&&t.ready&&s.add([p(this,"view.map.layers",(s=>this._createMapLayerHandles(s))),p(this,"view.layerViews",(s=>this._createLayerViewHandles(s))),p(this,"listItemCreatedFunction",(()=>this._compileList()))],u)}};s([t()],b.prototype,"listItemCreatedFunction",void 0),s([t({type:y})],b.prototype,"operationalItems",void 0),s([t({dependsOn:["view.ready"],readOnly:!0})],b.prototype,"state",null),s([t()],b.prototype,"view",void 0),b=s([o("esri.widgets.LayerList.LayerListViewModel")],b);var k=b;export default k;
