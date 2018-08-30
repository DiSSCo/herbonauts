*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Onglet MAP
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}* 

<div id="mission-map-canvas" class="map" style="height:500px; width:100%"></div>

<link rel="stylesheet" href="@{'/public/leaflet/leaflet.css'}">
<link rel="stylesheet" href="@{'/public/leaflet.markercluster/MarkerCluster.css'}">
<link rel="stylesheet" href="@{'/public/leaflet.markercluster/MarkerCluster.Default.css'}">

<script src="@{'/public/leaflet/leaflet.js'}" type="text/javascript"></script>
<script src="@{'/public/leaflet.markercluster/leaflet.markercluster.js'}" type="text/javascript"></script>


*{
var markers = L.markerClusterGroup();

for (var i = 0; i < addressPoints.length; i++) {
var a = addressPoints[i];
var title = a[2];
var marker = L.marker(new L.LatLng(a[0], a[1]), { title: title });
marker.bindPopup(title);
markers.addLayer(marker);
}

map.addLayer(markers);

}*

<script type="text/javascript">

	function markerIcon(color) {
	    var color = color || 'blue';
        var icon = new L.Icon({
            iconUrl: 'https://cdn.rawgit.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-' + color + '.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        });
        return icon;
	}

	function initializeMap() {
        var mapElement = document.getElementById("mission-map-canvas");

        var topoAttribution = 'Kartendaten: &copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap</a>-Mitwirkende, <a href="http://viewfinderpanoramas.org">SRTM</a> | Kartendarstellung: &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)';

        map = L.map(mapElement).setView([20, 0], 2);
        L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
            maxZoom: 17,
            attribution: topoAttribution,
            zoomControl: false
        }).addTo(map);

        map.zoomControl.setPosition('bottomright');

        window.leafletMap = map;


        function addMarkers(markers) {

            var cluster = L.markerClusterGroup();

            _.each(markers, function(m) {

                if (!m.latitude || !m.longitude) {
                    return;
				}

                var pos = [m.latitude, m.longitude];
                var marker = new L.marker(pos);

                cluster.addLayer(marker);

                marker.setIcon(markerIcon('green'));
                marker.on('click', onMarkerClick);

                function onMarkerClick(e) {

                    var masterSpecimenId = m.masterId || m.specimenId;
                    var bubbleURL = specimenBubbleAction({id: masterSpecimenId});

                    var popup = e.target.getPopup();
                    if (!popup) {
						$.ajax({
							url: bubbleURL,
							success: function(popupContent) {
								marker.bindPopup(popupContent).openPopup();
								popup.openPopup();
							}
						});
					} else {
                        popup.openPopup();
					}

                }

            })

            map.addLayer(cluster);

        }

        // Add markers
        $.ajax({
            url: '${_url}',
            dataType: 'json',
            success: addMarkers
        });

	}



	// Initialisation de la map lors de l'affichage de l'onglet
	$('#${_tabId}').on('shown', function() {
		initializeMap();
	});

</script>



