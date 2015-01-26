package com.unwood.player.concrete;

import java.util.List;

import com.unwood.fivechess.Point;
import com.unwood.player.base.BasePlayer;
import com.unwood.player.interfaces.IPlayer;



public class HumanPlayer extends BasePlayer implements IPlayer{

	//下一步棋子，传入对手已经下的棋子集合中
	@Override
	public void run(List<Point> enemyPoints,Point p) {
		getMyPoints().add(p);
		allFreePoints.remove(p);
	}
}
