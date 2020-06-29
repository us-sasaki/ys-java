'use strict';

const startBridge = function() {
	( async () => {
		//
		try {
			const m = new PlayMain('canvas');
			const p = problem[0];

			m.addProblem(Problem.regular(p));
			await m.start();
		} catch (e) {
			// catch されていない。なぜか？
			console.log("error occurred!");
			throw e;
		}
	
	})();
};

