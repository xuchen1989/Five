package com.unwood.fivechess;

import java.util.List;


public interface IChessboard {
	public int getMaxX();  //ȡ��������������
	public int getMaxY();  //ȡ���������������
	public List<Point> getFreePoints();  //ȡ�õ�ǰ���пհ׵㣬��Щ��ſ������塣Chessboard.java��
}
