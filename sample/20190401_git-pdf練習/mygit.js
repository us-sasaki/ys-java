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
	 * git デフォルトではパスの中で日本語が \(8進数) のようなコードに
	 * エンコードされる。この形式では、prefix() と互換性がない。
	 * これを回避するために、 .git/config の[core]エントリに次を加える。
	 * quotepath = false
	 * これで日本語がエンコードされなくなる。
	 *
	 * @return	{Array}	ファイルパス(git root からの相対パス)の配列
	 * 
	 */
	static lastModifiedFiles() {
		const commitId = GitUtils.lastCommitedIds()[0];
		const modified = execSync("git log -1 --name-only --pretty= "+commitId);
		return modified.toString().split(/\r\n|\r|\n/);
	}
	
	/**
	 * このディレクトリ配下の git 管理されているファイルを取得します。
	 *
	 * @return	{Array}	ファイルパス(相対パス)の配列
	 */
	static managedFiles() {
		const commitId = GitUtils.lastCommitedIds()[0];
		const managed = execSync('git ls-tree --name-only -r '+commitId);
		return managed.toString().split(/\r\n|\r|\n/);
	}
	
	/**
	 * git root から、このディレクトリまでの相対パスを返します。
	 * lastModifiedFiles は git root からの相対パスで
	 * managedFiles はこのディレクトリからの相対パスのため、git root
	 * に合わせるのにこの関数を利用できます。
	 *
	 * @param	{String}	path	相対パス
	 * @return	{String}	path が指定されていた場合、git root からの相対
	 *						パス、指定されていない場合、このディレクトリへの
	 *						相対パス
	 */
	static prefix(path) {
		const prefix = execSync('git rev-parse --show-prefix');
		let pref = prefix.toString(); // buffer 型 -> string
		pref = pref.substring(0, pref.length-1); // 最後に改行(\n)がある
		if (path === void 0) return pref;
		return pref + path;
	}
	
	/**
	 * 現在のディレクトリ以下のファイルで、直近のコミットで削除された
	 * ファイルの相対パスを返却します。
	 */
	static deletedFiles() {
		const managed = GitUtils.managedFiles().map(GitUtils.prefix);
		const lastModified = GitUtils.lastModifiedFiles();
		
		// lastModified にあって、managed になく、prefix ではじまるものが答え
		const pref = GitUtils.prefix();
		return lastModified.filter(path => path.startsWith(pref))
							.filter(path => !managed.includes(path));
	}
	
	static modifiedFiles() {
		const managed = GitUtils.managedFiles().map(p => GitUtils.prefix(p));
		const lastModified = GitUtils.lastModifiedFiles();
		
		// managed にあって、lastModified にもあるものが答え
		return managed.filter(path => lastModified.includes(path));
	}
	
	static otherFiles() {
		const managed = GitUtils.managedFiles().map(GitUtils.prefix);
		const lastModified = GitUtils.lastModifiedFiles();
		
		// lastModified の中にあって、prefix ではじまらないもの
		const pref = GitUtils.prefix();
		return lastModified.filter(path => !path.startsWith(pref));
	}
	
	
	
//		const modified = execSync("git log -1 --pretty=format:'' "+commitId);

//	static lastModifiedFiles() {
//		const commitId = GitUtils.lastCommitedIds()[0];
//		const modified = execSync('git ls-tree --name-only -r '+commitId);
//		return modified.toString().split('/\r\n|\r|\n/');
//	}
	
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
