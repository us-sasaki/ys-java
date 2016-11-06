/**
 * JSON ファイルのライドデータ
 */
var jsonData;

/**
 * SVG領域に設定する余白pixel数
 */
var lpad = 30;
var rpad = 24;
var tpad = 4;
var bpad = 4;

var xScale;
var cursorX;
var windowX;

var wheelTimeoutId;

/**
 * ファイル名を指定して、JSONデータを読み込みます。
 * 開発用で、本物では使いません。drawmap.js で読み込んだデータを使用します。
 */
function readJsonFile(filename) {
	d3.json(filename, function(d) {
		setJsonFile(d);
	});
}

/**
 * JSONデータを指定し、初期処理、描画します。
 */
function setJsonFile(data) {
	jsonData = data;
	initChart();
}

/**
 * JSON ファイルに含まれる、"yyMMdd HH:mm:ss" をtime(msec)に変換します
 */
function toTime(d) {
	var date = new Date(
			parseInt(d.substring(0,2)) + 2000,	// year
			parseInt(d.substring(2,4)) - 1,		// month
			parseInt(d.substring(4,6)),			// day
			parseInt(d.substring(7,9)),			// hour
			parseInt(d.substring(10,12)),		// minutes
			parseInt(d.substring(13,15)) );		// seconds
	return date.getTime();
}

/**
 * JSON データに time フィールドを追加し、画面描画します。
 */
function initChart() {
	// time フィールド追加
	for (var i = 0; i < jsonData.length; i++) {
		jsonData[i].time = toTime(jsonData[i].date);
	}
	// svelo フィールド追加(生データはがたがたしすぎるので滑らかに)
	var v = 0;
	for (var i = 0; i < jsonData.length; i++) {
		v = 0.9*v + 0.1*(jsonData[i].velo*36/10); // 過去データに9割ひきずられる
		jsonData[i].svelo = v;
	}
	cursorX = lpad;
	windowX = lpad;
	draw(jsonData);
	
	// リサイズ時の処理
	var timer = false;
	window.addEventListener('resize', function() {
		if (timer != false) clearTimeout(timer);
		timer = setTimeout(function() { draw(jsonData); }, 10);
	});
	
	// mouseover時の処理
	var result = d3.select("#result").node();
	result.addEventListener('mousemove', function(mevent) {
		var rct = result.getBoundingClientRect();
		var mx = mevent.clientX - rct.left - lpad;
		//var my = mevent.clientY - rct.top - tpad;
		
		setTimeout( function() {
			moveCursor(mx + lpad);
		}, 10);
	});
	// click時の処理
	var clickEventHndlr = function(mEvent, panTo) {
		var rct = result.getBoundingClientRect();
		var mx = mEvent.clientX - rct.left - lpad;
		
		setTimeout( function() {
			moveInfoWindow(mx + lpad, panTo);
		} ,10);
	};
	result.addEventListener('click', function(mEvent) {
						clickEventHndlr(mEvent,false);
	});
	// doubleclick時の処理
	result.addEventListener('dblclick', function(mEvent) {
						clickEventHndlr(mEvent,true);
	});
	// wheel時の処理
	result.addEventListener('wheel' , function(wEvent) {
		if (wheelTimeoutId) clearTimeout(wheelTimeoutId);
		wheelTimtoutId = setTimeout( function() {
			var map = document.getElementById("mapFrame").contentWindow.map;
			map.setZoom(map.getZoom() + ((wEvent.deltaY > 0)? -1:1) );
		}, 100);
	});
}

function drawCursor(x) {
	if (cursorX == x) return x;
	// SVG取得
	var svg = d3.select("#result svg");
	// 高さ取得
	var w = svg.node().getBoundingClientRect().width;
	var h = svg.node().getBoundingClientRect().height;
	
	if (x < lpad || x > w-rpad) return -1;
	
	cursorX = x;
	
	// カーソルをあらわす縦線
	var cursorLine = d3.svg.line()
		.x(function() { return cursorX; })
		.y(function(d) { return d; });
	// カーソルの線をひく
	svg.select("path#cursor") // append でなく、select 利用
		.attr("class", "cursor")
		.attr("d", cursorLine([4, h-4]));
	return x;
}
function drawWindowLine(x) {
	// SVG取得
	var svg = d3.select("#result svg");
	// 高さ取得
	var h = svg.node().getBoundingClientRect().height;
	// ウィンドウカーソルをあらわす縦線
	var cursorLine = d3.svg.line()
		.x(function() { return windowX; })
		.y(function(d) { return d; });
	// ウィンドウカーソルの線をひく
	svg.select("path#window") // append でなく、select 利用
		.attr("class", "window-line")
		.attr("d", cursorLine([4, h-4]));
}

function moveCursor(x) {
	var x = drawCursor(x);
	if (x == -1) return;
	// map の cursor を移動
	var mf = document.getElementById("mapFrame").contentWindow;
	mf.setCursorAtTime(xScale.invert(x).getTime());
}

function moveInfoWindow(x, panTo) {
	var x = drawCursor(x);
	if (x == -1) return;
	windowX = x;
	drawWindowLine(x);
	// map の InfoWindow を表示
	var mf = document.getElementById("mapFrame").contentWindow;
	mf.setInfoWindowAtTime(xScale.invert(x).getTime(), panTo);
}

function moveInfoWindowAtTime(t) {
	var x = xScale(t);
	windowX = x;
	drawWindowLine(x);
}

/**
 * 画面描画します
 */
function draw(data) {
	data.unshift( {
		lat:data[0].lat,
		lng:data[0].lng,
		alt:-2,
		time:data[0].time-1,
		velo:0,
		svelo:0
	} );
	data.push( {
		lat:data[data.length-1].lat,
		lng:data[data.length-1].lng,
		alt:-2,
		time:data[data.length-1].time+1,
		velo:0,
		svelo:0
	} );
	// SVG取得
	var div = d3.select('#result');
	var svg = div.select("svg");
	svg.remove();
	svg = div.append("svg");
	
	// d3.select で取れる obj は d3 でラップされたものと思われる。
	// node() で HTMLElement のような DOM オブジェクトが取得できるらしい。
	var w = svg.node().getBoundingClientRect().width;
	var h = svg.node().getBoundingClientRect().height;
	
	// クリア(この方法は要素が増え続けるのでNG!)
	svg.append("rect")
		.attr({width:w, height:h, fill:"#FFFFFF"});
	
	
	// xスケール
	var xmin = d3.min(data, function(d) { return d.time; });
	var xmax = d3.max(data, function(d) { return d.time; });
	
	xScale = d3.time.scale()
		.domain([xmin, xmax])
		.range([lpad, w - rpad]);
	
	// yスケール
	var ymin = d3.min(data, function(d) { return d.alt; });
	var ymax = d3.max(data, function(d) { return d.alt; });
	var yvmax = d3.max(data, function(d) { return d.svelo; });
	
	var yScale = d3.scale.linear()
		.domain([-3, ymax+(3800-ymax)/20])
		.range([h - bpad, tpad]);
	
	var yvScale = d3.scale.linear()
		.domain([0, yvmax+(150-yvmax)/10])
		.range([h - bpad, tpad]);
	
	// x軸
	var xAxis = d3.svg.axis()
		.scale(xScale)
		.tickFormat(function(d) {
			return d3.time.format('%H:%M')(new Date(d))
		});
	
	// y軸
	var yAxis = d3.svg.axis()
		.scale(yScale)
		.ticks(4)
	//	.tickSize([6,6])
		.orient("left");
	var yvAxis = d3.svg.axis()
		.scale(yvScale)
		.ticks(4)
		.orient("right");
	
	// 高度をあらわす折れ線グラフ
	var line = d3.svg.line()
		.x(function(d, i) { return xScale(d.time); })
		.y(function(d, i) { return yScale(d.alt); });
	
	// 速度をあらわす折れ線グラフ
	var vline = d3.svg.line()
		.x(function(d, i) { return xScale(d.time); })
		.y(function(d, i) { return yvScale(d.svelo); });
	
	//
	// 空っぽい fill のグラデーション指定
	//
	gradient = svg.append("svg:defs")
		.append("svg:linearGradient")
		.attr({id:"skygradient", x1:"0%", y1:"0%", x2:"0%", y2:"100%"});
	gradient.append("svg:stop")
		.attr("offset", "0%")
		.attr("stop-color", "#2070FF") //"#40E0FF")
		.attr("stop-opacity", 1)

	gradient.append("svg:stop")
		.attr("offset", "70%")
		.attr("stop-color", "#40E0FF") //"#2070FF")
		.attr("stop-opacity", 1)
	
	// 山っぽい fill のグラデーション指定
	gradient = svg.append("svg:defs")
		.append("svg:linearGradient")
		.attr({id:"gradient", x1:"0%", y1:"0%", x2:"0%", y2:"100%"});
	gradient.append("svg:stop")
		.attr("offset", "20%")
		.attr("stop-color", "#FFFF80")
		.attr("stop-opacity", 1)

	gradient.append("svg:stop")
		.attr("offset", "80%")
		.attr("stop-color", "#006600")
		.attr("stop-opacity", 1)
	
	// 空をかく
	svg.append("rect")
		.attr( {x:lpad, y:tpad, width:w-lpad-rpad, height:h-tpad-bpad} )
		.attr("fill", "url(#skygradient)");
	
	// 高度の線をひく
	svg.append("path")
		.attr("class", "altitude")
		.attr("d", line(jsonData))
		.attr("fill", "url(#gradient)");
	// 速度の線をひく
	svg.append("path")
		.attr("class", "velocity")
		.attr("d", vline(jsonData));
	
   	// 時刻を表す軸
	svg.append("g")
		.attr("class", "timeaxis")
		.attr("transform", "translate(" + 0 + ", " + (h - bpad) + 	")")
		.call(xAxis)
		.selectAll("text")
		.attr("x", 10)
		.attr("y", -15)
//		.attr("transform", "rotate(90)")
		.style("text-anchor", "start");
	
	// 高度の軸
	svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + lpad + ", 0)")
		.call(yAxis);
	// 速度の軸
	svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + (w-rpad) + ", 0)")
		.call(yvAxis);
	
	//
	svg.append("path").attr("id", "window");
	svg.append("path").attr("id", "cursor");
		
	data.shift();
	data.pop();

}

