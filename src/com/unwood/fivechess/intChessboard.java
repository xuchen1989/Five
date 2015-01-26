package com.unwood.fivechess;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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



public class intChessboard extends View implements IChessboard{

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
    
    //��һ��ƫ�����ϽǴ�������Ϊ�����̾���
	private static int yOffset;
	private static int xOffset;
	
	//�������	
	private IPlayer player1 = new HumanPlayer();	//�䵱client�ˣ�����
	private IPlayer player2 = new HumanPlayer();
		
	// ����δ�µĿհ׵�
	private final List<Point> allFreePoints = new ArrayList<Point>();
	
	
	private Context context=null;
	
	//����
	private SoundPool snd;
	private int soundBegin;
	private int soundGo;
	private int soundOver;
	private int soundWin;

	
    public intChessboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setFocusable(true);
        
        //��������ɫ�ĵ�׼���ã�����������
        Resources r = this.getContext().getResources();
        fillPointArrays(WHITE,r.getDrawable(R.drawable.white));      
        fillPointArrays(BLACK,r.getDrawable(R.drawable.black));
        
        setMessageBox(context);
        
        initSound();
        
        connectToServer();//����server��
        
   }
   
    
    private void setMessageBox(Context context){
    	new AlertDialog.Builder(context) 
        .setMessage("�����ӣ���������") 
        .setPositiveButton("ȷ��", 
                       new DialogInterface.OnClickListener(){ 
                               public void onClick(DialogInterface dialoginterface, int i){ 
                            	   if (currentMode == READY) { 
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
        maxX = (int) Math.floor(w / pointSize)-1;       
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
    
    //����
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
      
    

    //��������״̬
	public void setMode(int newMode) {
		currentMode = newMode;
		if(currentMode==PLAYER_TWO_LOST){
			//��ʾ���2����
			setMessageBox(this.context,"��������");
			snd.play(soundWin, 1, 1, 0, 0, 1);
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currentMode = READY;
		}else if(currentMode==RUNNING){
			
		}else if(currentMode==READY){
			
		}else if(currentMode==PLAYER_ONE_LOST){
			//��ʾ���1����
			setMessageBox(this.context,"��������");
			snd.play(soundOver, 1, 1, 0, 0, 1);
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		createPoints();
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
		
		//��player2��server�ˣ�˼����ô��ʱ��������player1��client�ˣ��Ĵ����¼�
		if(onProcessing()){
			return true;
		}
		
		player1Run(event);
		
		return true;
	}
	
/*	private synchronized void playerRun(MotionEvent event){
		if(isPlayer1Run()){//��һ�������
			player1Run(event);
		}else if(isPlayer2Run()){//�ڶ��������
			player2Run();
		}
	}*/
	
	
	private void player1Run(MotionEvent event){
		Point point = newPoint(event.getX(), event.getY());	
		if(allFreePoints.contains(point)){//�����Ƿ����
			setOnProcessing();
			player1.run(player2.getMyPoints(),point);
			//ˢ��һ������
			refressCanvas();
			snd.play(soundGo, 1, 1, 0, 0, 1);//������Ч
			
			//��player1�µ��ӣ����ӣ������귢�ͳ�ȥ
			try {
				OutputStream os = socket.getOutputStream();  
				DataOutputStream out = new DataOutputStream(os);  
				out.writeInt(point.x*100+point.y);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//�жϵ�һ������Ƿ��Ѿ�����
			if(!player1.hasWin()){//�һ�û��Ӯ
				setPlayer2Run();
				refreshHandler.player2RunAfter(100);
			
			}else{
				//������ʾ��Ϸ����
				setMode(PLAYER_TWO_LOST);
			}
		}
	}
	
	
	
	private RefreshHandler refreshHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler {

		//���������Ҫ��ָ����ʱ�̷�һ����Ϣ
        public void player2RunAfter(long delayMillis) {
        	this.removeMessages(0);
        	//����Ϣ����handleMessage����
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
        
        //�յ���Ϣ
        @Override
        public void handleMessage(Message msg) {
        	int number=0,x,y;
    		//���նԷ����͹����ĶԷ��µ��ӵ�����ֵ
    		try { 
    			InputStream is = socket.getInputStream();  
                DataInputStream dis = new DataInputStream(is);  
                number = dis.readInt();  
    		} catch (IOException e) {		
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    				
    		x=(int)(number/100);
    		y=(int)(number%100);
    		Point point = new Point(x,y);
    		player2.run(player1.getMyPoints(),point);
    		
    		refressCanvas();
    		snd.play(soundGo, 1, 1, 0, 0, 1);
    		
    		if(!player2.hasWin()){
    			setPlayer1Run();
    		}else{
    			setMode(PLAYER_ONE_LOST);
    		}
        }
    };
	
    
    
/*	private void player2Run(){
		int number=0,x,y;
		//���նԷ����͹����ĶԷ��µ��ӵ�����ֵ
			try{ 
			InputStream is = socket.getInputStream();  
            DataInputStream dis = new DataInputStream(is);  
            number = dis.readInt();  
		} catch (IOException e) {		
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		x=(int)(number/100);
		y=(int)(number%100);
		Point point = new Point(x,y);
		
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
	}*/


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

	//�Ƿ��ֵ������������
	private boolean isPlayer1Run(){
		return whoRun==1;
	}
	
	private void setPlayer2Run(){
		whoRun = 2;
	}
	
	
	private boolean isPlayer2Run(){
		return whoRun==2;
	}
	

	
	private void refressCanvas(){
		//����onDraw����
        intChessboard.this.invalidate();
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
    	
    	drawPlayer1Point(canvas);
    	
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

	@Override
	public int getMaxX() {
		return maxX;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}
    

	
	public Socket socket=null;
	
	//����server��
	public void connectToServer(){
		try{
			socket = new Socket("10.0.2.2",8888);
//			OutputStream os = socket.getOutputStream();  
//			DataOutputStream out = new DataOutputStream(os);  
//			out.writeInt(213);  
//			out.flush();
    	}catch (Exception e){
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		}
	}
	
}
