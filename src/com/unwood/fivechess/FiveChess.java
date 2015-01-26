package com.unwood.fivechess;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class FiveChess extends Activity {

	public static int FIGHTTINGMODE=0;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); //隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN); //设置全屏
        
    	setContentView(R.layout.item);
        
    }
    
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        MenuInflater inflater = getMenuInflater();  
        inflater.inflate(R.menu.options_menu, menu);      
        return true;
    }  
 
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
        case R.id.menu_start:  
        	FIGHTTINGMODE=1;
        	setContentView(R.layout.main);
          break;  
        case R.id.menu_dstart: 
        	FIGHTTINGMODE=2;
        	setContentView(R.layout.main);   	
            break;  
        case R.id.menu_int:
        	setContentView(R.layout.intmain);
            break;  
        case R.id.menu_exit:  
             System.exit(1);
            break;  
        }  
        return super.onOptionsItemSelected(item);  
    } 
    
}