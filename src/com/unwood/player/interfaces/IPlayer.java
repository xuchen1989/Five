package com.unwood.player.interfaces;

import java.util.List;

import com.unwood.fivechess.IChessboard;
import com.unwood.fivechess.Point;


public interface IPlayer {
	
	//下一步棋子，传入对手已经下的棋子集合
	public void run(List<Point> enemyPoints, Point point);//HumanPlayer.java中

	//判断我是否是否赢了
	public boolean hasWin();
	
	public void setChessboard(IChessboard chessboard);
	
	public List<Point> getMyPoints();
	
}
