
function pieChart(filename, chartname) {

d3.json(filename, function(myData) {

//pie chart
nv.addGraph(function() {
  var chart = nv.models.pieChart()
      .x(function(d) { return d.label })
      .y(function(d) { return d.value })
      .showLabels(true);
      
    d3.select("#"+chartname+" svg")
        .datum(myData)
        .transition().duration(350)
        .call(chart);

  nv.utils.windowResize(chart.update);


  return chart;
});

});

}
