// グローバル変数 map
var map;

// グローバル変数(定数)
var image_dir = "photo/";
var icon_dir = "img/";

// マップ上に表示している経路
var paths = new Array();

// マップ上に表示しているマーカー
var markers = new Array();

/** ファイルから読み込んだ JSON データ */
var jsonData;

/** 同時に１つしか開かない 速度/高度/時刻 表示用の InfoWindow */
var velowindow;

/** 同時に１つしかないマップ上の現在位置を示すカーソル */
var cursor;

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
	
	if (velowindow) velowindow.close();
	
	// ファイルを読み込む
	$.getJSON(fileName, function(data) {
		jsonData = data;
	    drawPaths(data);
		// ライドルーラーに設定
		window.top.setJsonFile(data);
	});
	
}


/**
 * 速度/高度/時刻 表示用の InfoWindow を開く。
 * すでに開いていたら、それは閉じる。
 *
 * @param	lat, lng	位置
 * @param	coords		path (最も近いものを全検索する),
 */
function openVeloWindow(lat, lng, coords) {
	// d.latLng にアクセス可能, this は Polyline obj.
	// Object.keys(d.latLng) --> lat, lng となた
	if (velowindow) velowindow.close();
	var ll = new google.maps.LatLng(lat, lng);
	var pos = {lat:lat, lng:lng};
	velowindow = new google.maps.InfoWindow({
		content: '<div style="overflow:hidden">'+pathInfo(coords, pos)+'</div>',
		position: ll
	});
	velowindow.open(map); //.open(map, this);
	velowindow.setPosition(pos);
}

/**
 * msec 時刻から、位置を求める。
 */
function getLatLngAtTime(t) {
	// 2分探索でindexを検索する
	var ind = -1;
	var tm = t;
	var l = 0;
	var r = jsonData.length-1;
	while (true) {
		if (jsonData[l].time >= tm) { ind = l; break; }
		if (jsonData[r].time <= tm) { ind = r; break; }
		var next = Math.floor((l+r)/2);
		if (jsonData[next].time > tm) r = next;
		else l = next;
		if (r - l <= 1) { ind = l; break; }
	}
	var lat, lng;
	if (ind == jsonData.length-1 ||
		jsonData[ind].time == jsonData[ind+1].time) {
		lat = jsonData[ind].lat;
		lng = jsonData[ind].lng;
	} else {
		var rate = (t - jsonData[ind].time)/(jsonData[ind+1].time - jsonData[ind].time);
		lat = (1-rate) * jsonData[ind].lat + rate * jsonData[ind+1].lat;
		lng = (1-rate) * jsonData[ind].lng + rate * jsonData[ind+1].lng;
	}
	return {lat:lat, lng:lng, index:ind};
}

/**
 * msec 時刻を与えると、その位置にマーカー(cursor)を表示する。
 */
function setCursorAtTime(t) {
	var latLng = getLatLngAtTime(t);
	if (!cursor) {
		cursor = new google.maps.Marker({
						position: latLng,
						map: map
					});
	} else {
		cursor.setPosition( latLng );
	}
}

function setInfoWindowAtTime(t, panTo) {
	var latLng = getLatLngAtTime(t);
	var sindex = latLng.index - 2;
	var eindex = latLng.index + 2;
	sindex = (sindex < 0)? 0 : sindex;
	eindex = (eindex > jsonData.length)? jsonData.length : eindex;
	var coords = jsonData.slice(sindex, eindex);
	openVeloWindow(latLng.lat, latLng.lng, coords); // 全部渡しているので遅い
	if (panTo) map.panTo(latLng);
}

/**
 * 指定された coords 情報を元に、map上に Polyline, Marker を配置する
 *
 */
function drawPaths(coords) {
	
	for (var i = 0; i < coords.length; i+=30) {
  	
		var crds = coords.slice(i, i+31);
		
		var polyline = new google.maps.Polyline({
		   	path: crds,
		   	geodesic: true,
		   	strokeColor: "#FF2000", //pathColor.getColor(),
		   	strokeOpacity: 0.7, // 不透明度
		   	strokeWeight: 4 // 線の太さ
		});
		polyline.cords = crds;
		
		// polyline にマウスオーバーしたときの速度/高度情報表示
		polyline.addListener('mouseover', function(d) {
			openVeloWindow(d.latLng.lat(), d.latLng.lng(), this.cords);
		});
		polyline.addListener('mouseout', function() {
			// mouseout したら消す
			setTimeout( function() {
				if (velowindow) velowindow.close();
			}, 1500 );
		});
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
					content: '<dl><dt><img src=\"' + image_dir + str+'\" ></dt></dl><br>' + str
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
				markers.push(photoMarker);
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
				markers.push(photoMarker);
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
				markers.push(photoMarker);
			}
		}
		
		paths[paths.length-1].setMap(map);
  		
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
  	
  	map.fitBounds(bounds);
}

/**
 * sliceされた Jsonデータ、polyline, マウス位置情報から
 * その地点での推定時間、速度、高度情報を文字列として返却する
 * <pre>
 *       pos・
 *         /|
 *        / |
 *       /G |
 *     p・----・q=crds[i+1]
 * 
 * b = (p -> q)   vector
 * c = (p -> pos) vector
 *
 * ただし、x-y 座標は lng-lat で与えられるため、cos G は実際の角度ではない
 * </pre>
 */
function pathInfo(crds, pos) {
	var j = 0; // j は crds[j]-crds[j+1] に pos があると思われる index
	var cmax = 0;
	var csinmin = 100000;
	var rate = 0;
	for (var i = 0; i < crds.length-1; i++) {
		var p = crds[i];   // path の始点
		var q = crds[i+1]; // path の終点
		
		// p始点の位置ベクトル b=(x,y)
		var x = q.lat - p.lat;
		var y = q.lng - p.lng;
		// p始点の位置ベクトル c=(u,v)
		var u = pos.lat - p.lat;
		var v = pos.lng - p.lng;
		
		var bc = x*u + y*v; // 内積
		var lbl = Math.sqrt(x*x + y*y); // b の長さ
		if (lbl == 0) continue; // p == q
		var lcl = Math.sqrt(u*u + v*v); // c の長さ
		if (lcl == 0) {
			rate = 0;
			break;
		}
		var cos = bc / lbl / lcl;
		var len = bc / lbl; // len = |c|cos G
		var csin = Math.sqrt(lcl*lcl - len*len); // 点と直線の距離
		if ((len >= 0)&&(len <= lbl)&&(csin < csinmin)) {
			j = i; // 長さが 0 以上 |b| 以下で なるべく pq に近い index
			csinmin = csin;
			rate = len / lbl;
		}
	}
	
	var date = crds[j].date;
	// t0 は始点の time(msec)
	var t0 = new Date(Number('20'+date.substring(0,2)), // year
						Number(date.substring(2,4))-1, // month
						Number(date.substring(4,6)), // day
						Number(date.substring(7,9)), // hour
						Number(date.substring(10,12)), // minute
						Number(date.substring(13,15)) ).getTime(); // second
	date = crds[j+1].date;
	// t1 は終点の time(msec)
	var t1 = new Date(Number('20'+date.substring(0,2)), // year
						Number(date.substring(2,4))-1, // month
						Number(date.substring(4,6)), // day
						Number(date.substring(7,9)), // hour
						Number(date.substring(10,12)), // minute
						Number(date.substring(13,15)) ).getTime(); // second
	// 0 ≦ len ≦ 1
	var t = (1.0-rate)*t0 + rate*t1;
	
	// t が計算されるのがここのため、ここで moveInfoWindowAtTime() を呼ぶ
	window.top.moveInfoWindowAtTime(t);
	
	var d = new Date(t);
	var v = (1.0-rate)*crds[j].velo + rate*crds[j+1].velo;
	var a = (1.0-rate)*crds[j].alt + rate*crds[j+1].alt;
	var gradm;
	var dist = crds[j+1].d;
	var grad = (crds[j+1].alt - crds[j].alt);
	if (grad != 0) grad = grad / dist;
	if (grad > 0.06) gradm = '登り(;´Д`)';
	else if (grad > 0.03) gradm = '登り';
	else if (grad < -0.06) gradm = '下り(^o^)';
	else if (grad < -0.03) gradm = '下り';
	else gradm = '';
	
	return '時刻'+d.getHours()+':'+('0'+d.getMinutes()).slice(-2)+'<br>速度'+(Math.floor(v*36))/10+'km/h<br>高度'+Math.floor(a)+"m "+gradm;
}

/**---------------------------------------------------------↓
 * Icon Relocator Class
 *
 * (lat, lng) を与えると、重ならないようにずらした位置を返却する
 * クラス。写真のアイコンが重なって見えてしまう現象を回避するために
 * 作成。
 *
 * 使い方：
 * var iconRelocator = new IconRelocator();
 * locatedCoords = locate(coords);
 *
 */
var IconRelocator = function() {
	
/*------------------------------------
 * Constructor(兼 instance variables)
 */
	this.originalLoc = [];
	this.located = [];
	
	this.projection = function() {
	};
};

/*------------------
 * instance methods
 */
	IconRelocator.prototype.locate = function(location) {
	};
	
	IconRelocator.prototype.clear = function() {
		this.originalLoc = [];
		this.located = [];
	};
	IconRelocator.prototype.setProjection = function() {
		
		this.clear();
	};

/* Icon Relocator Class
 *----------------------------------------------------------↑
 */
