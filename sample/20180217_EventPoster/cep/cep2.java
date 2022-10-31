/*
 * Context ���g���Ă݂�
 *
 * �f�o�C�X�C�x���g�� event.source.value �ɂ���ĕ�����R���e�L�X�g
 */
create context DevicewiseContext
	partition by event.source.value from EventCreated;

context DevicewiseContext
select
	sum(getNumber(e.event, "postValue.value")), count(*), last(*)
from
	EventCreated.win:length(2) e;
