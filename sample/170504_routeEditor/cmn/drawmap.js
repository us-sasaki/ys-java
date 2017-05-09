// グローバル変数 map
var map;
var markers = [];
var poly;
var infoWindow;

// Google Maps API から call back される
function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center: new google.maps.LatLng(35.6713, 139.7573), // 会社
		zoom: 19
	});
	map.addListener('click', function(event) {
		addMarker(event.latLng);
	});
	map.addListener('rightclick', function(event) {
		showInfoWindow(event.latLng);
	});
	
	poly = new google.maps.Polyline({
		strokeColor: '#000000',
		strokeOpacity: 1.0,
		strokeWeight: 3
	});
	poly.addListener('click', function(event) {
		insertMarkerAt(event.latLng);
	});
	poly.setMap(map);
}

/**
 * 指定された地図上の位置に、マーカーを配置します。
 */
function addMarker(latLng) {
	addMarkerAt(latLng, markers.length);
}

/*
 * 指定された地図上の位置に、マーカーを配置します。
 * マーカーの順序は、指定されたインデックスになります。
 */
function addMarkerAt(latLng, index) {
	var marker = new google.maps.Marker({
		position: latLng,
		map: map,
		draggable: true
	});
	// マーカーをクリックしたら消す
	marker.addListener('click', function() {
		var i = 0;
		for (;i < markers.length; i++) {
			if (markers[i] == marker) break;
		}
		if (i < markers.length) {
			marker.setMap(null);
			markers.splice(i, 1);
			poly.getPath().removeAt(i);
		}
	});
	// マーカーをドラッグしたら Polyline に位置変更を通知
	marker.addListener('drag', function(event) {
		var i = 0;
		for (; i < markers.length; i++) {
			if (markers[i] == marker) break;
		}
		poly.getPath().setAt(i, marker.getPosition());
	});
	
	markers.splice(index, 0, marker);
	marker.setMap(map);
	poly.getPath().insertAt(index, marker.getPosition());
	
}

/*
 * 指定されたポリライン上の位置に、マーカーを挿入します。
 */
function insertMarkerAt(latLng) {
	var j = getIndexOf(poly.getPath(), latLng);
	
	addMarkerAt(latLng, j+1);
}

/**
 * 指定された paths (polyline.getPath() の値) と、ポリライン上の位置
 * の両端にあるマーカーのインデックスを取得します。
 * 取得されるのは、小さいほうのインデックスです。
 * <pre>
 *   latLng・
 *         /|
 *        / |
 *       /G |
 *     p・----・q=paths[i+1]
 * 
 * b = (p -> q)   vector
 * c = (p -> pos) vector
 *
 * ただし、x-y 座標は lng-lat で与えられるため、cos G は実際の角度ではない
 * </pre>
 */
function getIndexOf(paths, latLng) {
	var j = 0; // j は paths[j]-paths[j+1] に latLng があると思われる index
	var cmax = 0;
	var csinmin = 100000;
	var rate = 0;
	for (var i = 0; i < paths.getLength()-1; i++) {
		var p = paths.getAt(i);   // path の始点
		var q = paths.getAt(i+1); // path の終点
		
		// p始点の位置ベクトル b=(x,y)
		var x = q.lat() - p.lat();
		var y = q.lng() - p.lng();
		// p始点の位置ベクトル c=(u,v)
		var u = latLng.lat() - p.lat();
		var v = latLng.lng() - p.lng();
		
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
	return j;
}

/**
 * 右クリック時の情報ウィンドウ表示を行います。
 */
function showInfoWindow(latLng) {
	if (infoWindow) infoWindow.close();
	var ll = new google.maps.LatLng(latLng.lat(), latLng.lng());
	var pos = {lat:latLng.lat(), lng:latLng.lng()};
	infoWindow = new google.maps.InfoWindow({
		content: makeContent(),
		position: ll
	});
	infoWindow.open(map);
	infoWindow.setPosition(pos);
}

/**
 * 情報ウィンドウに表示させる位置JSON形式を作成します。
 */
function makeContent() {
	// HTML の form に入力された値を読み取る
	var params = getParams();
	
	if (params.format == "usm") {
		// format uniform speed motion
		// 総行程の距離(m)を算出
		var distance = 0;
		var distances = [];
		for (var i = 0; i < markers.length - 1; i++) {
			distances.push(google.maps.geometry.spherical.computeDistanceBetween(markers[i].getPosition(), markers[i+1].getPosition()));
			distance += distances[i];
		}
		// 全体の時間(sec)を算出
		var totalTime = (params.end.getTime() - params.start.getTime())/1000;
		
		if (params.interval == 0) return "[]";
		
		// 平均速度(m/s)
		// time <= 0 の場合？
		var velo = distance / totalTime;
		var json = "[";
		
		for (var t = 0; t <= totalTime; t += params.interval) {
			// 時刻 t の時の位置を求める
			var index = 0;
			var d = 0;
			for (;index < markers.length - 1; index++) {
				d += distances[index];
				if (velo * t <= d) break; // index と index + 1 の間にいる
			}
			if (index == markers.length - 1) index--;
			var d1 = d - velo * t; // index + 1 からの距離
			// P を求める点として、
			// index, index+1 を rate:1-rate に内分する点が P
			var rate = 1.0 - (d1 / distances[index]);
			
			var lat = (1.0 - rate) * markers[index].getPosition().lat()
						+ rate * markers[index+1].getPosition().lat();
			var lng = (1.0 - rate) * markers[index].getPosition().lng()
						+ rate * markers[index+1].getPosition().lng();
			lat = Math.floor(lat * 100000)/100000;
			lng = Math.floor(lng * 100000)/100000;
			
			// JSON 化
			json = json + "{\"lat\":"+lat+",\"lng\":"+lng+",\"date\":\""+c8yDateString(params.start.getTime()+t*1000)+"\"}";
			if (t + params.interval <= totalTime) json = json + ",";
		}
		json = json + "]";
		return json;
		
	} else {
		// format points
		var json = "[";
		for (var i = 0; i < markers.length; i++) {
			var latLng = markers[i].getPosition();
			var lat = Math.floor(latLng.lat()*100000) / 100000;
			var lng = Math.floor(latLng.lng()*100000) / 100000;
			json = json + "{\"lat\":"+lat+",\"lng\":"+lng+"}";
			if (i < markers.length - 1) {
				json = json + ",";
			}
		}
		return json + "]";
	}
}

function parseDateString(date) {
	var t = new Date(Number('20'+date.substring(0,2)), // year
						Number(date.substring(2,4))-1, // month
						Number(date.substring(4,6)), // day
						Number(date.substring(7,9)), // hour
						Number(date.substring(10,12)), // minute
						Number(date.substring(13,15)) ).getTime(); // second
	return new Date(t);
	// d.getHours(), d.getMinutes()
}

function getParams() {
	// form はトップレベル window の name="paramForm" で定義されているとする
	var form = window.top.document.forms.paramForm;
	// HTML form にあるパラメータ取得
	var format = form.form.value;
	var start  = form.start.value;
	var end    = form.end.value;
	var interval  = form.interval.value;
	
	if (format == "points") return {format:"points"};
	return {
		format:"usm",
		interval:Number(interval),
		start:parseDateString(start),
		end:parseDateString(end)
	};
}

function c8yDateString(time) {
	var date = new Date(time);
	var year = date.getFullYear();
	var month = ('0'+(1+date.getMonth())).slice(-2); // 0(1月)～11
	var day = ('0'+date.getDate()).slice(-2); // 1～31
	var hour = ('0'+date.getHours()).slice(-2); // 0～23
	var minute = ('0'+date.getMinutes()).slice(-2); // 0～59
	var second = ('0'+date.getSeconds()).slice(-2); // 0～59
	
	return year+'-'+month+'-'+day+'T'+hour+':'+minute+':'+second+'.000+09:00';
}
