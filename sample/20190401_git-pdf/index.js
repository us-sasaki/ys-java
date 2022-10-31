const fs = require('fs');
const path = require('path');
const mygit = require('./mygit.js').gitUtils;

console.log(require('./mygit.js'));

/**
 * gitlab Webhook として使用する microservice の作成テスト用
 * push されたファイルと同じディレクトリに 'createdPdfs' ディレクトリがある
 * 場合、pdf ファイルを生成する
 */
const pushedFiles = ['./git/a.txt', './git/b.txt', './git/notExists.txt'];

const fileExists = function(filePath) {
	try {
		fs.statSync(filePath);
		return true;
	} catch(err) {
		return false;
	}
}

pushedFiles.map( text => { let p = path.parse(text); p.path = text; return p; } )
			.map ( p => { p.exists = fileExists(p.path); return p; })
			.forEach( obj => {console.log(obj);} );

const log = (line => console.log(line));

/*
log("---- commit id");
mygit.lastCommitedIds().forEach(log);
log("---- last modified files");
mygit.lastModifiedFiles().forEach(log);
log("---- managed files");
mygit.managedFiles().forEach(log);
log("---- prefix = "+mygit.prefix());
log("---- deleted files");
mygit.deletedFiles().forEach(log);
log("---- modified files");
mygit.modifiedFiles().forEach(log);
log("---- unchanged files");
mygit.unchangedFiles().forEach(log);
log("---- other files");
mygit.otherFiles().forEach(log);
*/

mygit.createPdf();
