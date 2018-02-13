package misc;

import java.util.*;

/**
 * diffmd.yaml の内容を保持するオブジェクト
 *
 * @version		February 13, 2018
 * @author		Yusuke Sasaki
 */
public class DiffMdProps {
	protected String target;
	protected String[] except;
	
	/**
	 * ターゲットディレクトリ
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	
	/**
	 * ターゲットディレクトリ
	 */
	public String getTarget() {
		return target;
	}
	
	/**
	 * 例外ディレクトリ/ファイル
	 */
	public void setExcept(String[] except) {
		this.except = except;
	}
	
	/**
	 * 例外ディレクトリ/ファイル
	 */
	public String[] getExcept() {
		return except;
	}
	
	
}
