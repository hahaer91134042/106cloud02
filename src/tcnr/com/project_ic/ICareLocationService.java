package tcnr.com.project_ic;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import static tcnr.com.project_ic.IC4000track.latGps;
import static tcnr.com.project_ic.IC4000track.lngGps;

public class ICareLocationService extends Service {
	String TAG="tcnr6==>";
//	double gpsLat=0 ,gpsLng=0;
	private  String loginAccount=null,action=null;
	
	Handler handler = new Handler();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		
		loginAccount= intent.getExtras().getString("account");//取出String action="showtime" 設定給action
		
		handler.postDelayed(sendSQL, 1000);
		return START_NOT_STICKY;
//		return super.onStartCommand(intent, flags, startId);
	}
	
	Runnable sendSQL =new Runnable() {
		
		@Override
		public void run() {
			String getLatLng=getNowLatLng();
			Log.d(TAG, "getLatLng->"+getLatLng);
			sendToSQL(getLatLng);
			handler.postDelayed(this, 20*1000);
			Toast.makeText(getBaseContext(),"你目前的位置:"+getLatLng+",已上傳" , Toast.LENGTH_SHORT).show();
		}

		private void sendToSQL(String getLatLng) {
			String sysDate=SQLiteWriter.getSystemTime();
			

			 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			  nameValuePairs.add(new BasicNameValuePair("query_string", "GPS"));
			  nameValuePairs.add(new BasicNameValuePair("account", loginAccount));
			  nameValuePairs.add(new BasicNameValuePair("date", sysDate));
			  nameValuePairs.add(new BasicNameValuePair("latlng", getLatLng));
;

			  String result = DBConnector.executeInsert("GPS", nameValuePairs);
			
		}
	}; 
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}



	@Override
	public void onDestroy() {
		
		handler.removeCallbacks(sendSQL);
		Toast.makeText(getBaseContext(),"已停止上傳..." , Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}




	private String getNowLatLng() {
		String latlng=latGps+","+lngGps;
		return latlng;
	}








}
