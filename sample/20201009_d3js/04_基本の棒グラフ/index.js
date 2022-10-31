let dataset = [ 2, 4, 5, 10, 3, 7 ];

const width = 720;
const height = 300;

const svg = d3.select("body").append("svg").attr("width", width).attr("height", height);
const padding = 30;

/**
 * x 軸の尺度(x 軸の値はラベル型..scaleBand を使う)
 * scaleBand の domain はラベルの配列
 */
const xScale = d3.scaleBand()
  .domain( dataset.map( (d, idx) => idx ))
  .range([padding, width - padding]);

/** y 軸の尺度 */
const yScale = d3.scaleLinear()
  .domain([0, d3.max(dataset, d => d)])
  .range([height - padding, padding]);

/** x, y 軸描画関数 (svg g 要素で call する) */
const axisx = d3.axisBottom(xScale)
  .ticks(dataset.length)
const axisy = d3.axisLeft(yScale)
  .ticks(10);

/** 描画(線のスタイルは css で指定) */
svg.append("g")
  .attr("transform", "translate(" + 0 + "," + (height - padding) + ")")
  .call(axisx);

svg.append("g")
  .attr("transform", "translate(" + padding + "," + 0 + ")")
  .call(axisy);

/** 棒の描画 */
const barWidth = xScale.bandwidth() / 2; // bandwidth() で１目盛りの描画幅がわかる
const rectGroup = svg.append("g");
const rect = rectGroup.selectAll(".bar")
  .data(dataset)
  .enter()
  .append("rect")
  .attr('class', "bar")
  .style("fill", "#4c94ff");

rect.attr("y", d => yScale(d))
  .attr("x", (d,idx) => xScale(idx) + (xScale.bandwidth() - barWidth)/2)
  .attr("width", barWidth)
  .attr("height", d => yScale(0) - yScale(d));
