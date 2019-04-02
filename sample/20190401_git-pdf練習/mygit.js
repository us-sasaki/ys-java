const fs = require('fs');
const execSync = require('child_process').execSync;

/**
 * git サービスを扱うのに便利な static 関数を定義する
 * 利用するときは、カレントディレクトリが git ディレクトリである必要がある。
 *
 * @author		Yusuke Sasaki
 */
class GitUtils {
	
	static clone(url) {
		const ret = execSync('git clone '+url);
	}
	
	static pull() {
		const ret = execSync('git pull');
	}
	
	/**
	 * 末尾指定した個数の commit id  のファイルを取得します。
	 *
	 * @param	n {number} 取得する commit id の数(省略すると 1)
	 * @return	{Array} commit id
	 */
	static lastCommitedIds(n) {
		if (n === void 0) n = 1;
		const commitIds = execSync('git log -'+n+' --pretty=%H')
								.toString().split('/\r\n|\r|\n/');
		return commitIds;
	}
	
	/**
	 * 最後の commit で変更されたファイルの一覧を取得します。
	 *
	 * @return	{Array}	ファイルパスの配列
	 * 
	 */
	static lastModifiedFiles() {
		const commitId = GitUtils.lastCommitedIds()[0];
		const modified = execSync('git ls-tree --name-only -r '+commitId);
		return modified.toString().split('/\r\n|\r|\n/');
	}
	
	/**
	 * 
	 */
	static managedFiles() {
		const commitId = GitUtils.lastCommitedIds()[0];
		const managed = execSync('git ls-tree --full-name -r '+commitId);
		return managed.toString().split('/\r\n|\r|\n/');
	}
	
	// 変更された(削除された場合を含む)ファイル一覧が出る
	//   git ls-tree --name-only -r {commit-id}
	// 指定した id に含まれる全ファイルが表示される
	//   git ls-tree --full-name -r {commit-id}
	// 全ファイル一覧に、変更されたファイルがあれば、変更されて存在するファイル
	// 100644 blob 43963d1daf19ee14b1c84567ae6f5cf2505fcae0
	//     "sample/20190401_git-pdf\347\267\264\347\277\222/git/a.txt"
	// のような表示となる
	//
	// 指定したcommit-id のファイル名を表示する(上と同等？)
	//   git log -1 --name-only --pretty=format:'' {commit-id}
	//
	// 過去指定した履歴数分の commit id を取得する
	//   git log -{履歴数} --pretty=%H
	//   新しいもの～古いものと並ぶ
}

/*---------
 * exports
 */
exports.gitUtils = GitUtils;
