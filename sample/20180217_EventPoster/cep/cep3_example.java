http://esper.espertech.com/release-7.0.0/esper-reference/html_single/index.html#context_nesting

/**
 * ����0�b�`30�b���f�o�C�X�P�ʂ̃p�[�e�B�V�����R���e�L�X�g
 */
create context ServiceTimeAndPerDevice
	context ServiceTime start (*, *, *, *, *, 0) end (*, *, *, *, *, 30),
	context PerDevice partition by measurement.source.value from MeasurementCreated;

/**
 * �R���e�L�X�g���ꂼ��̃p�[�e�B�V�����ŕ��ϒl���v�Z����
 */
context ServiceTimeAndPerDevice
select
	avg(getNumber(m.measurement, {{value�܂ł�JSON path}})),
	m.measurement.source.value
from
	MeasurementCreated m
output last every 5 seconds;
