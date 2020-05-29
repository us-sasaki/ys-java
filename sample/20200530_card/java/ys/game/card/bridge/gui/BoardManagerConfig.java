package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * GUI �I�v�V�����A�v���C���e����@�\�Ƃ��ẮA�����܂߂���悤�ɂ�����
 * �E�v���C�i�r�Q�[�V����(����B����C�x���g�ł��݂ꂪ��������B)
 * �����f
 * ���_�u���_�~�[���[�h
 * �E���_�̐ݒ�
 * �E�����I�I�[�v�����[�h
 * �E�J�[�h�̗��̖͗l�ݒ�
 * �E�_�Ŏ��ԂȂǁAwait �̐ݒ�
 * �EUndo
 * �E�����̋@�\�̗L�����A������
 */
public class BoardManagerConfig {
	protected boolean	ddAvailable	= false;
	protected boolean	dd			= false;
	protected String	desc;
	protected String	contStr;
	protected String	title;
	
	public boolean doubleDummyIsAvailable() {
		return ddAvailable;
	}
	
	public void setDoubleDummy(boolean b) {
		dd = b;
	}
	
	public boolean doubleDummy() {
		if (!ddAvailable) return false;
		return dd;
	}
	
	public String getDescription() {
		return desc;
	}
	
	public String getContractString() {
		return contStr;
	}
	
	public String getTitle() {
		return title;
	}
}
