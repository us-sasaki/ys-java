const fs = require('fs');
const path = require('path');
const mygit = require('./mygit.js').gitUtils;

/**
 * gitlab Webhook として使用する microservice の作成テスト用
 * push されたファイルと同じディレクトリに 'createdPdf' ディレクトリがある
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

console.log("----");
mygit.lastCommitedIds().forEach(line => console.log(line));
console.log("----");
mygit.lastModifiedFiles().forEach(line => console.log(line));
console.log("----");
mygit.managedFiles().forEach(line => console.log(line));
console.log("----");
