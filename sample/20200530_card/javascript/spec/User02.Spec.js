'use strict';

const startBridge = function() {
	( async () => {
		//
		const m = new PlayMain('canvas');
		problem.forEach( p => m.addProblem(Problem.regular(p)));
		
		while (true) {
			await m.start();
		}
	
	})();
};

