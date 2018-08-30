function buildTimed(selector, data) {
    nv.addGraph(function() {
        var chart = nv.models.lineChart()
            .x(function(d) { return d[0] })
            .y(function(d) { return d[1] })
            .color(d3.scale.category10().range());
            //.tickFormat(d3.time.format("%d/%m/%Y"));
        //.useInteractiveGuideline(true);

        ;

        chart.margin({bottom: 100});

        chart.xAxis
            .orient("bottom")
            .rotateLabels(-45)
            .tickFormat(function(d) {
                return d3.time.format('%d/%m/%Y')(new Date(d))  // %x
            });



        chart.yAxis.tickFormat(d3.format(''));

        d3.select(selector)
            .datum(data)
            .transition().duration(500)
            .call(chart)
        ;

        nv.utils.windowResize(chart.update);

        return chart;
    });
}


function buildContributionByQuestion(selector, data) {
    nv.addGraph(function() {
        var chart = nv.models.multiBarHorizontalChart()
            .x(function(d) { return d.label })
            .y(function(d) { return d.value })
            .margin({top: 30, right: 20, bottom: 50, left: 80})
            .showValues(true)
            .tooltips(true)
            .showControls(false)
            .stacked(true);

        chart.yAxis
            .tickFormat(d3.format(',.0f'));

        d3.select(selector)
            .datum(data)
            .transition().duration(500)
            .call(chart);

        nv.utils.windowResize(chart.update);

        return chart;
    });
}

function buildHorizontalDist(selector, data) {
    nv.addGraph(function() {
        var chart = nv.models.multiBarHorizontalChart()
            .x(function(d) { return d.label })
            .y(function(d) { return d.value })
            .margin({top: 30, right: 20, bottom: 50, left: 80})
            .showValues(true)
            .tooltips(true)
            .showControls(false)
            .stacked(true);

        chart.yAxis
            .tickFormat(d3.format(',.0f'));

        d3.select(selector)
            .datum(data)
            .transition().duration(500)
            .call(chart);

        nv.utils.windowResize(chart.update);

        return chart;
    });
}


function buildDemo(selector, data) {
    nv.addGraph(function() {
        var chart = nv.models.multiBarHorizontalChart()
            .x(function(d) { return d.label })
            .y(function(d) { return d.value })
            .margin({top: 30, right: 20, bottom: 50, left: 80})
            .showValues(true)
            .tooltips(true)
            .showControls(false)
            .stacked(true);

        chart.yAxis
            .tickFormat(d3.format(',.0f'));

        d3.select(selector)
            .datum(data)
            .transition().duration(500)
            .call(chart);

        nv.utils.windowResize(chart.update);

        return chart;
    });
}