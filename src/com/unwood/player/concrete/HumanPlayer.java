package com.unwood.player.concrete;

import java.util.List;

import com.unwood.fivechess.Point;
import com.unwood.player.base.BasePlayer;
import com.unwood.player.interfaces.IPlayer;



public class HumanPlayer extends BasePlayer implements IPlayer{

	//��һ�����ӣ���������Ѿ��µ����Ӽ�����
	@Override
	public void run(List<Point> enemyPoints,Point p) {
		getMyPoints().add(p);
		allFreePoints.remove(p);
	}
}
