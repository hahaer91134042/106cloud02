package tcnr.com.project_ic;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;

public class ICareService extends Service {


	@Override
	public boolean stopService(Intent name) {
		Log.d(TAG, "stopService....");
		return super.stopService(name);
	}

//	public static String[] Textlat,Textlon,TextTEMP,TextHUMD,Text24R,Textloc,TextTOWN ;
//	public static String[] hosLat,hosLng,hosName,hosLoc,hosTenur,hosType,hosTel,hosAddr ;
//	public static String[] bikeSNA,biketot,bikesbi,bikesarea,bikemday,bikelat,bikelng,bikear,bikebemp,bikeact ;
	
//	public static String[][] TaKaChiaData;//,weatherData hospitalData,
	
	private JSONArray weatherJSONarr,hosJSONarr,bikeJSONarr;
	// --------------
	private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};
	private String[] MYLOGINSTATUSCOLUMN = new String[]{"id", "loginDate","account","code", "loginStatus" };
	private String[] MYCOLUMN_GCMmsg = new String[] {"id","msgDate","GCMmsg" };
	private String[] MYCOLUMN_OpenData = new String[] {"id","weather","hospital","TaKaChia" };
	// ----------------
	Handler handler = new Handler();
	 private Long startTime;
	 int autotime = 60,checkweather=0,checkbike=0,checkhos=0;
	 protected static int checkSave=0;
    private  String loginAccount=null,action=null;
    String TAG="tcnr6==>";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		action= intent.getExtras().getString("action");
		if(action.equals("login")){
			loginAccount= intent.getExtras().getString("account");//取出key=account的字串設定給loginAccount
			handler.postDelayed(loginStatus, 1000);
			handler.post(getWeatherData);
			handler.post(getHospitalData);
			handler.post(saveOpenData);
//			Log.d(TAG, "SeverGetAccount=>"+loginAccount);
		}
		
		
		
		
		return START_NOT_STICKY; //onStartCommand mode:當main activity呼叫的時候才運行產生Intent 所以不會有null intent
		
	}
	//-------------定時自動更新SQLite裡面loginstatus的時間---------------------------
	Runnable loginStatus =new Runnable() {

		@Override
		public void run() {
			String sysDate=SQLiteWriter.getSystemTime();
			mContRes=getContentResolver();
			String whereClause = "account='" + loginAccount + "'";
			ContentValues newRow = new ContentValues();
			newRow.put("loginDate", sysDate);
			newRow.put("loginStatus", "login");
			SQLiteWriter.sqlUpdate(mContRes,DBContentProvider.CONTENT_URI_loginStatus,MYLOGINSTATUSCOLUMN,newRow,whereClause);
			Toast.makeText(getApplicationContext(), "登入狀態為login", Toast.LENGTH_SHORT).show();
			handler.postDelayed(this, autotime*60*1000);//一小時更新一次
		}
		
	};
	//------------------------------------------------------------
	Runnable getHospitalData=new Runnable() 
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			getHospitalData();
		}
		
	};
	
	Runnable getWeatherData=new Runnable() {
		
		@Override
		public void run() {
			getWeatherData();
			getTaKaChiaData();
		}


	};
	//----------------當所有opendata下載好的時候 將資料存進SQLite------------
	Runnable saveOpenData=new Runnable() {
		
		@Override
		public void run() {//每隔一秒檢查一次OpenData是否loading好了
			if (checkweather==1 && checkbike==1 && checkhos==1)
			{
				saveGetOpenDatatoSQLite();
				checkweather=0; checkbike=0; checkhos=0;
				checkSave=1;				
				handler.removeCallbacks(this);
			}else			
			handler.postDelayed(this,1000);
		}


	};
	//----------------------------------------------
	//--------------開始執行將所有的data存進sqlite------------------------
	private void saveGetOpenDatatoSQLite() 
	{
		mContRes = getContentResolver();
		SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_OpenData, MYCOLUMN_OpenData);
		ContentValues newRow = new ContentValues();	
		newRow.put("weather",weatherJSONarr.toString());
		newRow.put("hospital",hosJSONarr.toString());
		newRow.put("TaKaChia",bikeJSONarr.toString());
		SQLiteWriter.wirteToSQLite(mContRes, DBContentProvider.CONTENT_URI_OpenData, MYCOLUMN_OpenData, newRow);
		Toast.makeText(getBaseContext(),"已經將OpenData存進SQLite",Toast.LENGTH_LONG).show();
	}
	//---------------執行將伺服器裡面 所有醫院的資料下載-----------
	private void getHospitalData() {
	 new Thread(new Runnable() {//開新的緒來下載
		
		@Override
		public void run() {
			hosJSONarr=null;
			try {
				
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("query_string", "hospital"));
				
				String result = DBConnector.executeQuery("import",params);
				 
				String r = result.toString().trim();
				   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
				   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
				
//				   Log.d(TAG, "httpstate="+DBConnector.httpstate );
				   if (DBConnector.httpstate == 200) {					    
//				    Toast.makeText(getBaseContext(), "已經完成由伺服器匯入",
//				      Toast.LENGTH_LONG).show();
//					   decodeHospitalData(result);
					   hosJSONarr=new JSONArray(result);//mySQL回傳的時候已經是JSONarray的格式了 所以就可以直接存了
					   checkhos=1;
					   Log.d(TAG, "醫院loading完畢..");
//					   Toast.makeText(getApplicationContext(), "醫院loading完畢..", Toast.LENGTH_LONG).show();
				    } else {
//				    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
//				      Toast.LENGTH_LONG).show();
				   }				
			}catch (Exception e) {
				Log.d(TAG,"error->"+ e.toString());
			}
			
		}
	}).start();	
		
 }
	/********************************
	 * hospitalData[i][0]=hosName   hospitalData[i][4]=hosTel
	 * hospitalData[i][1]=hosTenure hospitalData[i][5]=hosAddr
	 * hospitalData[i][2]=hosType   hospitalData[i][6]=Lat
	 * hospitalData[i][3]=hosLoc    hospitalData[i][7]=Lng	  
	 *******************************************/
//	private void decodeHospitalData(String result) {
//		
//		
////		hosLat=null;hosLng=null;hosName=null;hosLoc=null;hosTenur=null;hosType=null;hosTel=null;hosAddr=null ;
//		 try {
//			JSONArray jsonArray = new JSONArray(result);
//			
//
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonData = jsonArray.getJSONObject(i);				
//
//
//				// 取出 jsonObject 中的字段的值的空格
////				Iterator itt = jsonData.keys();
////				while (itt.hasNext()) {
////					String key = itt.next().toString();
////					String value = jsonData.getString(key);
////					if (value == null) {
////						continue;
////					} else if ("".equals(value.trim())) {
////						continue;
////					} else {
////						newRow.put(key, value.trim());
////					}
////				}
//
//
//			}
//			
//		} catch (JSONException e) {			
//			e.printStackTrace();
//		}
//	}
	//-------------------從伺服器下載Ubike的資料---------------
	private void getTaKaChiaData() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String result = DBConnector.getTaKaChiaData();
				
				 if (DBConnector.httpstate == 200) {		    

					    deCodeTaKaChiaData(result);//將回傳的資料傳給這個method解析
					    
					    } else {

					   } 	
				
			}
		}).start();
		
	}
	//-------------------開始解析Ubike的資料------------
	/****************************************************
	 * TaKaChiaData[i][0]:sna  TaKaChiaData[i][5]:lat
	 * TaKaChiaData[i][1]:tot  TaKaChiaData[i][6]:lng
	 * TaKaChiaData[i][2]:sbi  TaKaChiaData[i][7]:ar
	 * TaKaChiaData[i][3]:sarea  TaKaChiaData[i][8]:bemp
	 * TaKaChiaData[i][4]:mday  TaKaChiaData[i][9]:act	 
	 *****************************************************/
	private void deCodeTaKaChiaData(String result) {		
		bikeJSONarr=null;

		try {
			JSONObject taKaChiaObj = new JSONObject(result);//因為ubike的資料一開始就是JSONobject
//			Log.d(TAG, "takachia.length->"+taKaChiaObj.length());
			JSONObject takaChiaObjLv2=taKaChiaObj.getJSONObject("retVal");//第2層的key  取出第2層的資料
			Log.d(TAG, "takachiaLv2.length->"+takaChiaObjLv2.length());

		
			bikeJSONarr=new JSONArray();//用來暫存解析完畢的ubike先準備好
//			Log.d(TAG, "takachiaLv2->"+takaChiaObjLv2); 
//			int count=0; count++;
				Iterator<String> itt=takaChiaObjLv2.keys();//用Iterator這object取出所有第2層的KEY
				while (itt.hasNext()) //如果Iterator還有下一個的話  就是true 迴圈會繼續跑
				{
					String key = itt.next().toString();//取出跑到位置的KEY值
					JSONObject takaChiaObjLv3=takaChiaObjLv2.getJSONObject(key);//取出第2層對應KEY的JSONobject
					bikeJSONarr.put(takaChiaObjLv3);//暫存進這個JSONarray					
//					Log.d(TAG, count+"//"+"TaKaChiaData[count][0]->"+TaKaChiaData[count][0]);
					
				}
				checkbike=1;
				 Log.d(TAG, "UBIKE loading完畢..");
//				Toast.makeText(getApplicationContext(), "TaKaChia loading完畢..", Toast.LENGTH_LONG).show();
//				Log.d(TAG, "bikeJSONarr->"+bikeJSONarr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**************************
	 * weatherData[i][0]=TOWN  weatherData[i][4]=TEMP
	 * weatherData[i][1]=loc   weatherData[i][5]=HUMD
	 * weatherData[i][2]=lat   weatherData[i][6]=H_24R
	 * weatherData[i][3]=lon
	 *************************/
	private void getWeatherData() 
	{
		new Thread(new Runnable()
		{
			
			@Override
			public void run() 
			{   weatherJSONarr=null;
				
				try {     //建立一個ArrayList<HashMap<String, Object>>來裝getXmlOpenData()回傳的天氣資料
					final ArrayList<HashMap<String, Object>> arrayList = getXmlOpenData();
					// XML讀取完會得到一個ArrayList

					// runOnUiThread(new Runnable() {
					// @Override
					// public void run() {
					// ----------------------------------開空間給他

					weatherJSONarr = new JSONArray();//開啟一個新的JSONarray

					// --將ArrayList裡面的東西一個一個取出來並重新包裝成JSONobject存進去陣列
//					for (int i = 0; i < arrayList.size(); i++) {
					for (HashMap<String, Object> fld:arrayList) {//用foreach逐筆取出來

//						HashMap<String, Object> fld = arrayList.get(i);

						JSONObject jObj = new JSONObject();// 一定要放在這裡

						try {//開始用JSONobject重新包裝好
							jObj.put("TOWN", fld.get("TOWN").toString());
							jObj.put("loc", fld.get("loc").toString());
							jObj.put("lat", fld.get("lat").toString());
							jObj.put("lon", fld.get("lon").toString());
							jObj.put("TEMP", fld.get("TEMP").toString());
							jObj.put("HUMD", fld.get("HUMD").toString());
							jObj.put("H_24R", fld.get("H_24R").toString());

							weatherJSONarr.put(jObj);//放進JSONarray裡面
						} catch (JSONException e) {							
							e.printStackTrace();
						}
					}
					checkweather=1;
					 Log.d(TAG, "天氣loading完畢..");
//					Toast.makeText(getApplicationContext(), "天氣loading完畢..", Toast.LENGTH_LONG).show();
					// Log.d(TAG, "arrayList=> " +arrayList);
					// }
					// });

				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}).start();		
	}
	
	 //-----------跟天氣的opendata連線的method-------------------------
public InputStream getUrlData(String url) throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI(url));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
// 解析空氣品質的OPEN DATA返回一個ArrayList集合
public ArrayList<HashMap<String, Object>> getXmlOpenData() throws URISyntaxException {
	String tagName = null;
	String tagName1 = null;
	String tagName2 = null;
	String tagName3 = null;
	String tagName4 = null;
	ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
	HashMap<String, Object> hashMap = new HashMap<String, Object>();
	
	Log.d(TAG, "hashMap=> " +hashMap);
	// 記錄出現次數
	int findCount = 0;
	try {
		// 定義工廠 XmlPullParserFactory
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		// 定義解析器 XmlPullParser
		XmlPullParser parser = factory.newPullParser();
		// 獲取xml輸入數據
		parser.setInput(new InputStreamReader(getUrlData("http://opendata.cwb.gov.tw/opendata/DIV2/O-A0001-001.xml")));
//		parser.setInput(new InputStreamReader(getUrlData("http://smilea.zapto.org/three/O-A0001-001.xml")));
//		parser.setInput(new InputStreamReader(getUrlData("file:///android_asset/O-A0001-001.xml")));
		// 開始解析事件
		int eventType = parser.getEventType();
		// 處理事件，不碰到文檔結束就一直處理
		while (eventType != XmlPullParser.END_DOCUMENT) {
			
			// 因為XmlPullParser預先定義了一堆靜態常量，所以這裡可以用switch
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:  //0
//				Log.d(TAG, "XmlPullParser.START_DOCUMENT=> " +XmlPullParser.START_DOCUMENT+ parser.getName() + " , " + parser.getText());
				break;
			case XmlPullParser.START_TAG:  //2
//				Log.d(TAG, "XmlPullParser.START_TAG=> " +XmlPullParser.START_TAG+ parser.getName() + " , " + parser.getText());
				// 給當前標籤起個名字
				tagName = parser.getName();
				// 看到感興趣的標籤個計數
				if (findCount == 0 && tagName.equals("location")) {
					findCount++;
				}
				
				break;
			case XmlPullParser.TEXT:  //4
//				Log.d(TAG, "XmlPullParser.TEXT=> " +XmlPullParser.TEXT+ parser.getName() + " , " + parser.getText());
				if (tagName.equals("lat") && hashMap.containsKey("lat") == false) {//經度
					hashMap.put("lat", parser.getText().toString());
				} else if (tagName.equals("lon") && hashMap.containsKey("lon") == false) {//緯度
					hashMap.put("lon", parser.getText().toString());
				} else if (tagName.equals("locationName") && hashMap.containsKey("loc") == false) {//觀測站名
					hashMap.put("loc", parser.getText().toString());
				} else if (tagName.equals("parameterName") && parser.getText().equals("TOWN")) {//城市
					tagName1 = "TOWN";
				} else if (tagName.equals("parameterValue") && tagName1 == "TOWN" && hashMap.containsKey("TOWN") == false) {
					hashMap.put("TOWN", parser.getText().toString());
					tagName1 = null;
				}else if (tagName.equals("elementName") && parser.getText().equals("TEMP")) {//溫度，單位 攝氏
					tagName2 = "TEMP";
				} else if (tagName.equals("value") && tagName2 == "TEMP" && hashMap.containsKey("TEMP") == false) {
					hashMap.put("TEMP", parser.getText().toString());
					tagName2 = null;
				}else if (tagName.equals("elementName") && parser.getText().equals("HUMD")) {//相對濕度，單位 百分比率
					tagName3 = "HUMD";
				} else if (tagName.equals("value") && tagName3 == "HUMD" && hashMap.containsKey("HUMD") == false) {
					hashMap.put("HUMD", parser.getText().toString());
					tagName3 = null;
				}else if (tagName.equals("elementName") && parser.getText().equals("H_24R")) {//日累積雨量，單位 毫米
					tagName4 = "H_24R";
				} else if (tagName.equals("value") && tagName4 == "H_24R" && hashMap.containsKey("H_24R") == false) {
					hashMap.put("H_24R", parser.getText().toString());
					tagName4 = null;
				}								
				break;
			case XmlPullParser.END_TAG:
//				Log.d(TAG, "XmlPullParser.END_TAG=> " + XmlPullParser.END_TAG+ parser.getName() + " , " + parser.getText());
				// 嘗試取得當前標籤名稱，若是Data才可以增加到arrayList，並且重置
				String trytagName = parser.getName();
				if (trytagName.equals("location")) {
					tagName = parser.getName();
//					Log.d(TAG, "hashMap=> " +hashMap);
					findCount = 0;
					arrayList.add(hashMap);						
				
					hashMap = new HashMap<String, Object>();
					
				}
				break;
			case XmlPullParser.END_DOCUMENT:
				Toast.makeText(getApplicationContext(), "天氣資料下載結束...",Toast.LENGTH_SHORT).show();
				Log.d(TAG, "XmlPullParser.END_DOCUMENT=>" + XmlPullParser.END_DOCUMENT);
				break;
			}
			// 別忘了一定要用next方法處理下一個事件，忘了的結果就成無窮環圈#_#
//			Log.d(TAG, "eventType => " +eventType + parser.getName() + " , " + parser.getText());
			eventType = parser.next();
//			Log.d(TAG, "eventType => " +eventType + parser.getName() + " , " + parser.getText());
		}
		return arrayList;
	} catch (XmlPullParserException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return arrayList;
}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
         handler.removeCallbacks(loginStatus);
		
		Log.d(TAG, "serviceOnDestroy....");
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUNbind....");
		return super.onUnbind(intent);
	}



}
/******
 * hospitalData=null;
 * hospitalData=new String[jsonArray.length()][8];
 * 				hospitalData[i][0]=jsonData.getString("hosName").toString().trim();
				hospitalData[i][1]=jsonData.getString("hosTenure").toString().trim();
				hospitalData[i][2]=jsonData.getString("hosType").toString().trim();
				hospitalData[i][3]=jsonData.getString("hosLoc").toString().trim();
				hospitalData[i][4]=jsonData.getString("hosTel").toString().trim();
				hospitalData[i][5]=jsonData.getString("hosAddr").toString().trim();
				hospitalData[i][6]=jsonData.getString("Lat").toString().trim();
				hospitalData[i][7]=jsonData.getString("Lng").toString().trim();
				
	weatherData=null;
 *  weatherData=new String[arrayList.size()][7];
 *  						TextTOWN=new String[arrayList.size()];
							Textloc=new String[arrayList.size()];
							Textlat=new String[arrayList.size()]; 
							Textlon=new String[arrayList.size()]; 
							TextTEMP=new String[arrayList.size()]; 
							TextHUMD=new String[arrayList.size()]; 
							Text24R=new String[arrayList.size()];
							
								weatherData[i][0]=fld.get("TOWN").toString();
								weatherData[i][1]=fld.get("loc").toString();
								weatherData[i][2]=fld.get("lat").toString();
								weatherData[i][3]=fld.get("lon").toString();
								weatherData[i][4]=fld.get("TEMP").toString();
								weatherData[i][5]=fld.get("HUMD").toString();
								weatherData[i][6]=fld.get("H_24R").toString();
								
								TextTOWN[i]=fld.get("TOWN").toString();
								Textloc[i]=fld.get("loc").toString();
								Textlat[i]=fld.get("lat").toString();
								Textlon[i]=fld.get("lon").toString();
								TextTEMP[i]=fld.get("TEMP").toString();
								TextHUMD[i]=fld.get("HUMD").toString();
								Text24R[i]=fld.get("H_24R").toString();
								
//		bikeSNA=null;biketot=null;bikesbi=null;bikesarea=null;bikemday=null;bikelat=null;
//		bikelng=null;bikear=null;bikebemp=null;bikeact=null; 
								
//			bikeSNA=new String[takaChiaObjLv2.length()];
//			biketot=new String[takaChiaObjLv2.length()];
//			bikesbi=new String[takaChiaObjLv2.length()];
//			bikesarea=new String[takaChiaObjLv2.length()];
//			bikemday=new String[takaChiaObjLv2.length()];
//			bikelat=new String[takaChiaObjLv2.length()];
//			bikelng=new String[takaChiaObjLv2.length()];
//			bikear=new String[takaChiaObjLv2.length()];
//			bikebemp=new String[takaChiaObjLv2.length()];
//			bikeact=new String[takaChiaObjLv2.length()];
 * 					
//					bikeSNA[count]=takaChiaObjLv3.getString("sna").toString().trim();						
//					biketot[count]=takaChiaObjLv3.getString("tot").toString().trim();
//					bikesbi[count]=takaChiaObjLv3.getString("sbi").toString().trim();
//					bikesarea[count]=takaChiaObjLv3.getString("sarea").toString().trim();
//					bikemday[count]=takaChiaObjLv3.getString("mday").toString().trim();
//					bikelat[count]=takaChiaObjLv3.getString("lat").toString().trim();
//					bikelng[count]=takaChiaObjLv3.getString("lng").toString().trim();
//					bikear[count]=takaChiaObjLv3.getString("ar").toString().trim();
//					bikebemp[count]=takaChiaObjLv3.getString("bemp").toString().trim();
//					bikeact[count]=takaChiaObjLv3.getString("act").toString().trim();
 * 
 * TaKaChiaData=null;
 * 	TaKaChiaData=new String[takaChiaObjLv2.length()][10];
 * //					Log.d(TAG, "第"+count+"//"+"takachiaLv3->"+takaChiaObjLv3); 
					TaKaChiaData[count][0]=takaChiaObjLv3.getString("sna").toString().trim();						
					TaKaChiaData[count][1]=takaChiaObjLv3.getString("tot").toString().trim();
					TaKaChiaData[count][2]=takaChiaObjLv3.getString("sbi").toString().trim();
					TaKaChiaData[count][3]=takaChiaObjLv3.getString("sarea").toString().trim();
					TaKaChiaData[count][4]=takaChiaObjLv3.getString("mday").toString().trim();
					TaKaChiaData[count][5]=takaChiaObjLv3.getString("lat").toString().trim();
					TaKaChiaData[count][6]=takaChiaObjLv3.getString("lng").toString().trim();
					TaKaChiaData[count][7]=takaChiaObjLv3.getString("ar").toString().trim();
					TaKaChiaData[count][8]=takaChiaObjLv3.getString("bemp").toString().trim();
					TaKaChiaData[count][9]=takaChiaObjLv3.getString("act").toString().trim();
 * 
 * */
