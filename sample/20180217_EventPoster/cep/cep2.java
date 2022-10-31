/*
 * Context を使ってみる
 *
 * デバイスイベントの event.source.value によって分けるコンテキスト
 */
create context DevicewiseContext
	partition by event.source.value from EventCreated;

context DevicewiseContext
select
	sum(getNumber(e.event, "postValue.value")), count(*), last(*)
from
	EventCreated.win:length(2) e;
