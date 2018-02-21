/**
 * 毎日 9:00～17:00 と source.id で分けられるコンテキスト
 */
create context ServiceTimeAndDevicewise
	context ServiceTime start (*, *, *, *, *, 0) end (*, *, *, *, *, 30),
	context Devicewise partition by event.source.value from EventCreated;

/**
 * コンテキストの中で、平均値を計算する
 */
context ServiceTimeAndDevicewise
select
	avg(getNumber(e.event, "postValue.value")),
	e.event.source.value
from
	EventCreated e
output last every 5 seconds;

