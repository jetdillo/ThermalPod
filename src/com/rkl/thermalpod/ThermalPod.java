package com.rkl.thermalpod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Demo using an Analog Devices TMP36GZ temperature sensor. 
 * 
 */
public class ThermalPod extends AbstractIOIOActivity {
	private ToggleButton button_;
    private TextView tempValueTV;
    private TextView tempScaleTV;
    
	
	
	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		tempValueTV = (TextView) findViewById(R.id.tempValTxt);
		tempScaleTV = (TextView) findViewById(R.id.tempScaleTxt);
		setContentView(R.layout.main);
	    	
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		/** The on-board LED. */
		private DigitalOutput led_;
		private AnalogInput temp_input ;
		private Uart debug;
		private int sensor_pin = 46;
		private int roundtemp=0;
		private float v;
		private float proc_temp = (float) 0.00;
	    

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(0, true);
			temp_input = ioio_.openAnalogInput(sensor_pin);
			debug = ioio_.openUart(6,7,115200, Parity.NONE, StopBits.ONE);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		protected void loop() throws ConnectionLostException {
			InputStream debugIn = debug.getInputStream();
			OutputStream debugOut = debug.getOutputStream();
				
					try {
						v = temp_input.getVoltage();
					} catch (InterruptedException e) {
						temp_input.close();
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 proc_temp = ((v*1024) - 500)/10;
					 String proc_string = new String(Float.toString(proc_temp));
					    roundtemp = Math.round(proc_temp);
					    final String digits_str = new String(Integer.toString(roundtemp));
					    String v_string = new String(Float.toString(v));
					    runOnUiThread(
					            new Runnable() {
					                public void run() {
					                	tempValueTV = (TextView) findViewById(R.id.tempValTxt);
										 tempValueTV.setText(digits_str);
					                }
					            }
					        );
					    try {
							debugOut.write(new String("v=:"+v_string+"\r\n").getBytes());
							debugOut.write(new String("proc_string="+proc_string+"\r\n").getBytes());
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
				
						// tempValueTV = (TextView) findViewById(R.id.tempValTxt);
					    // tempValueTV.setText(digits_str);
		 
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}
}