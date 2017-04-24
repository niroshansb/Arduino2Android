package niroshan.jp.andro;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Button;
import android.os.Handler;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;


public class MainActivity extends Activity {

	
	TextView myLabel,humid,tempe;
	//EditText myTextbox;
	EditText comment;
	private TextView textView,textView1;
	private GpsTools gpsTools;

	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean stopWorker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button openButton = (Button) findViewById(R.id.open);
		Button sendBytton = (Button) findViewById(R.id.sendArd);
		Button closeButton= (Button) findViewById(R.id.close);
		myLabel = (TextView) findViewById(R.id.label);
		//myTextbox = (EditText) findViewById(R.id.myTextbox);
		//name=(TextView) findViewById(R.id.name);
		comment=(EditText) findViewById(R.id.comment);
		humid = (TextView) findViewById(R.id.labelHumid);
		tempe = (TextView) findViewById(R.id.labelTemp);
		//number=(TextView) findViewById(R.id.number);
		//lang =(TextView) findViewById(R.id.lang);
		textView = (TextView) this.findViewById(R.id.textView);
		textView1 = (TextView) this.findViewById(R.id.textView1);

		openButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					findBT();
					openBT();
				}
				catch (IOException ex){ }
			}
		});

		sendBytton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try {
					sendData();
				}
				catch (IOException ex) { }
			}

		});

		closeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					closeBT();
				}
				catch (IOException ex) { }
			}
		});





		if (gpsTools == null) {
			gpsTools = new GpsTools(this) {
				@Override
				public void onGpsLocationChanged(Location location) {
					super.onGpsLocationChanged(location);
					refreshLocation(location);
				}
			};
		}
		
		
	}

	void findBT()
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null)
		{
			myLabel.setText("Not available");

		}

		if(!mBluetoothAdapter.isEnabled())
		{
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth,0);
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0)
		{
			for(BluetoothDevice device : pairedDevices)
			{
				if(device.getName().equals("BmateOCU"))
				{
					mmDevice = device;
					break;
				}
			}
		}
		myLabel.setText("Bluetooth Device Found");

	}

	void openBT() throws IOException
	{
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
		mmSocket.connect();
		mmOutputStream = mmSocket.getOutputStream();
		mmInputStream = mmSocket.getInputStream();

		beginListenForData();

		myLabel.setText("Bluetooth Connected");

	}

	void beginListenForData()
	{
		final Handler handler = new Handler();
		final byte delimiter = 10;
		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable()
		{
			public void run()
			{
				while(!Thread.currentThread().isInterrupted() && !stopWorker)
				{
					try
					{
						int bytesAvailable = mmInputStream.available();
						if(bytesAvailable > 0)
						{
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for(int i=0;i<bytesAvailable;i++)
							{
								byte b = packetBytes[i];
								if(b == delimiter)
								{
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
									final String data = new String(encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									handler.post(new Runnable()
									{
										public void run()
										{
											//myLabel.setText(data);
											String[] str_array= data.split(":");
											String humid1 = str_array[0];
											String tempe1 = str_array[1];
											humid.setText(humid1);
											tempe.setText(tempe1);

										}

									});
								}
								else
								{
									readBuffer[readBufferPosition++] = b;
								}

							}

						}
					}
					catch (IOException ex)
					{
						stopWorker = true;
					}
				}
			}
		});
		workerThread.start();

	}

	void sendData() throws IOException
	{
		//String msg = myTextbox.getText().toString();
		String msg = "t";
		msg += "\n";
		mmOutputStream.write(msg.getBytes());
		myLabel.setText("Data Received");

	}
	void closeBT() throws IOException
	{
		stopWorker = true;
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		myLabel.setText("Bluetooth Closed");
		humid.setText(" ");
		tempe.setText(" ");
	}



	private void refreshLocation(Location location) {
		Double longitude = location.getLongitude();
		Double latitude = location.getLatitude();
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		sb.append(longitude);
		sb1.append(latitude);
		textView.setText(sb.toString());
		textView1.setText(sb1.toString());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	public void send(View v){
		
		 new Send().execute();
		comment.setText(" ");
		myLabel.setText("Data sent");
		humid.setText(" ");
		tempe.setText(" ");

	}
	
	
	
	class Send extends AsyncTask<String, Void,Long > {

	   

	    protected Long doInBackground(String... urls) {	
	
	//String Name=name.getText().toString();
	//String Email=email.getText().toString();
	//String Number=number.getText().toString();
	//String Lang=lang.getText().toString();
	String Long= textView.getText().toString();
	String Lat=textView1.getText().toString();
	String Humid=humid.getText().toString();
	String Tempe=tempe.getText().toString();
	String Comm=comment.getText().toString();




	HttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost("<your php url>");

	try {
	    // Add your data
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	   // nameValuePairs.add(new BasicNameValuePair("Name", Name));
	   // nameValuePairs.add(new BasicNameValuePair("Email", Email));
	   // nameValuePairs.add(new BasicNameValuePair("Trans", Number));
		//nameValuePairs.add(new BasicNameValuePair("Lang", Lang));
		nameValuePairs.add(new BasicNameValuePair("Humid",Humid));
		nameValuePairs.add(new BasicNameValuePair("Tempe",Tempe));
		nameValuePairs.add(new BasicNameValuePair("Long",Long));
		nameValuePairs.add(new BasicNameValuePair("Lat",Lat));
		nameValuePairs.add(new BasicNameValuePair("Comm",Comm));
	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    
	    
	    // Execute HTTP Post Request
	    HttpResponse response = httpclient.execute(httppost);


	} catch (Exception e) {
	    // TODO Auto-generated catch block

	}
	return null;
	   
}
	    protected void onProgressUpdate(Integer... progress) {
	       
	     }

	     protected void onPostExecute(Long result) {
	         
	     }
	 }

	@Override
	protected void onPause() {
		super.onPause();
		gpsTools.stopGpsUpdate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		gpsTools.startGpsUpdate();
	}
}
	 


