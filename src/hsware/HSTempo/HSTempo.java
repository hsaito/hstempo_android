/*
*   HSTempo - the Tempo measuring application for Android platform.
    Copyright (C) 2009 Hideki Saito
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * HSTempo Main Code
 * @since 1.0.0
 * @author Hideki Saito
 */

package hsware.HSTempo;



import android.app.Activity;
//import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Main class for HSTempo
 * @author Hideki Saito
 * @version 1.2.0.1
 * @since 1.0.0
 */
public class HSTempo extends Activity {
	private int beatcount = 0;
	private int bpmvalue = 0;
	private int [] bpmhistory10;
	private int bpmhistory10_pt;
	private int [] bpmhistory15;
	private int bpmhistory15_pt;
	private int [] bpmhistory20;
	private int bpmhistory20_pt;
	private Handler mHandler = new Handler();
	private long prev;
	private int stability;
	
    //static final private int AUDIO_ID = Menu.FIRST;
    static final private int RESET_ID = Menu.FIRST + 1;
    static final private int QUIT_ID = Menu.FIRST + 2;
    static final private int ABOUT_ID = Menu.FIRST + 3;
    //static final private int MARACAS_ID = Menu.FIRST + 4;

	
    /** Called when the activity is first created. 
     * Initializes all necessary values here.
     * @author Hideki Saito
     * @version 1.0.5
     * @since 1.0.0
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        // Listen for Beat Button Clicks
        // Currently it listens for click event, which makes this a bit inaccurate.
        // Fix is underway.
        Button beatbutton = (Button)findViewById(R.id.BeatButton);
        beatbutton.setOnTouchListener(onBeatPress);
        
        // Listen for Reset Button Clicks
        Button resetbutton = (Button)findViewById(R.id.ResetButton);
        resetbutton.setOnClickListener(onResetPress);
        resetbutton.setEnabled(false);
        
    	EditText beatcountbox = (EditText) findViewById(R.id.BeatCountDisplay);
    	beatcountbox.setText(String.valueOf(beatcount));
    	
    	EditText bpmcountbox = (EditText) findViewById(R.id.BeatMonitorDisplay);
    	bpmcountbox.setText(String.valueOf(bpmvalue));
    	
    	resetAll();
    }
        
    
    boolean session_active = false;
    boolean accept_beat = true;
    long starttime = 0;
    
    
    /**
     * Function to handle button press Event
     * @author Hideki Saito
     * @version 1.2.0.1
     * @since 1.2.0.1
     */
    

    private boolean pressEvent(View v)
    {
		// To send a result, simply call setResult() before your
		// activity is finished, building an Intent with the data
		// you wish to send.
		if(session_active == false)
		{	
			Button resetbutton = (Button)findViewById(R.id.ResetButton);
			resetbutton.setEnabled(true);
			starttime = System.currentTimeMillis();
			prev = starttime;
			//        		atbeat = System.currentTimeMillis();
			Log.i("Calc","Starttime is "+String.valueOf(starttime));
			session_active = true;
			beatcount++;
			bpmvalue = 0;
			mHandler.removeCallbacks(autoUpdateDisp);
			mHandler.postDelayed(autoUpdateDisp, 100);
		}
		else
		{
			double bpmvalue_double;
			long atbeat = System.currentTimeMillis();
			bpmvalue_double = (double)((double)beatcount / (double)((atbeat - starttime))*1000*60);
			Log.i("Calc","Beatcount is "+String.valueOf(bpmvalue_double));
			Log.i("Calc","Timeoffset is "+String.valueOf(atbeat - starttime));
			Log.i("Calc","BPM value is "+String.valueOf(bpmvalue));
			EditText interval = (EditText) findViewById(R.id.Interval);
			interval.setText(String.valueOf(atbeat - prev));
			prev = atbeat;
			bpmvalue = (int) Math.round(bpmvalue_double);
			beatcount++;
		}
		ImageView light = (ImageView) findViewById(R.id.VBILamp);
		light.setImageResource(R.drawable.lamp_white);
		mHandler.postDelayed(VBItimeout, 20);
		// Finally, update the display.
		UpdateDisplay();
		return true;
};
 
    
    /**
     * Listen for beat button
     * @author Hideki Saito
     * @version 1.2.0.1
     * @since 1.0.0
     */
    private OnTouchListener onBeatPress = new OnTouchListener()
    {
    	public boolean onTouch(View v, MotionEvent m)
    	{
    		if(m.getAction() == MotionEvent.ACTION_DOWN)
    		{
    			pressEvent(v);
    			return true;
    		}
    		return false;
    	}
    };
    
    private Runnable autoUpdateDisp = new Runnable() { 
        public void run() {
        	EditText elapsed = (EditText) findViewById(R.id.ElapsedBox);
        	elapsed.setText((String.valueOf((int)(System.currentTimeMillis() - starttime)/1000)));
//        	EditText fromlast = (EditText) findViewById(R.id.LastBeatBox);
//        	fromlast.setText((String.valueOf((int)(System.currentTimeMillis() - atbeat)/1000)));
        	Log.i("Calc","Elapsed millsec "+String.valueOf(System.currentTimeMillis() - starttime));
        	mHandler.removeCallbacks(autoUpdateDisp);
        	mHandler.postDelayed(autoUpdateDisp, 100); 
        }
     };
    
   private Runnable VBIupdate = new Runnable()
   {
	   public void run() {
		   ImageView light = (ImageView) findViewById(R.id.VBILamp);
		   if(bpmvalue != 0)
		   {
			   
			   if(stability < 15)
			   {
				   light.setImageResource(R.drawable.lamp_green);
			   }
			   else
			   {
				   light.setImageResource(R.drawable.lamp_red);
			   }
		   
			   long msfrombeat = (long)(((double)60/(double)bpmvalue)*1000);
			   Log.i("VBI","Beat is "+msfrombeat);
			   mHandler.removeCallbacks(VBItimeout);
			   mHandler.removeCallbacks(VBIupdate); 
			   mHandler.postDelayed(VBIupdate, msfrombeat);
			   mHandler.postDelayed(VBItimeout, msfrombeat/10);
		   }
		   else
		   {
			   light.setImageResource(R.drawable.lamp_red);
		   }
	   }
   };
     
   private Runnable VBItimeout = new Runnable()
   {
	 public void run()
	 {
		 mHandler.removeCallbacks(VBItimeout);
		 ImageView light = (ImageView) findViewById(R.id.VBILamp);
		 light.setImageResource(R.drawable.lamp_off);
         mHandler.removeCallbacks(VBItimeout);
	 }
   };
   
     /**
      * Updates display
      * @author Hideki Saito
      * @version 1.0.5
      * @since 1.0.0
      */
    protected void UpdateDisplay() {
    	EditText beatcountbox = (EditText) findViewById(R.id.BeatCountDisplay);
    	beatcountbox.setText(String.valueOf(beatcount));
    	EditText bpmcountbox = (EditText) findViewById(R.id.BeatMonitorDisplay);
    	bpmcountbox.setText(String.valueOf(bpmvalue));
    	
    	// Fill in the history
    	bpmhistory10[bpmhistory10_pt] = bpmvalue;
    	bpmhistory10_pt++;
    	if(bpmhistory10_pt > 9)
    		bpmhistory10_pt = 0;

    	bpmhistory15[bpmhistory15_pt] = bpmvalue;
    	bpmhistory15_pt++;
    	if(bpmhistory15_pt > 14)
    		bpmhistory15_pt = 0;

    	bpmhistory20[bpmhistory20_pt] = bpmvalue;
    	bpmhistory20_pt++;
    	if(bpmhistory20_pt > 19)
    		bpmhistory20_pt = 0;
    	

    	int bpmtemp10 = 0;
    	int bpmtemp15 = 0;
    	int bpmtemp20 = 0;
    	
    	EditText bpm10 = (EditText) findViewById(R.id.Avg10);
    	if(beatcount > 10)
    	{
    		int temp = 0;
    		for(int i = 0; i < 10; i++)
    			temp += bpmhistory10[i];
    		bpm10.setText(String.valueOf(temp/10));
    		bpmtemp10 = temp/10;
    	}
    	else
    		bpm10.setText("X");
    	EditText bpm15 = (EditText) findViewById(R.id.Avg15);
 
    	if(beatcount > 15)
    	{
    		int temp = 0;
    		for(int i = 0; i < 15; i++)
    			temp += bpmhistory15[i];
    		bpm15.setText(String.valueOf(temp/15));
    		bpmtemp15 = temp/15;
    	}
    	else
    		bpm15.setText("X");
    	
    	EditText bpm20 = (EditText) findViewById(R.id.Avg20);
    	if(beatcount > 20)
    	{
    		int temp = 0;
    		for(int i = 0; i < 20; i++)
    			temp += bpmhistory20[i];
    		bpm20.setText(String.valueOf(temp/20));
    		bpmtemp20 = temp/20;
    	}
    	else
    		bpm20.setText("X");
    	
    	stability = Math.abs(bpmvalue-(bpmtemp10+bpmtemp15+bpmtemp20)/3);
    	ProgressBar StabilityBar = (ProgressBar) findViewById(R.id.ProgressBar01);
    	StabilityBar.setProgress(20-stability);
    	
       	mHandler.removeCallbacks(VBIupdate);
    	mHandler.postDelayed(VBIupdate, (long)(((double)bpmvalue/(double)60)*1000)); 
	};

	/**
	 * Listens for reset button.
	 * @author Hideki Saito
	 * @version 1.0.5
	 * @since 1.0.0
	 */
	private OnClickListener onResetPress = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		resetAll();
    	}
    };
    
    int start = 0;

    /**
     * Resets all the value
     * @author Hideki Saito
     * @version 1.0.5
     * @since 1.0.0
     */
    void resetAll()
    {
		session_active = false;
		mHandler.removeCallbacks(autoUpdateDisp);
		beatcount = 0;
    	bpmvalue = 0;
    	starttime = 0;
    	
    	bpmhistory10 = new int[10];
    	for(int i = 0; i < 10; i++)
    		bpmhistory10[i] = 0;
    	bpmhistory15 = new int[15];
    	for(int i = 0; i < 15; i++)
    		bpmhistory15[i] = 0; 
    	bpmhistory20 = new int[20];
    	for(int i = 0; i < 20; i++)
    		bpmhistory20[i] = 0;
    	bpmhistory10_pt = 0;
    	bpmhistory15_pt = 0;
    	bpmhistory20_pt = 0;

//    	EditText fromlast = (EditText) findViewById(R.id.LastBeatBox);
//    	fromlast.setText("0");
    	EditText bpm10 = (EditText) findViewById(R.id.Avg10);
    	EditText bpm15 = (EditText) findViewById(R.id.Avg15);
    	EditText bpm20 = (EditText) findViewById(R.id.Avg20);
    	bpm10.setText("X");
    	bpm15.setText("X");
    	bpm20.setText("X");

    	ProgressBar StabilityBar = (ProgressBar) findViewById(R.id.ProgressBar01);
    	StabilityBar.setProgress(0);
    	
    	
        Button resetbutton = (Button)findViewById(R.id.ResetButton);
        resetbutton.setEnabled(false);

    	EditText elapsed = (EditText) findViewById(R.id.ElapsedBox);
    	elapsed.setText("0");

		EditText interval = (EditText) findViewById(R.id.Interval);
		interval.setText("0");
    	
    	//    	EditText fromlast = (EditText) findViewById(R.id.LastBeatBox);
//    	fromlast.setText("0");
        
    	for(int i = 0; i < 10; i++)
    		bpmhistory10[i] = 0;
    	for(int i = 0; i < 15; i++)
    		bpmhistory15[i] = 0; 
    	for(int i = 0; i < 20; i++)
    		bpmhistory20[i] = 0;
    	bpmhistory10_pt = 0;
    	bpmhistory15_pt = 0;
    	bpmhistory20_pt = 0;
    	mHandler.removeCallbacks(VBIupdate);
    	mHandler.removeCallbacks(VBItimeout);
    	ImageView light = (ImageView) findViewById(R.id.VBILamp);
    	light.setImageResource(R.drawable.lamp_red);
    	accept_beat = true;
    	UpdateDisplay();
    }
    
    
    /**
     * Specify contents for popup menu.
     * @author Hideki Saito
     * @version 1.0.5
     * @since 1.0.3
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        //menu.add(0, AUDIO_ID, 0, R.string.Audio).setShortcut('0', 'b');
        menu.add(0, RESET_ID, 0, R.string.Reset).setShortcut('1', 'c');
        menu.add(0, QUIT_ID, 0, R.string.Quit).setShortcut('2', 'q');
        menu.add(0, ABOUT_ID, 0, R.string.About).setShortcut('3', 'v');
        //menu.add(0, MARACAS_ID, 0, R.string.Maracas).setShortcut('4','m');

        return true;
    }

    /**
     * Called right before your activity's option menu is displayed.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Before showing the menu, we need to decide whether the clear
        // item is enabled depending on whether there is text to clear.
        //menu.findItem(CLEAR_ID).setVisible(mEditor.getText().length() > 0);

        return true;
    }

    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case ABOUT_ID:
            Dialog d = new Dialog(this);
            d.setContentView(R.layout.about);
            d.show();
            return true;
        case RESET_ID:
    		resetAll();
            return true;
        case QUIT_ID:
        	finish();
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onStop(){
    	// Killing timer (they may eat up your battery)
    	mHandler.removeCallbacks(autoUpdateDisp);
    	mHandler.removeCallbacks(VBIupdate);
    	mHandler.removeCallbacks(VBItimeout);
    	super.onStop();
    }
}