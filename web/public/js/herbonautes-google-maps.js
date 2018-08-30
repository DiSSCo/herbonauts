	var typeOptionsFactory = {
		
		create: function(rootURL, institute, collection, code, w, h) {
			return {
		    getTileUrl: function(coord, zoom) {



		        var normalizedCoord = getNormalizedCoord(coord, zoom);
		        if (!normalizedCoord) {
		          return null;
		        }
		        var bound = Math.pow(2, zoom);


		        return rootURL + 
			        institute + '/' + 
			        collection + '/' + 
			        code + 
			        '/tile_' + zoom + '_' + 
	        		normalizedCoord.x + "_" +
		            normalizedCoord.y + ".jpg";
		        
		        /*"http://mnhn.bluestone.fr/img/test_" */
		    },
		    tileSize: new google.maps.Size(w, h),
		    maxZoom: 4,
		    minZoom: 0,
			zoom: 0,
		    radius: 1738000,
		    name: code
		  }
		}
	};

  function initializeSpecimenMap(rootURL, institute, collection, code, w, h, defaultZoom) {

    console.log("Initialize specimen map");

    var myLatlng = new google.maps.LatLng(0, 0);
    var myOptions = {
      center: myLatlng,
      zoom: defaultZoom,
      streetViewControl: false,
      panControl: false,
      mapTypeControl: false,
      zoomControlOptions: {
      	position: google.maps.ControlPosition.TOP_RIGHT
      }
    };

    var map = new google.maps.Map(document.getElementById("map-container"),
        myOptions);
    map.mapTypes.set('specimen', new google.maps.ImageMapType(typeOptionsFactory.create(rootURL, institute, collection, code, w, h)));
    map.setMapTypeId('specimen');
    map.setTilt(45);

  }

  // Normalizes the coords that tiles repeat across the x axis (horizontally)
  // like the standard Google map tiles.
  function getNormalizedCoord(coord, zoom) {
    var y = coord.y;
    var x = coord.x;

    // tile range in one direction range is dependent on zoom level
    // 0 = 1 tile, 1 = 2 tiles, 2 = 4 tiles, 3 = 8 tiles, etc
    var tileRange = 1 << zoom;

    // don't repeat across y-axis (vertically)
    if (y < 0 || y >= tileRange) {
      return null;
    }

    // repeat across x-axis
    if (x < 0 || x >= tileRange) {
      return null;
    }

    return {
      x: x,
      y: y
    };
  }