package com.unwood.fivechess;

import java.util.List;


public interface IChessboard {
	public int getMaxX();  //取得棋盘最大横坐标
	public int getMaxY();  //取得棋盘最大纵坐标
	public List<Point> getFreePoints();  //取得当前所有空白点，这些点才可以下棋。Chessboard.java中
}
