// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
/* jshint worker: true */
/* global importScripts: false */

/* REQUIRES esri/workers/scripts/helpers */

/* global merge, mixin */
(function(context) {
  /**
   * @typedef {Object} Envelope
   * @property {number} x - min x value
   * @property {number} y - min y value
   * @property {number} w - envelope width
   * @property {number} h - envelope height
   */
  /**
   * @typedef {Object} Point
   * @property {number} x - x coordinate
   * @property {number} y - y coordinate
   * @property {number=} z - optional z coordinate
   * @property {number=} m - optional m coordinate
   */
  /**
   * normalizes items for use in insert, search, & remove functions
   * @param  {(Array.<number>|Point|Envelope)} item - The item to normalize
   * @return {Point|Envelope)} {@link Point}s are returned for kdtree. {@link Envelope}s are returned for all other index systems
   */

  function prepareItem(item) {
    var norm;
    if (Array.isArray(item)) {
      if (item.length > 3) {
        //bbox
        norm = {
          x: item[0],
          y: item[1],
          w: Math.abs(item[0] - item[2]),
          h: Math.abs(item[1] - item[3])
        };
        if(this.system == 'kdtree'){
          //use center point
          norm = {
            x: norm.x + (norm.w/2),
            y: norm.y + (norm.h/2)
          };
        }
      } else {
        //point
        norm = {
          x: item[0],
          y: item[1],
          attributes: item[3]
        };
        if (this.system != 'kdtree') {
          norm.w = 0;
          norm.h = 0;
        }
      }
    } else if ('x' in item && 'y' in item) {
      norm = item;
      if (this.system != 'kdtree' && !('h' in item && 'w' in item)) {
        //appears to be a point, convert to envelope
        norm.w = 0;
        norm.h = 0;
      }
    }
    return norm;
  }


  function Indexer(options) {
    options = options || {};
    var data = options.data;
    delete options.data;
    merge(options, this);
    if(data){
      var ndxOpts = options.indexOptions || {};
      merge({layerId: options.layerId}, ndxOpts);
      this.index = this.create(data, ndxOpts);
    }
  }

  mixin(Indexer, /** @lends Indexer.prototype */ {
    /**
     * The indexing system to use. Choices are: "rtree", "quadtree", "kdtree". "kdtree" is only used for points.
     * @type {String}
     * @default 'rtree'
     */
    system: 'rtree',
    /**
     * The index instance used
     * @type {Object}
     */
    index: null,
    /**
     * Creates the index and assigns it to the `index` property
     * @param  {Array.<(Array.<number>|Envelope|Point)>} data - the data to create the index with.
     * @param  {[type]} options [description]
     * @return {[type]}         [description]
     */
    create: function(data, options){
      this.index = this['_create_'+this.system](data, options || this.indexOptions);
      return this.index;
    },
    /**
     * search the index
     * @param  {(Array.<number>|Envelope|{point: (Point|Array.<number>), count:number, distance:?number, filter:?function(Point):boolean})} criteria 
     *  the search area and optional search options
     * @return {Array} An array of nodes matching the criteria
     */
    search: function(criteria, returnNode) {
      return this['_search_'+this.system](criteria, returnNode);
    },
    /**
     * insert item into index
     * @param  {(Array.<number>|Envelope|Point)} geom - the indexed area
     * @param  {*} data - information to store in the index (typically an id)
     * @return {Object} root node of index
     */
    insert: function(geom, data, layerId){
      if(geom.geom){
        data = geom.data;
        geom = geom.geom;
        delete geom.data;
      }
      return this['_insert_'+this.system](geom, data, layerId);
    },
    /**
     * remove item from index
     * @param  {(Array.<number>|Envelope|Point)} geom - the indexed area to remove from or clear
     * @param  {(boolean|*)} data - stored data which must match for a node to be removed. If `false` clear all nodes contained by the geom parameter
     * @return {Array} Array of removed nodes.
     */
    remove: function(geom, data){
      if(geom.geom){
        data = geom.data;
        geom = geom.geom;
        delete geom.data;
      }
      return this['_remove_'+this.system](geom, data);
    },
    /**
     * serializes the index
     * @return {string} JSON string representation of the index
     */
    serialize: function(){
      var index = this.index;
      if(index.toJSON !== Object.prototype.toJSON){
        return index.toJSON();
      } else {
        return index.serialize && index.serialize();
      }
    },
    /**
     * create an index from serialized index
     * @param  {string} snapshot - JSON string representation of an index
     * @return {Object} created index
     */
    load: function(snapshot){
      this.index = this['_load_'+this.system](snapshot);
      return this.index;
    },
    _indexLibraries: {
      kdtree: {
        ns: 'kdTree',
        script: 'libs/kdTree.js'  
      },
      rtree: {
        ns: 'RTree',
        script: 'libs/rtree.js'
      },
      quadtree: {
        ns: 'Quadtree',
        script: 'libs/quadtree.js'
      }
    },
    _create_rtree: function(data, options){
      if(!(this._indexLibraries.rtree.ns in context)){
        importScripts(this._indexLibraries.rtree.script);
        /* global RTree, geomToBbox */
      }
      var tree = new RTree(options && options.width);
      var len = data.length;
      var item;
      while (len--) {
        item = data[len];
        item.id = (item.id != null) ? item.id : this.idField && item.attributes[this.idField];
        item.layerId = item.layerId || options.layerId;
        if(item.geometry){
          tree.insert(prepareItem(geomToBbox(item.geometry)), item);
        } else {
          tree.insert(prepareItem(item), item);
        }
      }
      return tree;
    },
    _create_kdtree: function(data, options){
      if(!(this._indexLibraries.kdtree.ns in context)){
        importScripts(this._indexLibraries.kdtree.script);
        /* global kdTree */
      }
      options = options || {};
      /** @type {function(Point,Point): number} **/
      var dist = options.dist || function(j,k){
        var xd = j.x-k.x; //a^2 + b^2 = c^2
        var yd = j.y-k.y;
        return Math.sqrt(xd*xd + yd*yd);
      };
      /** @type {Array.<string>} **/
      var dim = options.dimensions || ['x', 'y'];
      var len = data.length;
      var pt, id;
      /** @type {Array.<Point>} **/
      var points = [];
      while (len--) {
        pt = data[len];
        id = (pt.id != null) ? pt.id : (this.idField && pt.attributes[this.idField]);
        if(pt.geometry){
          pt.geometry = prepareItem(geomToBbox(pt.geometry));
          merge(pt.attributes, pt.geometry);
          pt.geometry.id = id;
          pt.geometry.layerId = options.layerId;
          points.push(pt.geometry);
        } else {
          pt = prepareItem(pt);
          pt.id = id;
          pt.layerId = options.layerId;
          points.push(pt);
        }
      }
      var tree = new kdTree(points, dist, dim);
      return tree;
    },
    _create_quadtree: function(data, options){
      throw 'Not Implimented';
    },
    /**
     * search the kdtree index
     * @param  {{point: (Point|Array.<number>), count:?number, distance:?number, filter:?function(Point):boolean}} criteria 
     *  the search area and options
     * @return {Array} An array of nodes matching the criteria
     * @private
     */
    _search_kdtree: function(criteria){
      var c = criteria;
      if(c.geometry){
        c.point = geomToBbox(c.geometry);
      } else if(c.point.geometry){
        c.point = geomToBbox(c.point.geometry);
      }
      return this.index.nearest(prepareItem(c.point), c.count || 1, c.filter, c.distance);
    },
    /**
     * search the rtree index
     * @param  {(Array.<number>|Envelope)} criteria - the bounds to search
     * @return {Array} An array of indexed data matching the criteria
     * @private
     */
    _search_rtree: function(criteria, returnNode){
      if(criteria.geometry){
        //got a feature/graphic
        criteria = geomToBbox(criteria.geometry);
      } else if(criteria.xmin !== undefined && criteria.ymax !== undefined){
        //got an esri.geometry.Extent convert to array
        var c = criteria;
        criteria = [c.xmin, c.ymin, c.xmax, c.ymax];
      }
      return this.index.search(prepareItem(criteria), returnNode === true);
    },
    _search_quadtree:function(criteria){
      throw 'Not Implimented';
    },
    _insert_kdtree: function(geom, data, layerId){
      if(geom.geometry){
        data = data || geom.attributes;
        data.id = (data.id != null) ? data.id : (this.idField && data[this.idField]);
        geom = geom.geometry;
      }
      geom = prepareItem(geom);
      geom.layerId = layerId;
      if(data){
        if(typeof(data) == 'object'){
          merge(data, geom);
        } else if (!Array.isArray(data)){
          //either a string or number, use as id
          geom.id = data;
        }
      }
      this.index.insert(geom);
      return this.index.root;
    },
    _insert_rtree: function(geom, data, layerId) {
      var result;
      if (geom.geometry) {
        if (data == null) { //not using strict comparision. want to catch both null & undefined, but not 0
          data = geom;
        }
        geom = geomToBbox(geom.geometry);
      }

      if (typeof data == 'string' || typeof data == 'number') {
        data = {
          id: data,
          layerId: layerId
        };
      } else {
        data.id = (data.id != null) ? data.id : (this.idField && (data[this.idField] || data.attributes[this.idField]));
        data.layerId = layerId;
      }
      result = this.index.insert(prepareItem(geom), data);
      return result;
    },
    _insert_quadtree:function(geom, data){
      throw 'Not Implimented';
    },
    _remove_kdtree: function(geom, data){
      geom = prepareItem(geom);
      if(data){
        if(typeof(data) == 'object'){
          merge(data, geom);
        } else if (!Array.isArray(data)){
          //either a string or number, use as id
          geom.id = data;
        }
      }
      var node = this.index.remove(geom);
      return node && [node];
    },
    _remove_rtree: function(geom, data){
      return this.index.remove(prepareItem(geom), data);
    },
    _remove_quadtree:function(geom, data){
      throw 'Not Implimented';
    },
    _load_rtree: function(snapshot){
      if(!(this._indexLibraries.rtree.ns in context)){
        importScripts(this._indexLibraries.rtree.script);
        /* global RTree, geomToBbox */
      }
      var tree = this.index || (this.index = new RTree());
      tree.deserialize(snapshot);
      return tree;
    },
    _load_kdtree: function(snapshot){
      return this._create_kdtree(snapshot);
    }
  });

  context.Indexer = Indexer;
  return Indexer;

}(this));