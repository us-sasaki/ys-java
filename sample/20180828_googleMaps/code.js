function initMap() {
  var chicago = new google.maps.LatLng(41.850, -87.650);

  var map = new google.maps.Map(document.getElementById('map'), {
    center: chicago,
    zoom: 3
  });

//  var coordInfoWindow = new google.maps.InfoWindow();
//  coordInfoWindow.setContent(createInfoWindowContent(chicago, map.getZoom()));
//  coordInfoWindow.setPosition(chicago);
//  coordInfoWindow.open(map);

//  map.addListener('zoom_changed', function() {
//    coordInfoWindow.setContent(createInfoWindowContent(chicago, map.getZoom()));
//    coordInfoWindow.open(map);
//  });
  
  map.addListener('click', function(d) {
  	var coordWindow = new google.maps.InfoWindow();
  	coordWindow.setContent(createInfoWindowContent(d.latLng, map.getZoom()));
  	coordWindow.setPosition(d.latLng);
  	coordWindow.open(map);
  });
  
  var polylines = new Array();
  
  map.addListener('bounds_changed', function() {
  	var n = polylines.length;
	for (var i = 0; i < n; i++) {
		polylines.shift().setMap(null);
	}
	
	var bounds = map.getBounds();
	var zoom = map.getZoom();
	var z = 1 << zoom;
	var ne = bounds.getNorthEast();
	var sw = bounds.getSouthWest();
	var worldNE = project(ne);
	var worldSW = project(sw);
	
	var p0 = new google.maps.Point(0,0);
	// 縦線(x, y) は tileCoordinate とする
	var y0 = Math.floor(z*worldNE.y/TILE_SIZE);
	var y1 = Math.floor(z*worldSW.y/TILE_SIZE)+1;
	
	for (var x = Math.floor(z*worldSW.x/TILE_SIZE);
			x < Math.floor(z*worldNE.x/TILE_SIZE) + 1; x++) {
		// (x, y0)-(x, y1)
		p0.x = x*TILE_SIZE/z; p0.y = y0*TILE_SIZE/z;
		var latLng0 = projectinv(p0);
		p0.x = x*TILE_SIZE/z; p0.y = y1*TILE_SIZE/z;
		var latLng1 = projectinv(p0);
		//
		var coords = [
			{lat: latLng0.lat(), lng: latLng0.lng()},
			{lat: latLng1.lat(), lng: latLng1.lng()}
		];
		
		var polyline = new google.maps.Polyline({
			path: coords,
			geodesic: true,
			strokeColor: '#FF0000',
			strokeOpacity: 0.5,
			strokeWeight: 1
		});
		polyline.setMap(map);
		polylines.push(polyline);
	}
	
	// 横線
	var x0 = Math.floor(z*worldSW.x/TILE_SIZE);
	var x1 = Math.floor(z*worldNE.x/TILE_SIZE)+1;
	
//	window.alert(" "+worldSW+"/"+worldNE);
//	window.alert('y='+Math.floor(z*worldSW.y/TILE_SIZE)+' ye='+Math.floor(z*worldNE.y/TILE_SIZE));
	for (var y = Math.floor(z*worldNE.y/TILE_SIZE);
			y <= Math.floor(z*worldSW.y/TILE_SIZE); y++) {
		// (x0, y)-(x1, y)
		p0.x = x0*TILE_SIZE/z; p0.y = y*TILE_SIZE/z;
		var latLng0 = projectinv(p0);
		p0.x = x1*TILE_SIZE/z; p0.y = y*TILE_SIZE/z;
		var latLng1 = projectinv(p0);
		//
		var coords = [
			{lat: latLng0.lat(), lng: latLng0.lng()},
			{lat: latLng1.lat(), lng: latLng1.lng()}
		];
		
		var polyline = new google.maps.Polyline({
			path: coords,
			geodesic: false,
			strokeColor: '#FF0000',
			strokeOpacity: 0.5,
			strokeWeight: 1
		});
		polyline.setMap(map);
		polylines.push(polyline);
	}
	
  });
}

var TILE_SIZE = 256;

function createInfoWindowContent(latLng, zoom) {
  var scale = 1 << zoom;

  var worldCoordinate = project(latLng);

  var pixelCoordinate = new google.maps.Point(
      Math.floor(worldCoordinate.x * scale),
      Math.floor(worldCoordinate.y * scale));

  var tileCoordinate = new google.maps.Point(
      Math.floor(worldCoordinate.x * scale / TILE_SIZE),
      Math.floor(worldCoordinate.y * scale / TILE_SIZE));

  return [
    '各種座標系',
    'LatLng: ' + latLng,
    'Zoom level: ' + zoom,
    'World Coordinate: ' + worldCoordinate,
    'Pixel Coordinate: ' + pixelCoordinate,
    'Tile Coordinate: ' + tileCoordinate,
    'inverse: ' + projectinv(worldCoordinate)
  ].join('<br>');
}

// The mapping between latitude, longitude and pixels is defined by the web
// mercator projection.
function project(latLng) {
  var siny = Math.sin(latLng.lat() * Math.PI / 180);

  // Truncating to 0.9999 effectively limits latitude to 89.189. This is
  // about a third of a tile past the edge of the world tile.
  siny = Math.min(Math.max(siny, -0.9999), 0.9999);

  return new google.maps.Point(
      TILE_SIZE * (0.5 + latLng.lng() / 360),
      TILE_SIZE * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI)));
}
function projectinv(point) {
	var lng = 360 * (point.x / TILE_SIZE - 0.5);
	var e = Math.exp(4 * Math.PI * (0.5 - point.y / TILE_SIZE));
	var siny = (e-1)/(e+1);
	var lat = Math.asin(siny)*180/Math.PI;
	return new google.maps.LatLng(lat, lng);
}
