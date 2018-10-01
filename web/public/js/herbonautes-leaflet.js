/**
 *
 * @param rootURL
 * @param institute
 * @param collection
 * @param code
 * @param w
 * @param h
 * @param defaultZoom
 */

function getTilesUrl(rootURL, institute, collection, code) {
    return rootURL +
        institute + '/' +
        collection + '/' +
        code +
        '/tile_{z}_{x}_{y}.jpg'
}

function getTilesMediaUrl(rootURL, institute, collection, code, media) {
    return rootURL +
        institute + '/' +
        collection + '/' +
        code + '/' +
        media.mediaNumber +
        '/tile_{z}_{x}_{y}.jpg'
}

function initializeSpecimenMediaMap(rootURL, institute, collection, code, media, defaultZoom) {

    console.log("Init specimen media map", rootURL, institute, collection, code, media, defaultZoom);

    if (window.specimenMap) {
        window.specimenMap.remove();
    }

    var mapElement = document.getElementById("map-container");

    var map = new L.Map(mapElement, {
        crs: L.CRS.Simple
    });

    // create the tile layer with correct attribution
    var tilesUrl = getTilesMediaUrl(rootURL, institute, collection, code, media);
    var attrib = institute + '/' + collection + '/' + code;

    var w = media.tileWidth,
        h = media.tileHeight;

    var bounds = [[0, 0], [-h, w]];

    var osm = new L.TileLayer(tilesUrl, {
        tileSize: new L.Point(w, h),
        bounds: bounds,
        noWrap: true,
        minZoom: 0,
        maxZoom: 4,
        attribution: attrib
    });

    // start the map
    map.setView(new L.LatLng(-h/2, w/2), defaultZoom);
    map.addLayer(osm);

    map.zoomControl.setPosition('topright');

    window.specimenMap = map;

}

function initializeSpecimenMap(rootURL, institute, collection, code, w, h, defaultZoom) {

    if (window.specimenMap) {
        window.specimenMap.remove();
    }

    var mapElement = document.getElementById("map-container");

    var map = new L.Map(mapElement, {
        crs: L.CRS.Simple
    });

    // create the tile layer with correct attribution
    var tilesUrl = getTilesUrl(rootURL, institute, collection, code);
    var attrib = institute + '/' + collection + '/' + code;

    var bounds = [[0, 0], [-h, w]];

    var osm = new L.TileLayer(tilesUrl, {
        tileSize: new L.Point(w, h),
        bounds: bounds,
        noWrap: true,
        minZoom: 0,
        maxZoom: 4,
        attribution: attrib
    });

    // start the map
    map.setView(new L.LatLng(-h/2, w/2), defaultZoom);
    map.addLayer(osm);

    map.zoomControl.setPosition('topright');

    window.specimenMap = map;
}