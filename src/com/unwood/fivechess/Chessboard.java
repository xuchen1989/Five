package com.unwood.fivechess;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;


import com.unwood.player.concrete.HumanPlayer;
import com.unwood.player.interfaces.IPlayer;
import com.unwood.player.base.BaseComputerAi;;


public class Chessboard extends View implements IChessboard{


	//游戏状态常量：
    //已准备好，可开局
    private static final int READY = 1;
    //已开局
    private static final int RUNNING = 2;
    //已结束
    private static final int PLAYER_TWO_LOST = 3;
    private static final int PLAYER_ONE_LOST = 4;
    
    //当前状态，默认为可开局状态
    private int currentMode = READY;
    
	//画笔对象
	private final Paint paint = new Paint();
	
	//代表白子
	private static final int WHITE = 0;
	
	//黑子
	private static final int BLACK = 1;
	
	//点大小
    private static int pointSize = 16;
		
	private static int boardHeight;
	private static int boardWidth;
	
	//不同颜色的Bitmap数组
	private Bitmap[] pointArray = new Bitmap[2];
	
	//屏幕右下角的坐标值，即最大坐标值
    private static int maxX;
    private static int maxY;
    
    //第一点偏离左上角像素，为了棋盘居中
	private static int yOffset;
	private static int xOffset;
	
	//两个玩家
	//第一个玩家默认为人类玩家
	private IPlayer player1 = new HumanPlayer();
	//第二个则根据选择人机战还是对战模式来初始化
	private IPlayer player2;
	//预先初始两个第二玩家
	//电脑玩家
	private static final IPlayer computer = new BaseComputerAi();
	//人类玩家
	private static final IPlayer human = new HumanPlayer();
	
	// 所有未下的空白点
	private final List<Point> allFreePoints = new ArrayList<Point>();
	
	
	private Context context=null;
	
	//声音
	private SoundPool snd;
	private int soundBegin;
	private int soundGo;
	private int soundOver;
	private int soundWin;

	
    public Chessboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setFocusable(true);
        
        //把两个颜色的点准备好，并放入数组
        Resources r = this.getContext().getResources();
        fillPointArrays(WHITE,r.getDrawable(R.drawable.white));      
        fillPointArrays(BLACK,r.getDrawable(R.drawable.black));
              
        setMessageBox(context);
        
        initSound();
        
   }
    
    private void setMessageBox(Context context){
    	new AlertDialog.Builder(context) 
        .setMessage("黑子先下") 
        .setPositiveButton("确定", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   if (currentMode == READY) {                                   	
                            		   if(fightingMode()==1){//人机对战
                            			   player2 = computer;
                                   	}else if(fightingMode()==2){//人人对战
                                   		   player2 = human;
                                   	}
                                   	restart();
                                   	setMode(RUNNING);
                                   	snd.play(soundBegin, 1, 1, 0, 0, 1);
                                   }
                                } 
                        }) 
        .show();
    }
    
    private void setMessageBox(Context context,String str){
    	new AlertDialog.Builder(context) 
        .setMessage(str+",选择菜单重新开始") 
        .setPositiveButton("确定", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   
                                } 
                        }) 
        .show();
    }
    
    
    //初始横线和竖线的数目
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        maxX = (int) Math.floor(w / pointSize)-1;    //即最右下角棋子的横纵坐标   
        maxY = (int) Math.floor((h) / pointSize)-1;

        //设置X、Y座标微调值，目的是使整个框居中
        xOffset = ((w - (pointSize * maxX)) / 2);
        yOffset = ((h - (pointSize * maxY)) / 2);
        
    }
    

   
       
    //画棋盘
    public void createboard(Canvas canvas)
    {
    	Resources res = this.getContext().getResources();
    	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.go);  
        canvas.drawColor(Color.WHITE);  
        canvas.drawBitmap(bmp, 0, 0, null);
        boardHeight=bmp.getHeight();
        boardWidth=bmp.getWidth();
         
    }
    
    //用获得的坐标画点
    private void drawPoint(Canvas canvas,Point p,int color){
    	canvas.drawBitmap(pointArray[color],p.x*pointSize+xOffset,p.y*pointSize+yOffset,paint);
    	
    }
        
    //画控制面板
    private void drawCtrl(Canvas canvas){
    	
    	Resources res = this.getContext().getResources();
    	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.goctrll);
    	Bitmap bmpfig = BitmapFactory.decodeResource(res, R.drawable.figrue);
    	   	
        canvas.drawBitmap(bmp, 0, boardHeight, null);
        
        Bitmap bmpman = BitmapFactory.decodeResource(res, R.drawable.man1);
    	canvas.drawBitmap(bmpman, 32, boardHeight+48, null);
    	
        if(isPlayer1Run()){//如果是第一玩家下棋
        	canvas.drawBitmap(bmpfig, boardWidth-64, boardHeight+48, null);//设置手指图标
        	//snd.play(soundGo, 1, 1, 0, 0, 1);//播放音效
		}else if(isPlayer2Run()){//如果是第二玩家下棋
			canvas.drawBitmap(bmpfig, boardWidth-144, boardHeight+48, null);//设置手指图标
			//snd.play(soundGo, 1, 1, 0, 0, 1);
		}
        
    }
    
    private void initSound()
    {
    	snd = new SoundPool(5, AudioManager.STREAM_SYSTEM,5);
    	soundBegin = snd.load(context,R.raw.begin , 0);
    	soundGo = snd.load(context,R.raw.go , 0);
    	soundWin = snd.load(context,R.raw.gamewin , 0);
    	soundOver = snd.load(context,R.raw.gameover , 0);
    }
      
    
    private int fightingMode()
    {
    	int mode=0;
    	mode=FiveChess.FIGHTTINGMODE;
    	return mode;
    }
    

    //设置运行状态
	public void setMode(int newMode) {
		currentMode = newMode;
		if(currentMode==PLAYER_TWO_LOST){
			//提示玩家2输了
			setMessageBox(this.context,"白子输了");
			snd.play(soundWin, 1, 1, 0, 0, 1);
			currentMode = READY;
		}else if(currentMode==RUNNING){
		//	textView.setText(null);
		}else if(currentMode==READY){
		//	textView.setText(R.string.mode_ready);
		}else if(currentMode==PLAYER_ONE_LOST){
			//提示玩家1输了
			setMessageBox(this.context,"黑子输了");
			snd.play(soundOver, 1, 1, 0, 0, 1);
			currentMode = READY;
		}
	}
	

	//监听键盘事件
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
		
		if((currentMode==PLAYER_TWO_LOST||currentMode==PLAYER_ONE_LOST) && keyCode == KeyEvent.KEYCODE_DPAD_CENTER){//重新开始
        	restart();
        	setMode(READY);
        }else{
        	return false;
        }
        return true;
	}
	
	//根据触摸点坐标找到对应点
	private Point newPoint(Float x, Float y){
		Point p = new Point(0, 0);
		for (int i = 0; i < maxX; i++) {
			if ((i * pointSize + xOffset) <= x
					&& x < ((i + 1) * pointSize + xOffset)) {
				p.setX(i);
			}
		}
		for (int i = 0; i < maxY; i++) {
			if ((i * pointSize + yOffset) <= y
					&& y < ((i + 1) * pointSize + yOffset)) {
				p.setY(i);
			}
		}
		return p;
	}
	
	//重新开始
	private void restart() {
		createPoints();//初始时全部空白点
		player1.setChessboard(this);
		player2.setChessboard(this);
		setPlayer1Run();
		//刷新一下
		refressCanvas();
	}
	
	//是否已开局
	private boolean hasStart(){
		return currentMode==RUNNING;
	}

	//处理触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		//还没有开局，或者是按下事件，不处理，只处理开局后的触摸弹起事件
		if(!hasStart() || event.getAction()!=MotionEvent.ACTION_UP){
			return true;
		}
		
		//是否正在处理一步棋的过程中
		if(onProcessing()){
			return true;
		}
		
		playerRun(event);
		
		return true;
	}
	
	private synchronized void playerRun(MotionEvent event){
		if(isPlayer1Run()){//第一玩家下棋
			player1Run(event);
		}else if(isPlayer2Run()){//第二玩家下棋
			player2Run(event);
		}
	}
	
	
	private void player1Run(MotionEvent event){
		Point point = newPoint(event.getX(), event.getY());
		if(allFreePoints.contains(point)){//此棋是否可下
			setOnProcessing();
			player1.run(player2.getMyPoints(),point);
			//刷新一下棋盘
			refressCanvas();//此处刷新手指的位置，下面第9行标记“~~”setPlayer2Run()将whoRun设置为2，使得手指指向player2（即白子）。存疑：为什么是这样？
			snd.play(soundGo, 1, 1, 0, 0, 1);//播放音效
			//判断第一个玩家是否已经下了
			if(!player1.hasWin()){//我还没有赢
				if(player2==computer){//如果第二玩家是电脑
					//100豪秒后才给玩家2下棋
					setPlayer2Run();
					refreshHandler.computerRunAfter(100);
				}else{
					setPlayer2Run();//~~
				}
			}else{
				//否则，提示游戏结束
				setMode(PLAYER_TWO_LOST);
			}
		}
	}
	
	
/*	//测试用
	private void setMessageBox(Context context,int str){
    	new AlertDialog.Builder(context) 
        .setMessage(str+"号玩家下棋") 
        .setPositiveButton("确定", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   
                                } 
                        }) 
        .show();
    }*/
	
	
	private void player2Run(MotionEvent event){
		Point point = newPoint(event.getX(), event.getY());
		if(allFreePoints.contains(point)){//此棋是否可下
			setOnProcessing();
			player2.run(player1.getMyPoints(),point);
			//刷新一下棋盘
			refressCanvas();
			snd.play(soundGo, 1, 1, 0, 0, 1);//播放音效
			//判断我是否赢了
			if(!player2.hasWin()){//我还没有赢
				setPlayer1Run();
			}else{
				//否则，提示游戏结束
				setMode(PLAYER_ONE_LOST);
			}
		}
	}
	
	
	private RefreshHandler refreshHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler {

		//这个方法主要在指定的时刻发一个消息
        public void computerRunAfter(long delayMillis) {
        	this.removeMessages(0);
        	//发消息触发handleMessage函数
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
        
        //收到消息
        @Override
        public void handleMessage(Message msg) {
        	//电脑走一步棋子
    		player2.run(player1.getMyPoints(),null);
    		//刷新一下
    		refressCanvas();
    		if(!player2.hasWin()){
    			//人下
    			setPlayer1Run();
    		}else{//第二个玩家赢了
    			setMode(PLAYER_ONE_LOST);
    		}
        }
    };
	
    
    //是否正在下某一步棋过程中，主是电脑下棋时需要较长的计算时间，这期间一定不可以再响应触摸事件
	private boolean onProcessing() {
		return whoRun == -1;
	}
	
	private void setOnProcessing(){
		whoRun = -1;
	}
	
	//默认第一个玩家先行
	private int whoRun = 1;
	
	private void setPlayer1Run(){
		whoRun = 1;
	}

	//是否轮到Player1下子
	private boolean isPlayer1Run(){
		return whoRun==1;
	}
	
	private void setPlayer2Run(){
		whoRun = 2;
	}
		
	//是否轮到Player2下子
	private boolean isPlayer2Run(){
		return whoRun==2;
	}
	

	private void refressCanvas(){
		//触发onDraw函数
        Chessboard.this.invalidate();//到此whoRun为-1
	}
	
	private void drawPlayer1Point(Canvas canvas){
		int size = player1.getMyPoints().size();
		if(size<0){
			return ;
		}
		for (int i = 0; i < size; i++) {
			drawPoint(canvas, player1.getMyPoints().get(i), BLACK);
		}
		
	}
	
	private void drawPlayer2Point(Canvas canvas){
		if(player2==null){
			return ;
		}
		int size = player2.getMyPoints().size();
		if(size<0){
			return ;
		}
		for (int i = 0; i < size; i++) {
			drawPoint(canvas, player2.getMyPoints().get(i), WHITE);
		}

	}
    
	
	//初始化好两种颜色的点
    public void fillPointArrays(int color,Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(pointSize, pointSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, pointSize, pointSize);
        drawable.draw(canvas);
        pointArray[color] = bitmap;
    }
    
    //doRun方法操作的是看不见的内存数据，此方法内容数据以图画的方式表现出来，所以画之前数据一定要先准备好
    @Override
    protected void onDraw(Canvas canvas) {
    	
    	createboard(canvas);
    	//画鼠标所在的点
    	drawPlayer1Point(canvas);
    	//画电脑下的棋子
    	drawPlayer2Point(canvas);

    	drawCtrl(canvas);
    	
    }


    //取得当前所有空白点，这些点才可以下棋
	@Override
	public List<Point> getFreePoints() {
		return allFreePoints;
	}
	
	//初始化空白点集合
	private void createPoints(){
		allFreePoints.clear();
		for (int i = 0; i < maxX; i++) {
			for (int j = 0; j < maxY-10; j++) {//减10 ，适应棋盘大小
				allFreePoints.add(new Point(i, j));
			}
		}
	}

	//取得棋盘最大横坐标
	@Override
	public int getMaxX() {
		return maxX;
	}

	//取得棋盘最大纵坐标
	@Override
	public int getMaxY() {
		return maxY;
	}

}
