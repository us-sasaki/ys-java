let dataset = [
  { date: "2020-05", value: [20, 10, 5] },
  { date: "2020-06", value: [90, 23, 5] },
  { date: "2020-07", value: [50, 55, 5] },
  { date: "2020-08", value: [33, 34, 5] },
  { date: "2020-09", value: [95, 11, 50] }
//  { date: "2020-05", value: {a:20, b:10, c:5} },
//  { date: "2020-06", value: {a:90, b:23, c:5} },
//  { date: "2020-07", value: {a:50, b:55, c:5} },
//  { date: "2020-08", value: {a:33, b:34, c:5} },
//  { date: "2020-09", value: {a:95, b:11, c:5} }
];
let dataset2 = [ 3, 1, 6, 7, 5 ]; // 折れ線用

const width = 720;
const height = 300;

const svg = d3.select("body").append("svg").attr("width", width).attr("height", height);
// tooltip用div要素追加
const tooltip = d3.select("body").append("div").attr("class", "chart--tooltip");

const padding = 30;

/** dataset を index を含む object array に変換する */
const timeparser = d3.timeParse("%Y-%m");
dataset = dataset.map( d => ({ date: d.date, value:d.value }) );
dataset2 = dataset2.map( (d, idx) => ({ date: dataset[idx].date, value:d}));

/**
 * x 軸の尺度(x 軸の値はラベル型..scaleBand を使う)
 * scaleBand の domain はラベルの配列
 */
const xScale = d3.scaleBand()
  .domain( dataset.map( d => d.date ))
  .range([padding, width - padding]);

/** y 軸(棒)の尺度 */
const yScale = d3.scaleLinear()
  .domain([0, d3.max(dataset, d => Object.keys(d.value).map(e => d.value[e]).reduce( (a, v) => a+v ))]) //d.value.a + d.value.b )])
  .range([height - padding, padding]);

/** y 軸(折れ線)の尺度 */
const yLScale = d3.scaleLinear()
  .domain([0, d3.max(dataset2, d => d.value)])
  .range([height - padding, padding]);

/** x, y 軸描画関数 (svg g 要素で call する) */
const axisx = d3.axisBottom(xScale)
  .ticks(dataset.length)
const axisy = d3.axisLeft(yScale)
  .ticks(10);
const axisyL = d3.axisRight(yLScale)
  .ticks(10);

/** 棒の描画 */
const bandwidth = xScale.bandwidth();
console.log("bandwidth="+bandwidth);

const barWidth = bandwidth / 2; // bandwidth() で１目盛りの描画幅がわかる

const series = d3.stack().keys(Object.keys(dataset[0].value))(dataset.map(d => d.value));
console.log(series);
const colors = ["#ff0000", "#00ff00", "#ffff00"];
// 行ごとのデータのためにグループを作成
const groups = svg.selectAll("g")
   .data(series)
   .enter()
   .append("g")
   .style("fill", function(d, i) {
      return colors[i];
   });
const rects = groups.selectAll("rect")
   .data( d => d )
   .enter()
   .append("rect")
   .attr("x", (d, i) => xScale(dataset[i].date) + (bandwidth - barWidth)/2)
   .attr("y", d => yScale(d[1]))
   .attr("height", d => yScale(d[0]) - yScale(d[1]))
   .attr("width", xScale.bandwidth()/2);

/** 描画(線のスタイルは css で指定) */
svg.append("g")
  .attr("transform", "translate(" + 0 + "," + (height - padding) + ")")
  .call(axisx);

svg.append("g")
  .attr("transform", "translate(" + padding + "," + 0 + ")")
  .call(axisy);

svg.append("g")
  .attr("transform", "translate(" + (width-padding) + "," + 0 + ")")
  .call(axisyL);


   /**
const rectGroup = svg.append("g");
const rect = rectGroup.selectAll(".bar")
  .data(dataset)
  .enter()
  .append("rect")
  .attr('class', "bar")
  .style("fill", "#4c94ff");

rect.attr("y", d => yScale(d.value[0]))
  .attr("x", d => xScale(d.date) + (bandwidth - barWidth)/2)
  .attr("width", barWidth)
  .attr("height", d => yScale(0) - yScale(d.value[0]));
*/

/** ツールチップの表示 
rect
  .on("mouseenter", (evt, d) => {
    console.log(`"entered (d)=(${JSON.stringify(d)})`);
    tooltip.style("opacity", 1)
      .html('<div>value:'+ d.value +'</div>')
      .style("transform", function(){ 
        let x = xScale(d.date) + (bandwidth / 2);
        let y = (yScale(d.value) - 30);
        return "translate("+ x + "px," + y +"px)";
    })
  })
  .on("mouseout", (evt, d) => {
    tooltip.style("opacity", 0);
  });

/** 折れ線の描画 */
/** line generator (svg path の d に指定する値を生成する関数) */
const line = d3.line()
  .x( d => { console.log(d); console.log(xScale(d.date)); return xScale(d.date)+(bandwidth/2); } )
  .y( d => yLScale(d.value) );

console.log(dataset);
console.log(dataset2);

svg.append("path")
  .datum(dataset2)
  .attr("fill", "none")
  .attr("class", "graph-line")
  .attr("d", line);