/**
 * 単純に e.event.postRandom.value を select するだけ
 */
select
	getObject(e.event, "postRandom.value")
from EventCreated e;
