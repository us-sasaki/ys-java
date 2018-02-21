/**
 * ���� 9:00�`17:00 �� source.id �ŕ�������R���e�L�X�g
 */
create context ServiceTimeAndDevicewise
	context ServiceTime start (*, *, *, *, *, 0) end (*, *, *, *, *, 30),
	context Devicewise partition by event.source.value from EventCreated;

/**
 * �R���e�L�X�g�̒��ŁA���ϒl���v�Z����
 */
context ServiceTimeAndDevicewise
select
	avg(getNumber(e.event, "postValue.value")),
	e.event.source.value
from
	EventCreated e
output last every 5 seconds;

