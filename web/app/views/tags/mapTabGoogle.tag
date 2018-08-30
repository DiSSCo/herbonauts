*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Onglet MAP
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}* 

<div id="mission-map-canvas" class="map" style="height:500px; width:100%"></div>
    	
    	
<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?sensor=false&key=${play.configuration.get("herbonautes.google.api.key")}"></script>

*{ Marker Cluster }*
<script type="text/javascript" src="@{'/public/js/markerclusterer.js'}"></script>


<script type="text/javascript">
	var contributionsMap = null;

	var globalCluster = null;

	function selectBubble() {
        var e = document.getElementById("bubble-select");
        var code = e.options[e.selectedIndex].value;
		$('.bubble-specimen').hide();
		$('#bubble-specimen-' + code).show();
	}

    function multiChoice(mc, infowindow) {

        var cluster = mc.clusters_;
        // if more than 1 point shares the same lat/long
        // the size of the cluster array will be 1 AND
        // the number of markers in the cluster will be > 1
        // REMEMBER: maxZoom was already reached and we can't zoom in anymore
        if (cluster.length == 1 && cluster[0].markers_.length > 1)
        {
            var markers = cluster[0].markers_;
			var specimenIds = [];
            for (var i=0; i < markers.length; i++)
            {
                // you'll probably want to generate your list of options here...
				//console.log("show markers", markers);
                specimenIds.push(markers[i].specimenId);
            }
            console.log("show markers", specimenIds);

            var bubbleURL = specimenBubblesAction({id: specimenIds[0]});

			for (var i = 1 ; i < specimenIds.length ; i++) {
                bubbleURL += "&id=" + specimenIds[i];
			}

            $.ajax({
                url: bubbleURL,
                success: function(content) {
                    infowindow.setContent(content);
                    infowindow.setPosition(cluster[0].getCenter());
                    infowindow.open(contributionsMap);
                }
            });



            return false;
        }

        return true;
    }

	function initializeMap() {
		if (contributionsMap != null) {
			return false;
		}

        var myStyles =[
            {
                featureType: "poi",
                elementType: "labels",
                stylers: [
                    { visibility: "off" }
                ]
            }
        ];

		var myOptions = {
			zoom: 1,
			center: new google.maps.LatLng(0, 0),
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			styles: myStyles
		};

		contributionsMap = 
			new google.maps.Map(document.getElementById('mission-map-canvas'), myOptions);


        var markers = [];
        /*for (var i = 0; i < 100; i++) {
            var latLng = new google.maps.LatLng(data.photos[i].latitude,
                    data.photos[i].longitude);
            var marker = new google.maps.Marker({'position': latLng});
            markers.push(marker);
        } */

        $.ajax({
        	url: '${_url}',
        	dataType: 'json',
        	success: function(data) {
        		var contributionCount = 0;
        		var sumLat = 0;
        		var sumLng = 0;

                var infowindow = new google.maps.InfoWindow({
                    content: ''
                });

        		$.each(data, function(i, contribution) {


					//var marker = new google.maps.Marker({map: contributionsMap});
					//marker.setDraggable(false);
				    //
					//marker.setPosition(new google.maps.LatLng(contribution.latitude, contribution.longitude));

					console.log("marker for ", contribution);

					var marker = new google.maps.Marker({
						specimenId: contribution.specimenId,
						masterSpecimenId: contribution.masterId
					});
					if (!!contribution.position) {
                        marker.setPosition(new google.maps.LatLng(contribution.position.lat, contribution.position.lng));
					} else {
                        marker.setPosition(new google.maps.LatLng(contribution.latitude, contribution.longitude));
                    }
					markers.push(marker);

					
					google.maps.event.addListener(marker, 'click', function() {
						
						var specimenId = contribution.specimenId;
						var masterSpecimenId = contribution.masterId;
						var bubbleURL = specimenBubbleAction({id: masterSpecimenId});



						$.ajax({
							url: bubbleURL,
							success: function(content) {
								infowindow.setContent(content);
							}
						});
						
						infowindow.open(contributionsMap, marker);
				    });

					
					if (!contribution.notPresent) {
						sumLat += contribution.latitude;
						sumLng += contribution.longitude;
						contributionCount++;
					}
					//console.log(contribution.latitude + ',' + contribution.longitude);
        		});

                var markerCluster = new MarkerClusterer(contributionsMap, markers);

                markerCluster.onClickZoom = function() { return multiChoice(markerCluster, infowindow); }

				markerCluster.setGridSize(30);
        		
        		if (contributionCount > 0 && false) {
        			contributionsMap.setCenter(
        					new google.maps.LatLng(
        							sumLat/contributionCount,
        							sumLng/contributionCount,
        							true
        					)
        				);
        			contributionsMap.setZoom(2);
        		}

                //var minClusterZoom = 4;
                //google.maps.event.addListener(markerCluster, 'clusterclick', function(cluster) {
				//	console.log(cluster);
                //    globalCluster = cluster;
                //    //contributionsMap.fitBounds(cluster.getBounds()); // Fit the bounds of the cluster clicked on
                //    //if( contributionsMap.getZoom() > minClusterZoom ) {// If zoomed in past 15 (first level without clustering), zoom out to 15
                //    //    contributionsMap.setZoom(minClusterZoom + 1);
                //    //}
                //});

				//console.log("done");
        		
        	}
        })
        
      }

	// Initialisation de la map lors de l'affichage de l'onglet
	$('#${_tabId}').on('shown', function() {
		initializeMap();
	});

</script>



