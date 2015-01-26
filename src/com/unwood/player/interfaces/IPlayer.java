package com.unwood.player.interfaces;

import java.util.List;

import com.unwood.fivechess.IChessboard;
import com.unwood.fivechess.Point;


public interface IPlayer {
	
	//��һ�����ӣ���������Ѿ��µ����Ӽ���
	public void run(List<Point> enemyPoints, Point point);//HumanPlayer.java��

	//�ж����Ƿ��Ƿ�Ӯ��
	public boolean hasWin();
	
	public void setChessboard(IChessboard chessboard);
	
	public List<Point> getMyPoints();
	
}
