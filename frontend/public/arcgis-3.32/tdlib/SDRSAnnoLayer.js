define(["dojo/_base/declare", "esri/layers/tiled"], function (declare) {
  return declare(esri.layers.TiledMapServiceLayer, {
    constructor: function () {
      this.spatialReference = new esri.SpatialReference({ wkid: 4490 });
      this.initialExtent = (this.fullExtent = new esri.geometry.Extent(112.8, 34, 124.5, 38.9, this.spatialReference));

      this.tileInfo = new esri.layers.TileInfo({
        "rows": 256,
        "cols": 256,
        "compressionQuality": 0,
        "origin": {
          "x": -180,
          "y": 90
        },
        "spatialReference": {
          "wkid": 4490
        },
        "lods": [
          { "level": 0, "resolution": 1.4062500000002376, "scale": 590995186.11759996 },
          { "level": 1, "resolution": 0.703125000000119, "scale": 295497593.0588 },
          { "level": 2, "resolution": 0.351562500000059, "scale": 147748796.5294 },
          { "level": 3, "resolution": 0.17578125000003, "scale": 73874398.2647 },
          { "level": 4, "resolution": 0.0878906250000148, "scale": 36937199.1323 },
          { "level": 5, "resolution": 0.0439453125000074, "scale": 18468599.566175 },
          { "level": 6, "resolution": 0.0219726562500037, "scale": 9234299.7830875 },
          { "level": 7, "resolution": 0.0109863281250019, "scale": 4617149.89154375 },
          { "level": 8, "resolution": 0.00549316406250093, "scale": 2308574.94577187 },
          { "level": 9, "resolution": 0.00274658203125046, "scale": 1154287.47288594 },
          { "level": 10, "resolution": 0.00137329101562523, "scale": 577143.736442969 },
          { "level": 11, "resolution": 0.000686645507812616, "scale": 288571.868221484 },
          { "level": 12, "resolution": 0.000343322753906308, "scale": 144285.934110742 },
          { "level": 13, "resolution": 0.000171661376953154, "scale": 72223.960000000006 },
          { "level": 14, "resolution": 8.5830688476577E-05, "scale": 36071.4835276855 },
          { "level": 15, "resolution": 4.29153442382885E-05, "scale": 18035.7417638428 },
          { "level": 16, "resolution": 2.14576721191443E-05, "scale": 9017.87088192139 },
          { "level": 17, "resolution": 1.07288360595721E-05, "scale": 4508.93544096069 },
          { "level": 18, "resolution": 5.36441802978606E-06, "scale": 2254.46772048035 },
          { "level": 19, "resolution": 2.68220901489303E-06, "scale": 1127.23386024017 },
          { "level": 20, "resolution": 1.34110450744652E-06, "scale": 563.616930120087 }
        ]
      });

      this.loaded = true;
      this.onLoad(this);
    },

    getTileUrl: function (level, row, col) {

      // return "http://www.sdmap.gov.cn/tileservice/sdrasterpubmapdj?SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile" + "&LAYER=1" + "&STYLE=default" + "&FORMAT=image/png" + "&TILEMATRIXSET=taishannew" + "&TILEMATRIX=" + level + "&TILEROW=" + row + "&TILECOL=" + col;
      return "http://2.40.7.227:8080/OneMapServer/rest/services/DZDTGuSuMapDark/MapServer?token=QbGyxIz4ZomJ7QeG5aZ515OALV9RVsvf2M2zOALRvciUvf3ir3YDw5zNt_zy9XAd_bKHHm0UojqXeqfFlp_Dz5PiT6wuiuQhJazQCinTPozNKjGNo7SG5-mZs4yj6kmbocoiXBK8jLIvv6qj8hF_5A.."
    }
  });
});