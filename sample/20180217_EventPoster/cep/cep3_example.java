http://esper.espertech.com/release-7.0.0/esper-reference/html_single/index.html#context_nesting

/**
 * 毎分0秒～30秒かつデバイス単位のパーティションコンテキスト
 */
create context ServiceTimeAndPerDevice
	context ServiceTime start (*, *, *, *, *, 0) end (*, *, *, *, *, 30),
	context PerDevice partition by measurement.source.value from MeasurementCreated;

/**
 * コンテキストそれぞれのパーティションで平均値を計算する
 */
context ServiceTimeAndPerDevice
select
	avg(getNumber(m.measurement, {{valueまでのJSON path}})),
	m.measurement.source.value
from
	MeasurementCreated m
output last every 5 seconds;
