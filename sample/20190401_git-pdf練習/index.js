const fs = require('fs');
const path = require('path');
const mygit = require('./mygit.js').gitUtils;

/**
 * gitlab Webhook �Ƃ��Ďg�p���� microservice �̍쐬�e�X�g�p
 * push ���ꂽ�t�@�C���Ɠ����f�B���N�g���� 'createdPdf' �f�B���N�g��������
 * �ꍇ�Apdf �t�@�C���𐶐�����
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
log("---- commit id");
mygit.lastCommitedIds().forEach(log);
log("---- last modified files");
mygit.lastModifiedFiles().forEach(log);
log("---- managed files");
mygit.managedFiles().forEach(log);
log("---- deleted files");
mygit.deletedFiles().forEach(log);
log("---- modified files");
mygit.modifiedFiles().forEach(log);
log("---- other files");
mygit.otherFiles().forEach(log);
