/* d3.min.js を読み込み済み */

/* この書き方 */
let margin = {
    left: 30,
    right: 30,
    bottom: 50,
    top: 30,
};

/* SVG要素のサイズ */
let width = window.innerWidth;
let height = window.innerHeight;

/* SVG要素を取得 & サイズ設定 */
let svg = d3.select("#svg-area")
  .attr("width", width)
  .attr("height", height);

  /* x, y 軸のスケールを作成する */
let xScale = d3.scaleLinear()    // v4 から scale.linear() ではなくなった
  .domain([0, 100])    // 入力値の範囲
  .range([margin.left, width - margin.right]);    // 出力位置の範囲
let yScale = d3.scaleLinear()
  .domain([24, 0])    // 一日の 00:00 ~ 24:00、みたいなものをイメージした
  .range([margin.top, height - margin.bottom]);

/* x 軸を描画する */
svg.append("g")
  .attr("class", "x_axis")
  .attr(
    "transform",
    "translate(" + [
      0,
      // 0    // このようにすると画面上部に軸が表示されるので..
      height - margin.bottom    // このように平行移動させる
    ].join(",") + ")"
  )
  .call(
    d3.axisBottom(xScale)
      .ticks(10)
  );

/* y 軸を描画する */
svg.append("g")
  .attr("class", "y_axis")
  .attr(
    "transform",
    "translate(" + [
      //0,    // このようにすると左にズレた状態で軸(目盛り)が描画されるので..
      margin.left,    // 右に平行移動させる
      0
    ].join(",") + ")"
  )
  .call(
    d3.axisLeft(yScale)
      .ticks(24)
  );
