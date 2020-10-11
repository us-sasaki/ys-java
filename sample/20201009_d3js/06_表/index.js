const dataset = [];

for (let i = 0; i < 5; i++) {
  const row = [];
  for (let j = 0; j < 30; j++) {
    row.push(i+j*5);
  }
  dataset.push(row);
}

const COLORS = ['blue', 'red', 'yellow', 'violet', 'lightgreen', 'steelblue'];

d3.select("body")
  .append("table")
  .attr("border", "1") // 枠線表示
  .append("tbody")
  .selectAll("tr")
  .data(dataset)
  .enter()
  .append("tr")
  .selectAll("td")
  .data( row => row )
  .enter()
  .append("td")
  .text( d => "_"+d )
  .style("background-color", d => COLORS[d % COLORS.length]);