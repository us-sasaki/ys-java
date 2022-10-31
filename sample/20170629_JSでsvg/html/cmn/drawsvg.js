draw();

/**
 * 画面描画します
 */
function draw(data) {
	// SVG取得
	var div = d3.select('#result');
	var svg = div.select("svg");
	svg.remove();
	svg = div.append("svg");
	
	// d3.select で取れる obj は d3 でラップされたものと思われる。
	// node() で HTMLElement のような DOM オブジェクトが取得できるらしい。
	var w = svg.node().getBoundingClientRect().width;
	var h = svg.node().getBoundingClientRect().height;
	
	svg.append("rect")
		.attr({x:0, y:0, width:w/2, height:h/2, fill:"#FF80FF"});
	svg.append("rect")
		.attr({x:w/2, y:0, width:w/2, height:h/2, fill:"#80FFFF"});
	svg.append("rect")
		.attr({x:w/2, y:0, width:w/2, height:h/2, fill:"#80FFFF"});
	svg.append("rect")
		.attr({x:w/2, y:0, width:w/2, height:h/2, fill:"#80FFFF"});
}
