function table(filename, chartname, labelStr) {

d3.json(filename, function(myData) {

nv.addGraph(function() {
  var chart = nv.models.indentedTree()
                .tableClass('table table-striped') //for bootstrap styling
                .columns([
                  {
                    key: 'label',
                    label: 'File Path',
                    showCount: true,
                    width: '75%',
                    type: 'text',
                  },
                  {
                    key: 'value',
                    label: labelStr,
                    width: '10%',
                    type: 'text'
                  },
                  {
                    key: 'owner',
                    label: 'Owner',
                    width: '15%',
                    type: 'text'
                  }
                ]);


  d3.select('#'+chartname)
      .datum(myData)
    .call(chart);

  return chart;
});

});

}
