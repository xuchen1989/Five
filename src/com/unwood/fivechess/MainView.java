package com.unwood.fivechess;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;


public class MainView extends View{//ª∂”≠ΩÁ√Ê

	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
		drawBG(canvas);	
    }
	
	private void drawBG(Canvas canvas)
	{
		Resources res = this.getContext().getResources();
    	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.gologo);  
        canvas.drawColor(Color.WHITE);  
        canvas.drawBitmap(bmp, 0, 0, null);
	}
	
}
