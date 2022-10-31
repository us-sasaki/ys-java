let dataset = [
  { date: "2007-10", value: 20 },
  { date: "2007-11", value: 90 },
  { date: "2007-12", value: 50 },
  { date: "2008-01", value: 33 },
  { date: "2008-02", value: 95 },
  { date: "2008-03", value: 12 },
  { date: "2008-04", value: 44 },
  { date: "2008-05", value: 67 },
  { date: "2008-06", value: 21 },
  { date: "2009-12", value: 88 },
];
const width = 720;
const height = 300;

const svg = d3.select("body").append("svg").attr("width", width).attr("height", height);
const padding = 30;

const timeparser = d3.timeParse("%Y-%m");
dataset = dataset.map( d => ({ date: timeparser(d.date), value:d.value }) );

/** x 軸の尺度 */
const xScale = d3.scaleTime()
  .domain([d3.min(dataset, d => d.date), d3.max(dataset, d => d.date )])
  .range([padding, width - padding]);

/** y 軸の尺度 */
const yScale = d3.scaleLinear()
  .domain([0, d3.max(dataset, function(d){return d.value;})])
  .range([height - padding, padding]);

/** x, y 軸描画関数 (svg g 要素で call する) */
const axisx = d3.axisBottom(xScale)
  .ticks(10)
  .tickFormat(d3.timeFormat("%Y-%m-%d"));
const axisy = d3.axisLeft(yScale)
  .ticks(4);

/** line generator (svg path の d に指定する値を生成する関数) */
const line = d3.line()
  .x( d => xScale(d.date) )
  .y( d => yScale(d.value) );

/** 描画(線のスタイルは css で指定) */
svg.append("g")
  .attr("transform", "translate(" + 0 + "," + (height - padding) + ")")
  .call(axisx);

svg.append("g")
  .attr("transform", "translate(" + padding + "," + 0 + ")")
  .call(axisy);

svg.append("path")
  .datum(dataset)
  .attr("fill", "none")
  .attr("class", "graph-line")
  .attr("d", line);