var metaData;

// -------------- jQuery -----------------

$(function(){

// モーダルコンテントの設定
$.getJSON('json/GPSMetaData.json', function(data) {
	metaData = data;
	var tblhtml = '<table class="ride-list"><tbody>';
	tblhtml += '<tr><th>#</th><td>title</td><td>date</td><td>dist.</td><td>ave.</td><td>max</td><td>photos</td><td>plots</td></tr>';
	
	for (var i = 0; i < metaData.length; i++) {
		var d = metaData[i];
		tblhtml += "<tr><th>"+(i+1)+"</th><td><a href='javascript:openRide("+i+")'>"+d.title+"</a></td>";
		tblhtml += "<td>"+d.startDate+"</td><td>"+d.totalDistance+"km</td><td>"+d.averageSpeed+"km/h</td>";
		tblhtml += "<td>"+d.maxSpeed+"km/h</td><td>"+d.photos+"個</td><td>"+d.plots+"</td></tr>";
	}
	tblhtml += '</tbody></table>';
	$("#modal-content").append(tblhtml);
});

//モーダルウィンドウを出現させるクリックイベント
$("#modal-open").click( function(){

	//キーボード操作などにより、オーバーレイが多重起動するのを防止する
	$( this ).blur() ;	//ボタンからフォーカスを外す
	if( $( "#modal-overlay" )[0] ) return false ;		//新しくモーダルウィンドウを起動しない

	//オーバーレイを出現させる
	$( "body" ).append( '<div id="modal-overlay"></div>' ) ;
	$( "#modal-overlay" ).fadeIn( "slow" ) ;

	//コンテンツをセンタリングする
	centeringModal() ;

	//コンテンツをフェードインする
	$( "#modal-content" ).fadeIn( "slow" ) ;
} ) ;

//リサイズされたら、センタリングをする関数[centeringModal()]を実行する
$( window ).resize( centeringModal ) ;

	//センタリングを実行する関数
	function centeringModal() {

		//画面(ウィンドウ)の幅、高さを取得
		var w = $( window ).width() ;
		var h = $( window ).height() ;

		// コンテンツ(#modal-content)の幅、高さを取得
		// jQueryのバージョンによっては、引数[{margin:true}]を指定した時、不具合を起こします。
		var cw = $( "#modal-content" ).outerWidth();
		var ch = $( "#modal-content" ).outerHeight();

		//センタリングを実行する
		$( "#modal-content" ).css( {"left": ((w - cw)/2) + "px","top": ((h - ch)/2) + "px"} ) ;
	}
	
} ) ;

function removeOverlay() {
		//[#modal-content]と[#modal-overlay]をフェードアウトした後に…
		$( "#modal-content,#modal-overlay" ).fadeOut( "slow" , function(){
			//[#modal-overlay]を削除する
			$('#modal-overlay').remove() ;
		} ) ;
}

function openRide(index) {
	// Google Map の iframe の中を取得
	// mf.func( ) で drawmap.js 内の関数を呼べる
	var mf = document.getElementById("mapFrame").contentWindow;
	
	removeOverlay();
	// Jsonファイルを指定して、Google Map 上に Polyline と Marker(写真 InfoWindow付)
	// を表示
	mf.setJsonFile('json/'+metaData[index].log);
	
}
