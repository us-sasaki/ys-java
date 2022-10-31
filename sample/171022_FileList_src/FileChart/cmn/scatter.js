function scatter(filename, chartname) {

d3.json(filename, function(myData) {

nv.addGraph(function() {
  var chart = nv.models.scatterChart()
                .showDistX(true)    //showDist, when true, will display those little distribution lines on the axis.
                .showDistY(true)
                .transitionDuration(350)
                .color(d3.scale.category10().range());

  //Configure how the tooltip looks.
  chart.tooltipContent(function(key, x, y) {
      return '<h3>' + key + '</h3><h4>削減:' + x + '<br>整理:' + y +'</h4>';
  });

  //Axis settings
  chart.xAxis.tickFormat(d3.format('.01f'));
  chart.yAxis.tickFormat(d3.format('.01f'));

  //We want to show shapes other than circles.
  chart.scatter.onlyCircles(true);

  d3.select("#"+chartname+" svg")
      .datum(myData)
      .call(chart);

  nv.utils.windowResize(chart.update);

  return chart;
});

});

}
