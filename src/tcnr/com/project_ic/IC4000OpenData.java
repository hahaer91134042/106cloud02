package tcnr.com.project_ic;





import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;
import android.widget.AdapterView.OnItemSelectedListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;





public class IC4000OpenData extends FragmentActivity 
                            implements IGPSActivity,
                                       InfoWindowAdapter,
                                       OnCameraChangeListener, 
                                       OnMarkerClickListener{

	private Spinner icSpin4401, icSpin4402Maptype;
	private Button icBtn4401;
	private static ContentResolver mContRes;
	private String[] MYCOLUMN_OpenData = new String[] {"id","weather","hospital","TaKaChia" };
	private String[][] getSQLiteHosData,getSQLitWearherData,getSQLiteTaKaChiaData;
	
	private GoogleMap map;
	private static LatLng mlatlng ;
	private UiSettings mUiSettings;
	private GPS gps;
	private LocationManager locationMgr;
	private String provider;
	private Marker markerMe;
	private  Circle circle;

	private double getmyGPSlat;
	private double getmyGPSlng;
	private float zoomsize = 15; // // 設定放大倍率1(地球)-21(街景)
	private float currentZoom = 17;
	private long startTime;
	private int hosDistance=0,bikeDistance=0;
	
//	String[] Textlat,Textlon,TextTEMP,TextHUMD,Text24R,Textloc,TextTOWN ;
	/**************************************
	 * 
	 * 
	 * 
	 * 
	 * *************************************/
	
	String TAG="tcnr6==>";
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.ic4000opendata);
		gps=new GPS(this);
		startTime=System.currentTimeMillis();
		setupviewcomponent();
		getOpenData();

	}
	

	private void getOpenData() {//開始從SQLite取出存入的opendata JSONarray並且解析
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				getSQLitWearherData = null;
				getSQLiteHosData = null;
				getSQLiteTaKaChiaData = null;
				mContRes = getContentResolver();
				String[][] getOpenData = SQLiteWriter.getSQLiteData(mContRes, DBContentProvider.CONTENT_URI_OpenData,
						MYCOLUMN_OpenData);
				// for (int i = 0; i < getOpenData.length; i++) {
				// Log.d(TAG, "getOpenData[0][0]->"+getOpenData[0][0]);->id
				// Log.d(TAG, "getOpenData[0][1]->"+getOpenData[0][1]);->weatherData
				// Log.d(TAG, "getOpenData[0][2]->"+getOpenData[0][2]);->hospitalData
				// Log.d(TAG, "getOpenData[0][3]->"+getOpenData[0][3]);->UbikeData
				// }

				// Log.d(TAG, "weatherjsonArray->"+weatherjsonArray);
				try {
					JSONArray weatherjsonArray = new JSONArray(getOpenData[0][1]);

					getSQLitWearherData = new String[weatherjsonArray.length()][7];					
					//依照"KEY"取出所有的value然後存進二維陣列裡面
					for (int i = 0; i < weatherjsonArray.length(); i++) {
						
						JSONObject jsonData = weatherjsonArray.getJSONObject(i);

						getSQLitWearherData[i][0] = jsonData.get("TOWN").toString().trim();
						getSQLitWearherData[i][1] = jsonData.get("loc").toString().trim();
						getSQLitWearherData[i][2] = jsonData.get("lat").toString().trim();
						getSQLitWearherData[i][3] = jsonData.get("lon").toString().trim();
						getSQLitWearherData[i][4] = jsonData.get("TEMP").toString().trim();
						getSQLitWearherData[i][5] = jsonData.get("HUMD").toString().trim();
						getSQLitWearherData[i][6] = jsonData.get("H_24R").toString().trim();
						// Log.d(TAG,
						// "getSQLitWearherData[i][0]->"+getSQLitWearherData[i][0]);

					}
					JSONArray hosjsonArray = new JSONArray(getOpenData[0][2]);
					getSQLiteHosData = new String[hosjsonArray.length()][8];
					//依照"KEY"取出所有的value然後存進二維陣列裡面
					for (int i = 0; i < hosjsonArray.length(); i++) {
						JSONObject jsonData = hosjsonArray.getJSONObject(i);

						getSQLiteHosData[i][0] = jsonData.get("hosName").toString().trim();
						getSQLiteHosData[i][1] = jsonData.get("hosTenure").toString().trim();
						getSQLiteHosData[i][2] = jsonData.get("hosType").toString().trim();
						getSQLiteHosData[i][3] = jsonData.get("hosLoc").toString().trim();
						getSQLiteHosData[i][4] = jsonData.get("hosTel").toString().trim();
						getSQLiteHosData[i][5] = jsonData.get("hosAddr").toString().trim();
						getSQLiteHosData[i][6] = jsonData.get("Lat").toString().trim();
						getSQLiteHosData[i][7] = jsonData.get("Lng").toString().trim();
					}
					JSONArray bikejsonArray = new JSONArray(getOpenData[0][3]);
					// Log.d(TAG, "bikejsonArray->"+bikejsonArray);
					getSQLiteTaKaChiaData = new String[bikejsonArray.length()][10];
					//依照"KEY"取出所有的value然後存進二維陣列裡面
					for (int i = 0; i < bikejsonArray.length(); i++) {
						JSONObject jsonData = bikejsonArray.getJSONObject(i);

						getSQLiteTaKaChiaData[i][0] = jsonData.getString("sna").toString().trim();
						getSQLiteTaKaChiaData[i][1] = jsonData.getString("tot").toString().trim();
						getSQLiteTaKaChiaData[i][2] = jsonData.getString("sbi").toString().trim();
						getSQLiteTaKaChiaData[i][3] = jsonData.getString("sarea").toString().trim();
						getSQLiteTaKaChiaData[i][4] = jsonData.getString("mday").toString().trim();
						getSQLiteTaKaChiaData[i][5] = jsonData.getString("lat").toString().trim();
						getSQLiteTaKaChiaData[i][6] = jsonData.getString("lng").toString().trim();
						getSQLiteTaKaChiaData[i][7] = jsonData.getString("ar").toString().trim();
						getSQLiteTaKaChiaData[i][8] = jsonData.getString("bemp").toString().trim();
						getSQLiteTaKaChiaData[i][9] = jsonData.getString("act").toString().trim();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}


	private void setupviewcomponent() {
		icSpin4402Maptype = (Spinner) this.findViewById(R.id.icSpin4402);
		icSpin4401=(Spinner)this.findViewById(R.id.icSpin4401);
		icBtn4401=(Button)findViewById(R.id.icBtn4401);
		icBtn4401.setOnClickListener(btnOn);
		//------設定選取opendata類型的spinner--------------
		ArrayAdapter<String> openDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		String[] openData = getResources().getStringArray(R.array.dataType);
		for (int i = 0; i < openData.length; i++) {
			openDataAdapter.add(openData[i]);
		}
		openDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		icSpin4401.setAdapter(openDataAdapter);
		icSpin4401.setOnItemSelectedListener(dataTypeOn);
		//-------------------------------------------------
		//------------------設定mapType Spinner--------
		ArrayAdapter<String> mapTypeadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		String[] mapType = getResources().getStringArray(R.array.mapType);
		for (int i = 0; i < mapType.length; i++) {
			mapTypeadapter.add(mapType[i]);
		}
		mapTypeadapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		icSpin4402Maptype.setAdapter(mapTypeadapter);
		icSpin4402Maptype.setOnItemSelectedListener(mapTypeOn);
        //------------------------------------------
	}
	private Button.OnClickListener btnOn=new Button.OnClickListener()
			{//移動鏡頭到自己的位置

				@Override
				public void onClick(View v) {
					cameraFocusOnMe(getmyGPSlat, getmyGPSlng);					
				}
		
			};
	
	private OnItemSelectedListener dataTypeOn = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			
			switch (position) {
			case 0:
				map.clear();
				hosDistance=0;
				bikeDistance=0;
				break;
			case 1://天氣
				Toast.makeText(IC4000OpenData.this,"資料讀取中,請稍後...",Toast.LENGTH_SHORT).show();
				map.clear();				
				showWeatherData();
				hosDistance=0;
				bikeDistance=0;
				break;
			case 2://醫院
				Toast.makeText(IC4000OpenData.this,"資料讀取中,請稍後...",Toast.LENGTH_SHORT).show();
				map.clear();
				hosDistance=1;
				bikeDistance=0;
				showHospitalData();
				
				break;
			case 3://Ubike
				Toast.makeText(IC4000OpenData.this,"資料讀取中,請稍後...",Toast.LENGTH_SHORT).show();
				map.clear();
				hosDistance=0;
				bikeDistance=1;
				showTaKaChiaData();
				break;
			}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	};
	/**************************
	 * getSQLitWearherData[i][0]=TOWN  getSQLitWearherData[i][4]=TEMP
	 * getSQLitWearherData[i][1]=loc   getSQLitWearherData[i][5]=HUMD
	 * getSQLitWearherData[i][2]=lat   getSQLitWearherData[i][6]=H_24R
	 * getSQLitWearherData[i][3]=lon
	 *************************/	
	private void showWeatherData() {
		int i=0;
		   for (String[] SQLitWearherData : getSQLitWearherData) {	
		    i++;
//		   }for (int i = 0; i < getSQLitWearherData.length; i++) {			   
		   BitmapDescriptor image = null;
		   String idName=null,imgName=null;
		   int infoBackGround=0;
		   
		   Double dLat = Double.parseDouble(SQLitWearherData[2]); // 南北緯
		   Double dLon = Double.parseDouble(SQLitWearherData[3]); // 東西經		    
		    
		    // --- 設定所選位置之當地圖片 ---//
		    if (Double.parseDouble(SQLitWearherData[6])>0.0) {
				  idName = "q" + String.format("%02d", 3);//地圖上面的icon
		          imgName = "q" + String.format("%02d", 3);//info裡面的小圖
		          infoBackGround=getResources().getIdentifier("flaginfored", "drawable",getPackageName());
			} else {
				if(Double.parseDouble(SQLitWearherData[4])>=27.0)
				{
					 idName = "q" + String.format("%02d", 0);
			         imgName = "q" + String.format("%02d", 0);
			         infoBackGround=getResources().getIdentifier("flaginfoyellow", "drawable",getPackageName());
				}else 
				{
					 idName = "q" + String.format("%02d", 1);
			         imgName = "q" + String.format("%02d", 1);
			         infoBackGround=getResources().getIdentifier("flaginfogreen", "drawable",getPackageName());
				}		          
			}
		    int flagiconID=getResources().getIdentifier(imgName, "raw",getPackageName());
		    
		    String vtitle = SQLitWearherData[1]+"#"+flagiconID+"#"+infoBackGround;
		   

		    int markiconID = getResources().getIdentifier(idName, "raw",getPackageName());
		    image = BitmapDescriptorFactory.fromResource(markiconID);
		   
		    LatLng  latlng = new LatLng(dLat,dLon);//更新成欲顯示的地圖座標   
		 
		    
		    Marker vgps = map.addMarker(new MarkerOptions()
		      .position(latlng)
		      .alpha(0.8f)
		      .infoWindowAnchor(1.6f, 0.9f)
		      .title(i+"."+vtitle)//i+"."+
		      .snippet("座標:" + dLat + "," + dLon+"\n溫度:"+SQLitWearherData[4]+"度C" + "\n濕度:" + Double.parseDouble(SQLitWearherData[5])*100.0 +
		    		  "%"+ "\n24小時降雨量:" + SQLitWearherData[6]+"毫米")
		      .icon(image));// 顯示圖標文字與照片 alpha 透明度 0~1
		 // .draggable(true) //設定maker 可移動
//		    map.setInfoWindowAdapter(new CustomInfoAdapter());//外圓內方，使用自定義式窗
		   }
	
}
	/********************************
	 * getSQLiteHosData[i][0]=hosName   getSQLiteHosData[i][4]=hosTel
	 * getSQLiteHosData[i][1]=hosTenure getSQLiteHosData[i][5]=hosAddr
	 * getSQLiteHosData[i][2]=hosType   getSQLiteHosData[i][6]=Lat
	 * getSQLiteHosData[i][3]=hosLoc    getSQLiteHosData[i][7]=Lng	  
	 *******************************************/
	private void showHospitalData() 
	{
		for (String[] SQLiteHosData : getSQLiteHosData) {			
//		}for (int i = 0; i < getSQLiteHosData.length; i++) {
		   BitmapDescriptor image = null;
		   String idName=null,imgName=null;
		   int infoBackGround=0;
		   
		   Double hLat = Double.parseDouble(SQLiteHosData[6]); // 南北緯
		   Double hLng = Double.parseDouble(SQLiteHosData[7]); // 東西經
		   
		   if (SQLiteHosData[2].equals("醫院"))
		   {   idName = "hos64";
			   imgName = "hospital";
			   infoBackGround=getResources().getIdentifier("hosflag", "drawable",getPackageName());
			  
		   }else if(SQLiteHosData[2].equals("綜合醫院"))
		   {  
			   idName = "hoscenter64";//地圖上的icon
			   imgName = "hospitalcenter";//info內小圖
			   infoBackGround=getResources().getIdentifier("hoscenterflag", "drawable",getPackageName());
			  
		   }
		  
		   int flagiconID=getResources().getIdentifier(imgName, "raw",getPackageName());   
		   
		   String vtitle = SQLiteHosData[0]+"#"+flagiconID+"#"+infoBackGround;
		   
		    int markiconID = getResources().getIdentifier(idName, "raw",getPackageName());
		    image = BitmapDescriptorFactory.fromResource(markiconID);
		   
		   LatLng  latlng = new LatLng(hLat,hLng);//更新成欲顯示的地圖座標 
		   
		   map.addMarker(new MarkerOptions()
				      .position(latlng)
				      .alpha(0.8f)
				      .infoWindowAnchor(0.5f, 0.9f)
				      .title(vtitle)
				      .snippet("地區:"+SQLiteHosData[3]+"\nTel:"+SQLiteHosData[4] + "\n地址:" + SQLiteHosData[5])
				      .icon(image));// 顯示圖標文字與照片 alpha 透明度 0~1
				 // .draggable(true) //設定maker 可移動
//				    map.setInfoWindowAdapter(new CustomInfoAdapter());//外圓內方，使用自定義式窗
	    }
			
		
		
	}
	/****************************************************
	 * getSQLiteTaKaChiaData[i][0]:sna  getSQLiteTaKaChiaData[i][5]:lat
	 * getSQLiteTaKaChiaData[i][1]:tot  getSQLiteTaKaChiaData[i][6]:lng
	 * getSQLiteTaKaChiaData[i][2]:sbi  getSQLiteTaKaChiaData[i][7]:ar
	 * getSQLiteTaKaChiaData[i][3]:sarea  getSQLiteTaKaChiaData[i][8]:bemp
	 * getSQLiteTaKaChiaData[i][4]:mday  getSQLiteTaKaChiaData[i][9]:act	 
	 *****************************************************/
	private void showTaKaChiaData() //在地圖上標示ubike的資料
	{
		cameraFocusOnMe(25.046074, 121.533991);
		for (String[] SQLiteTaKaChiaData : getSQLiteTaKaChiaData) {			
//		}
//		for (int i = 0; i < getSQLiteTaKaChiaData.length; i++){  
		   BitmapDescriptor image = null;
		   String idName=null,imgName=null,status=null;
		   int infoBackGround=0;
		   
		   Double bikeLat = Double.parseDouble(SQLiteTaKaChiaData[5]); // 南北緯
		   Double bikeLng = Double.parseDouble(SQLiteTaKaChiaData[6]); // 東西經
		   
		   if (SQLiteTaKaChiaData[9].equals("1")) 
		   {
			   idName="ubike";
			   imgName="ubikelogo";
			   status="可用";
			   infoBackGround=getResources().getIdentifier("bikeinfo", "drawable",getPackageName());
			
		   }else if (SQLiteTaKaChiaData[9].equals("0")) 
		   {
			   idName="bikestop";
			   imgName="cry";
			   status="禁用中";
			   infoBackGround=getResources().getIdentifier("bikeinfo", "drawable",getPackageName());
		   }	
		   
		    int flagiconID=getResources().getIdentifier(imgName, "raw",getPackageName()); 	
		    
		    String vtitle = SQLiteTaKaChiaData[0]+"#"+flagiconID+"#"+infoBackGround;
 		
		    int markiconID = getResources().getIdentifier(idName, "drawable",getPackageName());
		    image = BitmapDescriptorFactory.fromResource(markiconID);
		   
		   LatLng  bikelatlng = new LatLng(bikeLat,bikeLng);//更新成欲顯示的地圖座標	
		   String getupdate=parseSaveDate(SQLiteTaKaChiaData[4]);  //時間格式轉換
		   
		   map.addMarker(new MarkerOptions()
				      .position(bikelatlng)
				      .alpha(1.0f)
				      .infoWindowAnchor(2.0f, 0.2f)
				      .title(vtitle)
				      .snippet("地址:"+SQLiteTaKaChiaData[3]+SQLiteTaKaChiaData[7]+"\n總停車格:"+SQLiteTaKaChiaData[1] + 
				    		  "\n目前車輛數量:" + SQLiteTaKaChiaData[2] +"\n空位數:"+SQLiteTaKaChiaData[8] +"\n禁用狀態:"+status+"\n更新時間:"+getupdate)
				      .icon(image));// 顯示圖標文字與照片 alpha 透明度 0~1
		   
		}
		
	}
	//--------轉換時間顯示格式---------
	public  String parseSaveDate(String getDate) {
	    long saveDate=0;String getChangeTime=null;
		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat newDateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			saveDate = Dateformatter.parse(getDate).getTime();
			
			getChangeTime=newDateformatter.format(saveDate);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return getChangeTime;
	}
	
	private OnItemSelectedListener mapTypeOn = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			switch (position) {
			case 0:
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 道路地圖。
				break;
			case 1:
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星空照圖
				break;
			case 2:
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); // 地形圖
				break;
			case 3:
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID); // 道路地圖混合空照圖
				break;
			case 4:// 顯示路況
				map.setTrafficEnabled(true);
				break;
			case 5:// 隱藏路況
				map.setTrafficEnabled(false);
				break;
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
	};	
	//-----------------開啟map---------------------------
		private void initMap() {
			if (map == null) 
			{
				map = ((MapFragment) getFragmentManager().findFragmentById(R.id.icMapFrag4400)).getMap();
		  	    map.setInfoWindowAdapter(this);
		  	    map.setOnCameraChangeListener(this);
		  	    map.setOnMarkerClickListener(this);
				if (map != null) 
				{
					// 設定地圖類型--------------------
					map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					
					// --------------------------
					map.setMyLocationEnabled(true); // 顯示自己位置
					mUiSettings = map.getUiSettings();
					// Keep the UI Settings state in sync with the checkboxes.
					// 顯示縮放按鈕
					mUiSettings.setZoomControlsEnabled(true);
					// 顯示指北針
					mUiSettings.setCompassEnabled(true);
					// 顯示我的位置按鈕
					mUiSettings.setMyLocationButtonEnabled(true);
					// 顯示我的位置圖示
					map.setMyLocationEnabled(true);
					mUiSettings.setScrollGesturesEnabled(true);// 開啟地圖捲動手勢
					mUiSettings.setZoomGesturesEnabled(true);// 開啟地圖縮放手勢
//					mUiSettings.setTiltGesturesEnabled(isChecked(R.id.tilt_toggle));// 開啟地圖傾斜手勢
					mUiSettings.setRotateGesturesEnabled(true);// 開啟地圖旋轉手勢 
					// ----設定地圖初始值大小
					// 移動地圖鏡頭到指定座標點,並設定地圖縮放等級
//					map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));
					// --------------------------------					
				}
			}
		}
		private String getTimeString(long time) {
			
			SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String sysTime = Dateformatter.format(time);
			return sysTime;
		}
		//----------------------------------------------
		private void getGPSlocation(Location location) {//onLOcationChange會呼叫這個method
			String where = "";
			if(location!=null){
				getmyGPSlat=0;getmyGPSlng=0;//規0
				double lat = location.getLatitude();
			    double	lng = location.getLongitude();
			    
			    float speed = location.getSpeed();
				long time = location.getTime();
				String timeStr = getTimeString(time);
				where = "經度：" + lng + "\n緯度：" + lat + "\n速度：" + speed + "\n時間：" + timeStr;

				// 標記"我的位置"
				showMarkerMe(lat, lng);
//				cameraFocusOnMe(lat, lng);
//				trackMe(lat, lng);				
				
				getmyGPSlat=lat;getmyGPSlng=lng;
				
//				map.addMarker(new MarkerOptions().position(VGPS)
//						.title("目前位置").snippet("座標:" + lat + "," + lng)
//						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//						.rotation(90));	//.showInfoWindow()
			}else{
				where = "GPS位置訊號消失！";
				Toast.makeText(IC4000OpenData.this,where,Toast.LENGTH_LONG).show();
			}			
			
		}	
		private void cameraFocusOnMe(double lat, double lng) { /* 移動地圖鏡頭 */
			CameraPosition camPosition = new CameraPosition.Builder()
					.target(new LatLng(lat, lng)).zoom(zoomsize).build();
			map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
		}
		
		private double radius=1000;
	    private ArrayList<Marker> hostmarker=new ArrayList<Marker>();
		private void showMarkerMe(double gpsLat, double gpsLng) {
			if (markerMe != null) {
				markerMe.remove();
				circle.remove();
				//----------
				for (Marker mark : hostmarker) {//清除在上一次在距離範圍內的marker
					mark.remove();
				}
				//------------
			}
			hostmarker.clear();//清空ArrayList
//			hosMark=null;
			int mFillColor= Color.argb(60,255,0 ,255);//設定圓周的顏色跟透明度
			
			int  resID = getResources().getIdentifier("p025", "raw",
				      getPackageName());
			int infoBackGround=getResources().getIdentifier("custom_info_bubble", "drawable",getPackageName());
			
			MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(gpsLat, gpsLng))
					                                     .title("目前位置"+"#"+resID+"#"+infoBackGround)
					                                     .snippet("GPS座標:\nLat:" + gpsLat + "\nLng:" + gpsLng)
					                                     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
					                                     ).infoWindowAnchor(0.5f, 1f);
			 circle = map.addCircle(new CircleOptions()
				     .center(new LatLng(gpsLat, gpsLng))
				     .radius(radius)
//				     .strokeColor(Color.RED)
				     .strokeWidth(0)
				     .fillColor(mFillColor)
				     );
            
			markerMe=map.addMarker(markerOpt);
			
			if(hosDistance==1)				
			{   
				/********************************
				 * getSQLiteHosData[i][0]=hosName   getSQLiteHosData[i][4]=hosTel
				 * getSQLiteHosData[i][1]=hosTenure getSQLiteHosData[i][5]=hosAddr
				 * getSQLiteHosData[i][2]=hosType   getSQLiteHosData[i][6]=Lat
				 * getSQLiteHosData[i][3]=hosLoc    getSQLiteHosData[i][7]=Lng	  
				 *******************************************/
				for (String[] SQLiteHosData : getSQLiteHosData) {					
//				}for (int i = 0; i <getSQLiteHosData.length; i++){
					Double getHosLat=Double.parseDouble(SQLiteHosData[6]);
					Double getHosLng=Double.parseDouble(SQLiteHosData[7]);
					Double getHosDistance=SQLiteWriter.getDistance(gpsLat,gpsLng,getHosLat,getHosLng);
					//逐筆判斷目標地點跟手機的距離
					if (getHosDistance<=1000.0) {
						Log.d(TAG, "getHosDis->"+getHosDistance);
						 int markiconID = getResources().getIdentifier("select", "drawable",getPackageName());
						 BitmapDescriptor   image = BitmapDescriptorFactory.fromResource(markiconID);
						 int  selectID = getResources().getIdentifier("p025", "raw",
							      getPackageName());
						 int selectInfoBackground=getResources().getIdentifier("border", "drawable",getPackageName());
						
						MarkerOptions hosMarkOption=new MarkerOptions().position(new LatLng(getHosLat,getHosLng))
								.title(SQLiteHosData[0]+"#"+selectID+"#"+selectInfoBackground)
								.snippet("距離"+getHosDistance+"公尺!!")
                                .icon(image);
					    Marker hosMark=map.addMarker(hosMarkOption);
						 Log.d(TAG, "getHosMarker->"+hosMark);
						hostmarker.add(hosMark);//記錄這一次在範圍內的marker
					}
				}
			}else if (bikeDistance==1) 
			{	/****************************************************
				 * getSQLiteTaKaChiaData[i][0]:sna  getSQLiteTaKaChiaData[i][5]:lat
				 * getSQLiteTaKaChiaData[i][1]:tot  getSQLiteTaKaChiaData[i][6]:lng
				 * getSQLiteTaKaChiaData[i][2]:sbi  getSQLiteTaKaChiaData[i][7]:ar
				 * getSQLiteTaKaChiaData[i][3]:sarea  getSQLiteTaKaChiaData[i][8]:bemp
				 * getSQLiteTaKaChiaData[i][4]:mday  getSQLiteTaKaChiaData[i][9]:act	 
				 *****************************************************/
				for (String[] SQLiteTaKaChiaData : getSQLiteTaKaChiaData) {					
//				}for (int i = 0; i <getSQLiteTaKaChiaData.length; i++){
				Double getBikeLat = Double.parseDouble(SQLiteTaKaChiaData[5]);
				Double getBikeLng = Double.parseDouble(SQLiteTaKaChiaData[6]);
				Double getBikeDistance = SQLiteWriter.getDistance(gpsLat, gpsLng, getBikeLat, getBikeLng);

				if (getBikeDistance <= 1000.0) 
				 {  //開始設定marker infowindow 還有要使用的img
					Log.d(TAG, "getBikeDistance->" + getBikeDistance);
					int markiconID = getResources().getIdentifier("select", "drawable", getPackageName());
					BitmapDescriptor image = BitmapDescriptorFactory.fromResource(markiconID);
					int selectID = getResources().getIdentifier("bikeinfoimg1", "raw", getPackageName());
					int selectInfoBackground = getResources().getIdentifier("border", "drawable", getPackageName());

					MarkerOptions bikeMarkOption = new MarkerOptions().position(new LatLng(getBikeLat, getBikeLng))
							.title(SQLiteTaKaChiaData[0] + "#" + selectID + "#" + selectInfoBackground)
							.snippet("距離" + getBikeDistance + "公尺!!").icon(image);
					Marker bikeMark = map.addMarker(bikeMarkOption);
					Log.d(TAG, "getbikeMarker->" + bikeMark);
					hostmarker.add(bikeMark);// 記錄這一次在範圍內的marker
				 }
			   }
				
			}
			
		}


		//----------------------------------
		private boolean initLocationProvider() {// 取得現在位置
			locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				provider = LocationManager.GPS_PROVIDER;// 取得GPS資料
				return true;
			}
			return false;
		}
		
		private Double taiLat=23.774319,taiLng=120.979868;
		private void nowaddress() {
			// 取得上次已知位置
			Location location = locationMgr.getLastKnownLocation(provider);
			getGPSlocation(location);
			if (location!=null)
			{//GPS有定位到 就移動到定位到的位置
				cameraFocusOnMe(location.getLatitude(), location.getLongitude());
			}else if (location==null) {//沒有就移動到預設的位置
				CameraPosition camPosition = new CameraPosition.Builder()
						.target(new LatLng(taiLat, taiLng)).zoom(8).build();
				map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
			}
			
			// 取得GPS listener
			locationMgr.addGpsStatusListener(gpsListener);
			// Location Listener
//			locationMgr.requestLocationUpdates(provider, minTime, minDist, this);
		}
		GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

			@Override
			public void onGpsStatusChanged(int event) {//監聽GPS的狀態
				// TODO Auto-generated method stub
				switch (event) {
				case GpsStatus.GPS_EVENT_STARTED:
					
					break;

				case GpsStatus.GPS_EVENT_STOPPED:
					
					break;

				case GpsStatus.GPS_EVENT_FIRST_FIX:
					
					break;

				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					
					break;
				}
			}
			
		};
		


	
//----------------LifeCycle--------
	@Override
	protected void onStart() {
		initMap();//開啟地圖
		if (initLocationProvider()) {
			nowaddress();		

		} else
		{
			Toast.makeText(this,"沒有取得GPS訊號...",Toast.LENGTH_SHORT).show();
		}
		super.onStart();
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (!gps.isRunning())
			gps.resumeGPS();
		initMap();
		if (initLocationProvider()) {
			nowaddress();

		}else
		{
			Toast.makeText(this,"沒有取得GPS訊號...",Toast.LENGTH_SHORT).show();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		gps.stopGPS();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	//----------------------------------
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void displayGPSSettingsDialog() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
		startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));//開啟設定頁面
	}
	
	@Override
	public void locationChanged(Location loc, double longitude, double latitude) {
        long nowTime=loc.getTime();
        long betweenTime=(nowTime-startTime)/1000;
//        Log.d(TAG, "betweenTime->"+betweenTime);
        if(betweenTime%5==0 && betweenTime>0)
			 getGPSlocation(loc); 
	
		
		
	}

	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		String[] getTitle=marker.getTitle().split("#");//傳過來的格式 title#infoImgResource#infoBackgroundResource
		
		 View infoWindow = getLayoutInflater().inflate(R.layout.infowindow_style, null);
		 infoWindow.setBackgroundResource(Integer.parseInt(getTitle[2]));
		
		 
		 TextView infoTitle = (TextView) infoWindow.findViewById(R.id.infoTitle);
		 infoTitle.setText(getTitle[0]);
		 
		 TextView infoContent = (TextView) infoWindow.findViewById(R.id.infoContent);
		 infoContent.setText(marker.getSnippet());
		 
		 ImageView infoImg=(ImageView)infoWindow.findViewById(R.id.infoImg);
		 infoImg.setImageResource(Integer.parseInt(getTitle[1]));
		 
		return infoWindow;
	}


	@Override
	public void onCameraChange(CameraPosition camPosition) {
		zoomsize=camPosition.zoom;
		
	}


	@Override
	public boolean onMarkerClick(final Marker marker_Animation) {
		 // 設定動畫
		   final Handler handler = new Handler();
		   final long start = SystemClock.uptimeMillis();
		   final long duration = 1500;

		   final Interpolator interpolator = new BounceInterpolator();

		   handler.post(new Runnable() {
		    @Override
		    public void run() {
		     long elapsed = SystemClock.uptimeMillis() - start;
		     float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
		     marker_Animation.setAnchor(0.5f, 1.0f + 2 * t);

		     if (t > 0.0) {
		      // Post again 16ms later.
		      handler.postDelayed(this, 16);
		     }
		    }
		   });
		return false;
	}
	

}

