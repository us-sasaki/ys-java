const fs = require('fs');
const path = require('path');

/**
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
