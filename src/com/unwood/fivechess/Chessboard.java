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


	//��Ϸ״̬������
    //��׼���ã��ɿ���
    private static final int READY = 1;
    //�ѿ���
    private static final int RUNNING = 2;
    //�ѽ���
    private static final int PLAYER_TWO_LOST = 3;
    private static final int PLAYER_ONE_LOST = 4;
    
    //��ǰ״̬��Ĭ��Ϊ�ɿ���״̬
    private int currentMode = READY;
    
	//���ʶ���
	private final Paint paint = new Paint();
	
	//�������
	private static final int WHITE = 0;
	
	//����
	private static final int BLACK = 1;
	
	//���С
    private static int pointSize = 16;
		
	private static int boardHeight;
	private static int boardWidth;
	
	//��ͬ��ɫ��Bitmap����
	private Bitmap[] pointArray = new Bitmap[2];
	
	//��Ļ���½ǵ�����ֵ�����������ֵ
    private static int maxX;
    private static int maxY;
    
    //��һ��ƫ�����Ͻ����أ�Ϊ�����̾���
	private static int yOffset;
	private static int xOffset;
	
	//�������
	//��һ�����Ĭ��Ϊ�������
	private IPlayer player1 = new HumanPlayer();
	//�ڶ��������ѡ���˻�ս���Ƕ�սģʽ����ʼ��
	private IPlayer player2;
	//Ԥ�ȳ�ʼ�����ڶ����
	//�������
	private static final IPlayer computer = new BaseComputerAi();
	//�������
	private static final IPlayer human = new HumanPlayer();
	
	// ����δ�µĿհ׵�
	private final List<Point> allFreePoints = new ArrayList<Point>();
	
	
	private Context context=null;
	
	//����
	private SoundPool snd;
	private int soundBegin;
	private int soundGo;
	private int soundOver;
	private int soundWin;

	
    public Chessboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setFocusable(true);
        
        //��������ɫ�ĵ�׼���ã�����������
        Resources r = this.getContext().getResources();
        fillPointArrays(WHITE,r.getDrawable(R.drawable.white));      
        fillPointArrays(BLACK,r.getDrawable(R.drawable.black));
              
        setMessageBox(context);
        
        initSound();
        
   }
    
    private void setMessageBox(Context context){
    	new AlertDialog.Builder(context) 
        .setMessage("��������") 
        .setPositiveButton("ȷ��", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   if (currentMode == READY) {                                   	
                            		   if(fightingMode()==1){//�˻���ս
                            			   player2 = computer;
                                   	}else if(fightingMode()==2){//���˶�ս
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
        .setMessage(str+",ѡ��˵����¿�ʼ") 
        .setPositiveButton("ȷ��", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   
                                } 
                        }) 
        .show();
    }
    
    
    //��ʼ���ߺ����ߵ���Ŀ
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        maxX = (int) Math.floor(w / pointSize)-1;    //�������½����ӵĺ�������   
        maxY = (int) Math.floor((h) / pointSize)-1;

        //����X��Y����΢��ֵ��Ŀ����ʹ���������
        xOffset = ((w - (pointSize * maxX)) / 2);
        yOffset = ((h - (pointSize * maxY)) / 2);
        
    }
    

   
       
    //������
    public void createboard(Canvas canvas)
    {
    	Resources res = this.getContext().getResources();
    	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.go);  
        canvas.drawColor(Color.WHITE);  
        canvas.drawBitmap(bmp, 0, 0, null);
        boardHeight=bmp.getHeight();
        boardWidth=bmp.getWidth();
         
    }
    
    //�û�õ����껭��
    private void drawPoint(Canvas canvas,Point p,int color){
    	canvas.drawBitmap(pointArray[color],p.x*pointSize+xOffset,p.y*pointSize+yOffset,paint);
    	
    }
        
    //���������
    private void drawCtrl(Canvas canvas){
    	
    	Resources res = this.getContext().getResources();
    	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.goctrll);
    	Bitmap bmpfig = BitmapFactory.decodeResource(res, R.drawable.figrue);
    	   	
        canvas.drawBitmap(bmp, 0, boardHeight, null);
        
        Bitmap bmpman = BitmapFactory.decodeResource(res, R.drawable.man1);
    	canvas.drawBitmap(bmpman, 32, boardHeight+48, null);
    	
        if(isPlayer1Run()){//����ǵ�һ�������
        	canvas.drawBitmap(bmpfig, boardWidth-64, boardHeight+48, null);//������ָͼ��
        	//snd.play(soundGo, 1, 1, 0, 0, 1);//������Ч
		}else if(isPlayer2Run()){//����ǵڶ��������
			canvas.drawBitmap(bmpfig, boardWidth-144, boardHeight+48, null);//������ָͼ��
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
    

    //��������״̬
	public void setMode(int newMode) {
		currentMode = newMode;
		if(currentMode==PLAYER_TWO_LOST){
			//��ʾ���2����
			setMessageBox(this.context,"��������");
			snd.play(soundWin, 1, 1, 0, 0, 1);
			currentMode = READY;
		}else if(currentMode==RUNNING){
		//	textView.setText(null);
		}else if(currentMode==READY){
		//	textView.setText(R.string.mode_ready);
		}else if(currentMode==PLAYER_ONE_LOST){
			//��ʾ���1����
			setMessageBox(this.context,"��������");
			snd.play(soundOver, 1, 1, 0, 0, 1);
			currentMode = READY;
		}
	}
	

	//���������¼�
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
		
		if((currentMode==PLAYER_TWO_LOST||currentMode==PLAYER_ONE_LOST) && keyCode == KeyEvent.KEYCODE_DPAD_CENTER){//���¿�ʼ
        	restart();
        	setMode(READY);
        }else{
        	return false;
        }
        return true;
	}
	
	//���ݴ����������ҵ���Ӧ��
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
	
	//���¿�ʼ
	private void restart() {
		createPoints();//��ʼʱȫ���հ׵�
		player1.setChessboard(this);
		player2.setChessboard(this);
		setPlayer1Run();
		//ˢ��һ��
		refressCanvas();
	}
	
	//�Ƿ��ѿ���
	private boolean hasStart(){
		return currentMode==RUNNING;
	}

	//�������¼�
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		//��û�п��֣������ǰ����¼���������ֻ�����ֺ�Ĵ��������¼�
		if(!hasStart() || event.getAction()!=MotionEvent.ACTION_UP){
			return true;
		}
		
		//�Ƿ����ڴ���һ����Ĺ�����
		if(onProcessing()){
			return true;
		}
		
		playerRun(event);
		
		return true;
	}
	
	private synchronized void playerRun(MotionEvent event){
		if(isPlayer1Run()){//��һ�������
			player1Run(event);
		}else if(isPlayer2Run()){//�ڶ��������
			player2Run(event);
		}
	}
	
	
	private void player1Run(MotionEvent event){
		Point point = newPoint(event.getX(), event.getY());
		if(allFreePoints.contains(point)){//�����Ƿ����
			setOnProcessing();
			player1.run(player2.getMyPoints(),point);
			//ˢ��һ������
			refressCanvas();//�˴�ˢ����ָ��λ�ã������9�б�ǡ�~~��setPlayer2Run()��whoRun����Ϊ2��ʹ����ָָ��player2�������ӣ������ɣ�Ϊʲô��������
			snd.play(soundGo, 1, 1, 0, 0, 1);//������Ч
			//�жϵ�һ������Ƿ��Ѿ�����
			if(!player1.hasWin()){//�һ�û��Ӯ
				if(player2==computer){//����ڶ�����ǵ���
					//100�����Ÿ����2����
					setPlayer2Run();
					refreshHandler.computerRunAfter(100);
				}else{
					setPlayer2Run();//~~
				}
			}else{
				//������ʾ��Ϸ����
				setMode(PLAYER_TWO_LOST);
			}
		}
	}
	
	
/*	//������
	private void setMessageBox(Context context,int str){
    	new AlertDialog.Builder(context) 
        .setMessage(str+"���������") 
        .setPositiveButton("ȷ��", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   
                                } 
                        }) 
        .show();
    }*/
	
	
	private void player2Run(MotionEvent event){
		Point point = newPoint(event.getX(), event.getY());
		if(allFreePoints.contains(point)){//�����Ƿ����
			setOnProcessing();
			player2.run(player1.getMyPoints(),point);
			//ˢ��һ������
			refressCanvas();
			snd.play(soundGo, 1, 1, 0, 0, 1);//������Ч
			//�ж����Ƿ�Ӯ��
			if(!player2.hasWin()){//�һ�û��Ӯ
				setPlayer1Run();
			}else{
				//������ʾ��Ϸ����
				setMode(PLAYER_ONE_LOST);
			}
		}
	}
	
	
	private RefreshHandler refreshHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler {

		//���������Ҫ��ָ����ʱ�̷�һ����Ϣ
        public void computerRunAfter(long delayMillis) {
        	this.removeMessages(0);
        	//����Ϣ����handleMessage����
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
        
        //�յ���Ϣ
        @Override
        public void handleMessage(Message msg) {
        	//������һ������
    		player2.run(player1.getMyPoints(),null);
    		//ˢ��һ��
    		refressCanvas();
    		if(!player2.hasWin()){
    			//����
    			setPlayer1Run();
    		}else{//�ڶ������Ӯ��
    			setMode(PLAYER_ONE_LOST);
    		}
        }
    };
	
    
    //�Ƿ�������ĳһ��������У����ǵ�������ʱ��Ҫ�ϳ��ļ���ʱ�䣬���ڼ�һ������������Ӧ�����¼�
	private boolean onProcessing() {
		return whoRun == -1;
	}
	
	private void setOnProcessing(){
		whoRun = -1;
	}
	
	//Ĭ�ϵ�һ���������
	private int whoRun = 1;
	
	private void setPlayer1Run(){
		whoRun = 1;
	}

	//�Ƿ��ֵ�Player1����
	private boolean isPlayer1Run(){
		return whoRun==1;
	}
	
	private void setPlayer2Run(){
		whoRun = 2;
	}
		
	//�Ƿ��ֵ�Player2����
	private boolean isPlayer2Run(){
		return whoRun==2;
	}
	

	private void refressCanvas(){
		//����onDraw����
        Chessboard.this.invalidate();//����whoRunΪ-1
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
    
	
	//��ʼ����������ɫ�ĵ�
    public void fillPointArrays(int color,Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(pointSize, pointSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, pointSize, pointSize);
        drawable.draw(canvas);
        pointArray[color] = bitmap;
    }
    
    //doRun�����������ǿ��������ڴ����ݣ��˷�������������ͼ���ķ�ʽ���ֳ��������Ի�֮ǰ����һ��Ҫ��׼����
    @Override
    protected void onDraw(Canvas canvas) {
    	
    	createboard(canvas);
    	//��������ڵĵ�
    	drawPlayer1Point(canvas);
    	//�������µ�����
    	drawPlayer2Point(canvas);

    	drawCtrl(canvas);
    	
    }


    //ȡ�õ�ǰ���пհ׵㣬��Щ��ſ�������
	@Override
	public List<Point> getFreePoints() {
		return allFreePoints;
	}
	
	//��ʼ���հ׵㼯��
	private void createPoints(){
		allFreePoints.clear();
		for (int i = 0; i < maxX; i++) {
			for (int j = 0; j < maxY-10; j++) {//��10 ����Ӧ���̴�С
				allFreePoints.add(new Point(i, j));
			}
		}
	}

	//ȡ��������������
	@Override
	public int getMaxX() {
		return maxX;
	}

	//ȡ���������������
	@Override
	public int getMaxY() {
		return maxY;
	}

}
