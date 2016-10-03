package tcnr.com.project_ic;

import static tcnr.com.project_ic.CommonUtilities.API_Key;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.navdrawer.SimpleSideDrawer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;


import android.location.Location;


import android.os.Bundle;
import android.os.Handler;

import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;

/*******************************
*1.按下我的位置->定時取得onChangeListener裡面的經緯度->再透過傳值給javascript function來顯示
*2.按下尾行目標->先檢查是否有選擇成員帳號
*                   ->否->開啟側邊攔
*                   ->是->定時從mySQL下載目標最近一筆的經緯度->傳值給javascript function來顯示
*
*
********************************/

@SuppressLint("SetJavaScriptEnabled")
public class IC4000track extends Activity 
                         implements android.view.View.OnClickListener,
                                    IGPSActivity{

	private String loginAccount;
	private Spinner icSpin4111track;
	private Button icBtn4111track,icBtn4112track,icBtn4113track,icBtn4114track,icBtn4115track;
//<!--------------側邊欄使用物件------------------>
	private SimpleSideDrawer mysidebar01;

	private float x, y, x1, x2, y1, y2;
	int range, angle, angleThreshold = 60;
	private ListView icLisVie4240;
	 List<Map<String, Object>> mList;
	 private String[] getSQLaccount,reg_id ;
	 private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};
	private String[] MYLOGINSTATUSCOLUMN = new String[]{"id", "loginDate","account","code", "loginStatus" };

//<!--------------------------------------------->
//<!------------------google地圖使用變數--------------------------------->
	public static double latGps //紀錄精度根緯度使用
			            , lngGps ;
	private static final String MAP_URL = "file:///android_asset/googleMap.html";//map使用位置
	private WebView icWebVie4110track;//webview物件
	private boolean webviewReady = false;//檢查webview是否準備好
//	private Location mostRecentLocation = null;
	
	private GPS gps;//將GPS class 在本類別使用
	
	int btnsend=0,btnTrack=0,btnMyposi=0,btnTarget=0,btnNavigation=0,spinLocus=0;
	private String getSelectAccount=null,getSelectReg_id=null,targetLatLng=null;
	Handler runPosition=new Handler();
	Handler runLocusData=new Handler();
	JSONArray locusJSONArray=new JSONArray();
//<!-------------------------------------------------->
	String TAG="tcnr6==>",tag=IC4000track.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ic4000track);
		mysidebar01 = new SimpleSideDrawer(this);//將SimpleSideDrawer繼承到這個class使用
		gps=new GPS(this);//將GPS繼承到這個class使用
		mysidebar01.setLeftBehindContentView(R.layout.ic4000leftside);//設定側邊來使用layout
//<!----------------設定手指移動距離險是側邊欄-------------------------->
		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		range = displayMetrics.widthPixels * 1 / 4;//設定手指滑動距離為1/4銀幕寬
//<!--------------------onTouch監聽---------------------------------->
		findViewById(R.id.mainlay).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getX();
				y = event.getY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					x1 = event.getX();
					y1 = event.getY();
					return true;
				case MotionEvent.ACTION_MOVE:

					return true;
				case MotionEvent.ACTION_UP:
					x2 = x;
					y2 = y;
					float xbar = Math.abs(x2 - x1);// x
					float ybar = Math.abs(y2 - y1);// y
					Double z = Math.sqrt(xbar * xbar + ybar * ybar);// 開根號 得出斜邊長
					angle = Math.round((float) (Math.asin(ybar / z) / Math.PI * 180));

					if (x1 != 0 && y1 != 0) {
						if (x1 - x2 > range) {// 向左滑動
						}
						if (x2 - x1 > range) { // 向右滑動
							mysidebar01.toggleLeftDrawer();
						}
						if (y2 - y1 > range && angle > angleThreshold) { // 向下滑動
						}
						if (y1 - y2 > range && angle > angleThreshold) { // 向上滑動
						}
					}
				}
				return true;
			} // layout設定監聽
		});
//<!------------------------------------------------------------------->
		u_setupWebView();//自定義method設定使用webview		
		setupviewcomponent();
		setupleftsideLisVie();
		sqLiteLoginAccount();
			if (webviewReady)
				icWebVie4110track.loadUrl(MAP_URL);
        
	}

//-------檢查SQLite裡面有沒有記錄登入資料 沒有等於沒登入--------
	private void sqLiteLoginAccount() {
		mContRes = getContentResolver();
		String[][] getSQLiteLoginStatusData=SQLiteWriter
				  .getSQLiteData(mContRes, DBContentProvider.CONTENT_URI_loginStatus, MYLOGINSTATUSCOLUMN);
		if(getSQLiteLoginStatusData!=null)
		{
			loginAccount=getSQLiteLoginStatusData[0][2];
		}else if(getSQLiteLoginStatusData==null)
		{
			icSpin4111track.setVisibility(View.INVISIBLE);
			icBtn4111track.setVisibility(View.INVISIBLE);
			icBtn4112track.setVisibility(View.INVISIBLE);
			icBtn4113track.setVisibility(View.INVISIBLE);
			icBtn4114track.setVisibility(View.INVISIBLE);
			icBtn4115track.setVisibility(View.INVISIBLE);
			icLisVie4240.setVisibility(View.INVISIBLE);
			Toast.makeText(IC4000track.this, "請先登入才能完整使用本功能...", Toast.LENGTH_LONG).show();
		}		
	}

   //----------------設定左邊側邊欄的成員名單--------------
	private void setupleftsideLisVie() {
		getSQLaccount=null;
		// -------------------------
		mContRes = getContentResolver();
		//-----------------------------	
		icLisVie4240 = (ListView) findViewById(R.id.icLisVie4240);
        String [][] getSQLiteLoginData=SQLiteWriter.getSQLiteData(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN);
		if (getSQLiteLoginData!=null) {
	  		mList = new ArrayList<Map<String, Object>>();
	  		getSQLaccount = new String[getSQLiteLoginData.length];
	  		reg_id=new String[getSQLiteLoginData.length];
			// ---
			
			for (int i = 0; i < getSQLiteLoginData.length; i++) {
				
				Map<String, Object> item = new HashMap<String, Object>();
				
				getSQLaccount[i] = getSQLiteLoginData[i][1];
				reg_id[i]=getSQLiteLoginData[i][5];
				
	  			item.put("imgView", R.drawable.member1);
	     	    item.put("txtView", getSQLiteLoginData[i][1] );  //需拆字				
	     	   mList.add(item);
	     	   

			}
			SimpleAdapter adapter = new SimpleAdapter(this, mList,
	  				R.layout.ic4000list_view_style, new String[] { "imgView", "txtView" },
	  				new int[] { R.id.imgView, R.id.txtView });
			
	  		icLisVie4240.setAdapter(adapter);
	  		icLisVie4240.setTextFilterEnabled(true);
	  		icLisVie4240.setOnItemClickListener(listviewOnItemClkLis);
		}	
	}
	AdapterView.OnItemClickListener listviewOnItemClkLis = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			getSelectReg_id=null;
			getSelectAccount=null;
			targetLatLng=null;
			
			getSelectReg_id=reg_id[position];
			getSelectAccount=getSQLaccount[position];
			Log.d(TAG, "getSelectReg_id==>"+getSelectReg_id);
//			String[][] getPosition= SQL_TargetTrackLocus(getSelectAccount);
			String[] getPosition= getSQL_TargetPosition(getSelectAccount);
			
			Log.d(TAG, "getPosition==>"+getPosition);
			if(getPosition!=null){
				targetLatLng=getPosition[0]+","+getPosition[1];
				
				Log.d(TAG, "targetLatLng==>"+targetLatLng);
				icWebVie4110track.loadUrl(MAP_URL);
				
			}else {
				Toast.makeText(IC4000track.this,"查無目標位置...", Toast.LENGTH_SHORT).show();
			}			
		}		
	};


//------------------------設定webview-----------------------
	private void u_setupWebView() {
		
		icWebVie4110track = (WebView) findViewById(R.id.icWebVie4110track);
		// <!------------------設定webview---------------------------------->
		icWebVie4110track.getSettings().setJavaScriptEnabled(true);//開啟webview使用javascript功能
		icWebVie4110track.addJavascriptInterface(IC4000track.this, "AndroidFunction");//設定一個javascript finction AndroidFunction()給html檔呼叫 
		icWebVie4110track.setWebViewClient(new WebViewClient() {//設定webview使用者
			@Override
			public void onPageFinished(WebView view, String url) {
				// webView.loadUrl(centerURL);
				webviewReady = true;// webview已經載入完畢
			}
		});
		icWebVie4110track.loadUrl(MAP_URL);//onCreate()時webview載入URL路徑 這裡是使用assets資料夾裡面的html檔
	}
//<!------- javascript 呼叫function AndroidFunction()------------------------->	
	@JavascriptInterface
	public double getLat() { // 上面記得要打JavascriptInterface
		return latGps;
	}

	@JavascriptInterface
	public double getLng() {
		return lngGps;
	}

	@JavascriptInterface
	public String Getstart() {
		return getNowLatLng();
	}

	@JavascriptInterface
	public String Getend() {
		return  targetLatLng;
	}

//	 @JavascriptInterface
//	 public JSONArray getJsonArray(){
//		 Log.d(TAG, "ArrayToJson->"+locusJSONArray);
//		return locusJSONArray;
//		 
//	 }
//<!---------------------------------------------->	
	private void setupviewcomponent() {
		// TODO Auto-generated method stub
		// <!------------設定所宣告的物件對應的ID------------------------>
		icSpin4111track = (Spinner) findViewById(R.id.icSpin4111track);
		icBtn4111track=(Button)findViewById(R.id.icBtn4111track);
		icBtn4112track=(Button)findViewById(R.id.icBtn4112trcak);
		icBtn4113track=(Button)findViewById(R.id.icBtn4113track);
		icBtn4114track=(Button)findViewById(R.id.icBtn4114track);
        icBtn4115track=(Button)findViewById(R.id.icBtn4115track);
		// <!--------------設定4111spinner--------------------------------------------->
		ArrayAdapter<CharSequence> adap_icSpin4111track_List = ArrayAdapter.createFromResource(this,
				R.array.icSpiArr4111track, android.R.layout.simple_spinner_item);
		icSpin4111track.setAdapter(adap_icSpin4111track_List);
		icSpin4111track.setOnItemSelectedListener(icSpin4111trackon);
		// <!-------------------------------------------->
		icBtn4111track.setOnClickListener(this);
		icBtn4112track.setOnClickListener(this);
		icBtn4113track.setOnClickListener(this);
		icBtn4114track.setOnClickListener(this);
        icBtn4115track.setOnClickListener(this);
	}

	// <!---------------------------------------------------->

	
	//<!----------------------------------------------------------------->
	@Override
	public void onClick(View v) {
		Intent itLocation=new Intent(getBaseContext(), ICareLocationService.class);
		
		switch (v.getId()) {
		case R.id.icBtn4111track://我的位置
            if(btnMyposi==0){
            	runPosition.postDelayed(autoMove, 3000);
            	btnMyposi=1;
            	icBtn4111track.setBackgroundColor(getResources().getColor(R.drawable.Black));
            	icBtn4111track.setTextColor(getResources().getColor(R.drawable.Aqua));
            	icBtn4111track.setText("停止");
            }else if(btnMyposi==1){
            	runPosition.removeCallbacks(autoMove);
            	btnMyposi=0;
            	icBtn4111track.setBackgroundColor(getResources().getColor(R.drawable.Aqua));
            	icBtn4111track.setTextColor(getResources().getColor(R.drawable.Black));
            	icBtn4111track.setText("我的位置");
            	Toast.makeText(IC4000track.this, "已停止自動定位...",Toast.LENGTH_SHORT).show();
            }			
			
			break;
		case R.id.icBtn4112trcak://尾行模式
			if(btnTarget==0){
				if(getSelectAccount==null||getSelectReg_id.equals("")){
					if(getSelectAccount==null){
						Toast.makeText(IC4000track.this,"請先選擇尾行目標...", Toast.LENGTH_SHORT).show();
						mysidebar01.toggleLeftDrawer();
						return;
					}else  {
						Toast.makeText(IC4000track.this,"目標註冊帳號有錯誤...", Toast.LENGTH_SHORT).show();
						return;
					}					
				} else {
					btnTarget = 1;
					icBtn4112track.setBackgroundColor(getResources().getColor(R.drawable.Black));
					icBtn4112track.setTextColor(getResources().getColor(R.drawable.Fuchsia));
					icBtn4112track.setText("停止尾行");
					runPosition.postDelayed(trackTarget, 1 * 1000);// 開始執行緒
					Toast.makeText(IC4000track.this, "開始自動追蹤目標的位置...", Toast.LENGTH_SHORT).show();
				}
			  
			} else if (btnTarget == 1) {
				btnTarget = 0;
				icBtn4112track.setBackgroundColor(getResources().getColor(R.drawable.Fuchsia));
				icBtn4112track.setTextColor(getResources().getColor(R.drawable.Lime));
				icBtn4112track.setText("尾行模式");
				Toast.makeText(IC4000track.this, "已停止自動追蹤目標的位置...",Toast.LENGTH_SHORT).show();
				runPosition.removeCallbacks(trackTarget);
				icWebVie4110track.loadUrl(MAP_URL);
			}

			break;
		case R.id.icBtn4113track://尾行路線
			
            if(btnNavigation==0){
				if(getSelectAccount==null||targetLatLng==null){
					if(getSelectAccount==null){
						Toast.makeText(IC4000track.this,"請先選擇尾行目標...", Toast.LENGTH_SHORT).show();
						mysidebar01.toggleLeftDrawer();
						return;
					}else  {
						Toast.makeText(IC4000track.this,"找不到目標位置無法規畫路線...", Toast.LENGTH_SHORT).show();
						return;
					}					
				}else {
	            	btnNavigation=1;
	            	icBtn4113track.setBackgroundColor(getResources().getColor(R.drawable.Black));
	            	icBtn4113track.setTextColor(getResources().getColor(R.drawable.Yellow));
	            	icBtn4113track.setText("停止規劃");
	            	
					final String URL = "javascript:getNavon("+btnNavigation+")" ;//+","+myPosition+","+targetLatLng+
	    			icWebVie4110track.loadUrl(URL);
//	    			runLocusData.postDelayed(pathWay, 5000);
				}
    			
    			
            }else if (btnNavigation==1) {
				btnNavigation=0;
            	icBtn4113track.setBackgroundColor(getResources().getColor(R.drawable.Yellow));
            	icBtn4113track.setTextColor(getResources().getColor(R.drawable.Black));
            	icBtn4113track.setText("尾行路線");
				final String URL = "javascript:getNavon("+btnNavigation+")" ;//+","+myPosition+","+
				icWebVie4110track.loadUrl(URL);
				icWebVie4110track.loadUrl(MAP_URL);
//				runLocusData.removeCallbacks(pathWay);
			}
			
			break;	
		case R.id.icBtn4114track://上傳位置
			if(btnsend==0){
				
				Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
//				bundle.putString("action", "login");
				bundle.putString("account", loginAccount);//傳送變數 String accoumt=登入者帳號
				itLocation .putExtras(bundle);//用intent傳送Bundle
				startService(itLocation);//參考intent開啟服務;這裡是ICareLocationService
				btnsend=1;
				icBtn4114track.setBackgroundColor(getResources().getColor(R.drawable.Red));
				icBtn4114track.setTextColor(getResources().getColor(R.drawable.Blue));
				icBtn4114track.setText("停止紀錄");
			}else if(btnsend==1) {
				icBtn4114track.setBackgroundColor(getResources().getColor(R.drawable.Blue));
				icBtn4114track.setTextColor(getResources().getColor(R.drawable.Red));
				btnsend=0;
				icBtn4114track.setText("紀錄位置");
				stopService(itLocation);//關掉service
			}
			break;
			case R.id.icBtn4115track:
				mysidebar01.toggleLeftDrawer();
			break;
		}
		
	}
//	private void startNavigation() {
//		String myPosition=getNowLatLng();
////		String[] getPosition=getSQL_TargetPosition(getSelectAccount);
//		
//	    if (webviewReady) {
//				// 由輸入的經緯度值標註在地圖上，呼叫在googlemaps.html中的mark函式
//				final String naviURL = "javascript:RoutePlanning(" + "24.179051,120.600610" + "," +"24.136829,120.685011"+ ")";
//				Log.d(TAG, naviURL);
//				icWebVie4110track.loadUrl(naviURL);
//
//			}
//		
//	}

//------------取得mySQL裡面 目標帳號的位置----------------
	private String[] getSQL_TargetPosition(String getAccount) {
		String[] getLastLatLng = null;
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("query_string", "target"));
		nameValuePairs.add(new BasicNameValuePair("account", getAccount));

		String result = DBConnector.executeQuery("target", nameValuePairs);
		Log.d(TAG, "targetPosition=" + result.toString());
		
		try {

			JSONArray jsonArray = new JSONArray(result);

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject jsonData = jsonArray.getJSONObject(i);

				getLastLatLng = jsonData.get("latlng").toString().trim().split(",");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getLastLatLng;
	}
	//-------------------計算2組經緯度之間的距離---------------------------
	public double getDistance(double latitude1, double longitude1,double latitude2, double longitude2){
		   double radLatitude1 = latitude1 * Math.PI / 180;
		   double radLatitude2 = latitude2 * Math.PI / 180;
		   double l = radLatitude1 - radLatitude2;
		   double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
		   double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
		                    + Math.cos(radLatitude1) * Math.cos(radLatitude2)
		                    * Math.pow(Math.sin(p / 2), 2)));
		   distance = distance * 6378137.0;
		   distance = Math.round(distance * 10000) / 10000;

		   return distance ;
		}
	//---------------發送GCM的method--------------------------
	private void sendGCMmsg(final String msg, final String reg_id) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				String devices = reg_id;//Client 端的 Registration ID  
		   	      Log.d("tcnr6==>", devices);
		    	  Sender sender = new Sender(API_Key);// Android API KEY  gcm的方法

		    		   Message message = new Message.Builder().addData("message", msg).build();//傳送的訊息      gcm的方法
		       	       Result result = null;
		     	      try {
		     	       result = sender.send(message, devices, 5);
		     	      } catch (IOException e) {
		     	       // TODO Auto-generated catch block
		     	       e.printStackTrace();
		     	      }
		     	      if (result.getMessageId() != null) {
		     	       Log.i("result", "getMessageId = "+result.getMessageId());
		     	       String canonicalRegId = result.getCanonicalRegistrationId();
		     	       Log.i("canonicalRegId", "canonicalRegId = "+canonicalRegId);
		     	      }   
		   	     }

			
		}).start();
	}

//	String getLat = null, getLng = null;
//	getLat = getLastLatLng[0];
//	getLng = getLastLatLng[1];
//	showTargetPosition(getLat, getLng);//好像可以不用因為沒用
//	private void showTargetPosition(String getLat, String getLng) {
//
//		if (webviewReady) {
//			// 由輸入的經緯度值標註在地圖上，呼叫在googlemaps.html中的mark函式
//			final String markURL = "javascript:mark(" + getLat + "," + getLng + ")";
//			icWebVie4110track.loadUrl(markURL);
//
//		}
//
//	}
	//------------------- 將尾行目標的經緯度傳入javaScript function在地圖上顯示位置-----------
	private void showTrackPosition(String getLat, String getLng) {
	    if (webviewReady) {
				// 由輸入的經緯度值標註在地圖上，呼叫在googlemaps.html中的mark函式
				final String markURL = "javascript:mark(" + getLat + "," + getLng + ")";
				icWebVie4110track.loadUrl(markURL);

				// 畫面移至標註點位置，呼叫在googlemaps.html中的centerAt函式
				final String centerURL = "javascript:centerAt(" + getLat + "," + getLng + ")";
				icWebVie4110track.loadUrl(centerURL);
			}		
	}
	//---------------------執行緒 autoMove----------------------------------------------
	Runnable autoMove=new Runnable() {
		
		@Override
		public void run() {
			
		    if(webviewReady){
		    	final String mypositionURL="javascript:myPosition ("+latGps+","+lngGps+")";
		    	icWebVie4110track.loadUrl(mypositionURL);//設定一個URL路徑去呼叫html黨裡面的function myPosition ("+latGps+","+lngGps+")
		    }                                            //並傳入經度跟緯度
		    
		    if(btnNavigation==1)
		    {   //-------當按下路徑規劃按鈕 傳送自己跟目標的經緯度給javaScript fountion
		    	String[] targetPosi=targetLatLng.trim().split(",");			   
			    final String pathwayURL="javascript:pathWay("+latGps+","+lngGps+","+targetPosi[0]+","+targetPosi[1]+")";
			    icWebVie4110track.loadUrl(pathwayURL);//設定一個URL路徑去呼叫html黨裡面的function myPosition ("+latGps+","+lngGps+")			    
		    }		    
			runPosition.postDelayed(this, 5000);
		}
	};
	//----------------追蹤目標的執行緒------------
	Runnable trackTarget=new Runnable() {

		@Override
		public void run() {
			targetLatLng=null;
			String[] getTrackPosition=getSQL_TargetPosition(getSelectAccount);
			String getLat=null,getLng=null;
			getLat=getTrackPosition[0];getLng=getTrackPosition[1];

			targetLatLng=getLat+","+getLng;//這是丟給Getend()
			
			showTrackPosition(getLat,getLng);			
			runPosition.postDelayed(this, 30*1000);//30秒就重複一次
			Double getTargetDistance=getDistance(latGps, lngGps, Double.parseDouble(getLat), Double.parseDouble(getLng));
			
			//---------判斷距離小於1公里就觸發GCM------------------
			if(getTargetDistance<1000){
				try {
					Thread.sleep(5000);
					String msg=loginAccount+"尾行靠近你身邊"+getTargetDistance+"公尺的範圍!!";
					sendGCMmsg( msg, getSelectReg_id);
					Log.d(TAG, "regID->"+getSelectReg_id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			Toast.makeText(IC4000track.this, "已更新目標位置\n尾行目標:"+getSelectAccount+"\n距離:"+getTargetDistance+"公尺", Toast.LENGTH_SHORT).show();
		}
		
	};

	
	// <!-----------------4111spinner的監聽-------------------------------------------->
	private Spinner.OnItemSelectedListener icSpin4111trackon = new Spinner.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			switch (position) {
			case 0:// 即時追蹤
				if(spinLocus==1){
					icWebVie4110track.loadUrl(MAP_URL);
					icBtn4111track.setVisibility(View.VISIBLE);
					icBtn4112track.setVisibility(View.VISIBLE);
					icBtn4113track.setVisibility(View.VISIBLE);				
	                spinLocus=0;
				}

				break;

			case 1:// 歷史軌跡
				if(spinLocus==0){
					if(getSelectAccount==null||targetLatLng==null){
						if(getSelectAccount==null){
							Toast.makeText(IC4000track.this,"請先選擇目標...", Toast.LENGTH_SHORT).show();
							mysidebar01.toggleLeftDrawer();
							icSpin4111track.setSelection(0, true);
							return;
						}else  {
							Toast.makeText(IC4000track.this,"找不到目標位置標示軌跡...", Toast.LENGTH_SHORT).show();
							icSpin4111track.setSelection(0, true);
							return;
						}					
					}else {
						icBtn4111track.setVisibility(View.INVISIBLE);
						icBtn4112track.setVisibility(View.INVISIBLE);
						icBtn4113track.setVisibility(View.INVISIBLE);
						
						spinLocus=1;
						SQL_TargetTrackLocus(getSelectAccount);
					}
				}
				

				break;
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	};
	//----------從mySQL裡面 輸出所有目標帳號的經緯度資料--------------
	private String[][] SQL_TargetTrackLocus( String getSelectAccount) {
 
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("query_string", "targetAll"));
		nameValuePairs.add(new BasicNameValuePair("account", getSelectAccount));

		String result = DBConnector.executeQuery("target", nameValuePairs);
		Log.d(TAG, "targetAllPosition=" + result.toString());
		String[] getLastLatLng = null,date=null;String[][] getLocusData=null;
		try {

			JSONArray jsonArray = new JSONArray(result);
            getLocusData=new String[jsonArray.length()][3];
            
			for (int i = 0; i < jsonArray.length(); i++) {
				String getLat = null, getLng = null,getDate=null;
				JSONObject jsonData = jsonArray.getJSONObject(i);
				
                getDate=jsonData.get("date").toString().trim();
                
				getLastLatLng = jsonData.get("latlng").toString().trim().split(",");
				getLat = getLastLatLng[0];
				getLng = getLastLatLng[1];
				
				getLocusData[i][0]=getDate.trim();
				getLocusData[i][1]=getLat;
				getLocusData[i][2]=getLng;
//				Log.d(TAG, "getLocusData="+getDate );
				
				date=jsonData.get("date").toString().trim().split(" ");
//				Log.d(TAG, "Date="+date[0]+"#"+date[1] );
				
			    showLocus(date,getLat,getLng);//把資料逐筆輸出
				//Log.d(TAG, "date="+getDate+"/getLat="+getLat+"/getLng="+getLng );
//				showTargetLocus(getDate,getLat, getLng,img);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "getLocusData="+getLocusData.toString() );
		return getLocusData;
	}

//String getDate,+ getDate + ","
	//--------把得到的目標資料拆開 在輸出給javaScript function
	private void showLocus( String[] date,String getLat, String getLng) {
		String[] YYMMDD=date[0].trim().split("/");
		String[] HHMinSS=date[1].trim().split(":");
		if (webviewReady) {
			// 由輸入的經緯度值標註在地圖上，呼叫在googlemaps.html中的mark函式
			final String markURL = "javascript:locusmark("+YYMMDD[0]+","+YYMMDD[1]+","+YYMMDD[2]+","+
			                          HHMinSS[0]+","+HHMinSS[1]+","+ getLat + "," + getLng + ")";
			icWebVie4110track.loadUrl(markURL);

		}
		
	}

//<!------------生命週期---------------------->
    @Override
    protected void onResume() { 
        if(!gps.isRunning()) gps.resumeGPS();   
        super.onResume();
    }

    @Override
    protected void onStop() {
       // gps.stopGPS();
        super.onStop();
    }
	@Override
	protected void onDestroy() {

//		Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
////		bundle.putString("action", "login");
//		bundle.putString("account", IC7001.LoginAccount);//傳送變數 放進Bundle裡面
//		Intent itLocation=new Intent(getBaseContext(), ICareLocationService.class);
//		itLocation .putExtras(bundle);//用intent傳送Bundle
//		stopService(itLocation);//關掉上傳定位點service
		
		runPosition.removeCallbacks(autoMove);//關掉自動定位
		runPosition.removeCallbacks(trackTarget);//關掉追蹤目標
		super.onDestroy();
	}
//<!---------------------------------------------->
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.ic4000track, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
		case R.id.action_finish:
			IC4000track.this.finish();
			break;
		case R.id.ic4000queryItem:// 跳回生理狀態頁面
			Intent itQuery = new Intent();
			itQuery.setClass(IC4000track.this, IC4000.class);
			startActivity(itQuery);
			break;
		case R.id.ic4000opendataItem://開啟icare資訊站
			Intent itOpendata = new Intent();
			itOpendata.setClass(IC4000track.this, IC4000OpenData.class);
			startActivity(itOpendata);
			
			break;

		}
		return super.onOptionsItemSelected(item);
	}
//<!----------------由GPS類別裡面繼承來的interface------------------------------------->
	@Override
	public void locationChanged(Location loc,double longitude, double latitude) {//當GPS位置有改變的時候經緯度會傳進來
 
		latGps=latitude;//將傳入的經度跟緯度設定給我們的變數使用
		lngGps=longitude;		
	}

	@Override
	public void displayGPSSettingsDialog() {//當GPS服務沒有開啟的時候會觸動這method
		// TODO Auto-generated method stub
	
		Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
		startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));//開啟設定頁面
         
	}
//<!--------------------------------------------------------------------->
	private String getNowLatLng() {
		String latlng=latGps+","+lngGps;
		return latlng.trim();
	}

	

	





}
//Runnable pathWay=new Runnable() {
//
//@Override
//public void run() {
//	String[] targetPosi=targetLatLng.trim().split(",");
//	
//    if(webviewReady){
//    	final String pathwayURL="javascript:pathWay("+latGps+","+lngGps+","+targetPosi[0]+","+targetPosi[1]+")";
//    	icWebVie4110track.loadUrl(pathwayURL);//設定一個URL路徑去呼叫html黨裡面的function myPosition ("+latGps+","+lngGps+")
//    }
//	runLocusData.postDelayed(this,5000);
//}
//};
//Runnable getLocusData=new Runnable() {
//
//@Override
//public void run() {
//	SQL_TargetTrackLocus(getSelectAccount);
//	
//}
//};

//private JSONArray dataToJSON(String[][] locusData) {
//locusJSONArray=null;
//JSONArray jsonArray=new JSONArray();		
//
//for (int i = 0; i < locusData.length; i++) {
//	JSONObject jsonObject = new JSONObject();
//
//	try {
//		jsonObject.put("date", locusData[i][0]);
//		jsonObject.put("lat", locusData[i][1]);
//		jsonObject.put("lng", locusData[i][2]);
//		
//		jsonArray.put(jsonObject);
//	} catch (JSONException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//
//}
//locusJSONArray=jsonArray;
//	
//return jsonArray;
//
//}
//private void showTargetLocus(String getDate, String getLat, String getLng, String img) {
//
//	// TODO Auto-generated method stub
//	if (webviewReady) {
//		final String myLocusURL = "javascript:locusMarker(" + getDate + "," + getLat + "," + getLng + "," + img	+ ")";
//		icWebVie4110track.loadUrl(myLocusURL);	
//		Log.d(TAG, myLocusURL );
//	}		
//
//}
