// グローバル変数 map
var map;

// グローバル変数(定数)
var image_dir = "photo/";
var icon_dir = "img/";

// マップ上に表示している経路
var paths = new Array();

// マップ上に表示しているマーカー
var markers = new Array();

// Google Maps API から call back される
function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center: new google.maps.LatLng(35.6315212, 139.3702332), // 下柚木
		zoom: 12
	});
	
	var url = new URLParam();
	if (url.args.fname) {
		setJsonFile('json/'+url.args.fname);
	}
}
/**---------------------------------------------------------↓
 * URLParam Class
 *
 * URLからパラメータを取得する。http://url.net/html.html?key1=value1&key2=value2..
 * パラメータはトップレベル window から取得する。
 *
 * 使い方：
 * var url = new URLParam();
 * とすると、 url.args.key1, url.args.key2, ... として
 * value1, value2, ...が取得可能。
 */
var URLParam = function() {

/*------------------------------------
 * Constructor(兼 instance variables)
 */
	this.args = new Object();
	
	// パラメータをargsに格納
	var pair = window.top.location.search.substring(1).split('&');
	for (var i = 0; pair[i]; i++) {
		var kv = pair[i].split('=');
		this.args[kv[0]]=kv[1];
	}
};
/* URLParam Class
 *----------------------------------------------------------↑
 */


/**------------------------------------------------------------
 * PathColor Class
 *
 * 線の色をラップするJSクラスを作ってみる
 */
var PathColor = function() {

/*------------------------------------
 * Constructor(兼 instance variables)
 */
	this.b  = 0;
	this.bs = 7;
	this.r  = 0;
	this.rs = 13;
	this.g  = 0;
	this.gs = 19;

};

/*------------------
 * instance methods
 */
	/**
	 * getColor 関数
	 * return	#ff56c4 のような色定数
	 */
	PathColor.prototype.getColor = function() {
		return '#'+PathColor.toHex(this.r)+PathColor.toHex(this.g)+PathColor.toHex(this.b);
	};
	
	/**
	 * 次の色に変える
	 */
	PathColor.prototype.next = function() {
		// 線の色を変える
		this.b += this.bs;
		if ((this.b < 0) || (this.b > 255)) { this.bs = -this.bs; this.b += this.bs; }
		this.r += this.rs;
		if ((this.r < 0) || (this.r > 255)) { this.rs = -this.rs; this.r += this.rs; }
		this.g += this.gs;
		if ((this.g < 0) || (this.g > 255)) { this.gs = -this.gs; this.g += this.gs; }
	};
	
/*---------------
 * class methods
 */
	/**
	 * 0-255の数値を2桁の16進文字列に変換する
	 *
	 * 1 -> 01
	 * 15 -> 0f
	 * 100 -> 64
	 */
	PathColor.toHex = function(n) {
		if (n < 16) return '0'+n.toString(16);
		return n.toString(16);
	};


/* PathColor Class
 *----------------------------------------------------------
 */

function setJsonFile(fileName) {
	// マーカーを消す
	for (var i = 0; i < markers.length; i++) {
		markers[i].setMap(null);
	}
	markers = [];
	
	// 経路を消す
	for (var i = 0; i < paths.length; i++) {
		paths[i].setMap(null);
	}
	paths = [];
	
	// ファイルを読み込む
	$.getJSON(fileName, function(data) {
	    drawPaths(data);
	});
}

/**
 * 指定された coords 情報を元に、map上に Polyline, Marker を配置する
 *
 */
function drawPaths(coords) {
//	var pathColor = new PathColor();
	
	for (var i = 0; i < coords.length; i+=30) {
  	
		var crds = coords.slice(i, i+31);
		
		var polyline = new google.maps.Polyline({
		   	path: crds,
		   	geodesic: true,
		   	strokeColor: "#FF2000", //pathColor.getColor(),
		   	strokeOpacity: 0.7, // 不透明度
		   	strokeWeight: 2 // 線の太さ
		});
//		polyline.infowindow = new google.maps.InfoWindow({
//			content: 'clicked'
//		});
		
//		polyline.addListener('click', function(d) {
			// d.latLng にアクセス可能, this は Polyline obj.
//			this.infowindow.open(map, d); //.open(map, this);
//		});
		paths.push(polyline);
		
		// マウスオーバーすると写真を表示する Marker をつくる
		for (var j = 0; j < crds.length; j++) {
			var str = coords[i+j].photoFile;
			if (!str) {
			} else if (str.indexOf('photo:') == 0) {
				//
				// 写真指定のとき(photo:)
				//
				str = str.substring(str.indexOf(':')+1);
				// coords[i+j].photoFile が defined のとき
				var infowindow = new google.maps.InfoWindow({
					content: '<dl><dt><img src=\"' + image_dir + str+'\" width=160 height=90></dt></dl><br>' + str
				});
				var photoMarker = new google.maps.Marker({
					position: coords[i+j],
					map: map,
					icon: icon_dir + 'flag.png',
					title: str
				});
				// 新しい変数(インスタンス変数に相当)を定義
				photoMarker.infowindow = infowindow;
				photoMarker.addListener('mouseover', function() {
					this.infowindow.open(map, this);
				}); // this を使うことでインスタンス変数相当を利用
				
				photoMarker.addListener('click', function() {
					window.open(image_dir + this.title);
				});
				
				photoMarker.addListener('mouseout', function() {
					this.infowindow.close();
				});
				
				// 配列に格納
				markers[markers.length] = photoMarker;
			} else if (str.indexOf("stop:") == 0) {
				//
				// stop 指定のとき(stop:)
				//
				var photoMarker = new google.maps.Marker({
					position: coords[i+j],
					map: map,
					icon: icon_dir + 'coffee.png',
					title: str
				});
				// 配列に格納
				markers[markers.length] = photoMarker;
			} else {
				//
				// その他、標準の Marker を表示
				//
				var photoMarker = new google.maps.Marker({
					position: coords[i+j],
					map: map,
					icon: icon_dir + 'info.png'
				});
				var infowindow = new google.maps.InfoWindow({
						content: str
					});
				photoMarker.infowindow = infowindow;
				photoMarker.addListener('click', function() {
					this.infowindow.open(map, this);
				});
				// 配列に格納
				markers[markers.length] = photoMarker;
			}
		}
		
		paths[paths.length-1].setMap(map);
  		
	  	// 色を変更
//		pathColor.next();
	}
  	
  	// 中央に
  	var minLat = 999;
  	var maxLat = -999
  	var minLng = 999;
  	var maxLng = -999;
  	
  	for (var i = 0; i < coords.length; i++) {
		if (coords[i].lat > maxLat) maxLat = coords[i].lat;
		if (coords[i].lat < minLat) minLat = coords[i].lat;
		if (coords[i].lng > maxLng) maxLng = coords[i].lng;
		if (coords[i].lng < minLng) minLng = coords[i].lng;
  	}
  	
  	var corner1 = new google.maps.LatLng(minLat, minLng);
  	var corner2 = new google.maps.LatLng(maxLat, maxLng);
  	var bounds = new google.maps.LatLngBounds().extend(corner1).extend(corner2);
  	
//  	var lat = (minLat + maxLat)/2;
//  	var lng = (minLng + maxLng)/2;
  	
//	map.panTo(new google.maps.LatLng(lat, lng));
	
//	var h = (maxLat - minLat);
//	var w = (maxLng - minLng);
	
	
//	map.setZoom(12); // zoom は固定
  	
  	map.fitBounds(bounds);
}
