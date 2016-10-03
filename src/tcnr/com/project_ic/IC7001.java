package tcnr.com.project_ic;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Iterator;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;


/****************************************** 
*登入流程:
*1.當使用者沒有登入過的情況:
*            一樣->登入成功->執行登入程序
*                            ->跳到主選單頁面
*                            ->下載所有成員資料併存進SQLite
*                            ->紀錄帳號登入時間(SQLite)跟登入狀態
*                            ->開啟ICareService->下載openData
*                                              ->定時更新登入時間
*
*            不一樣->請使用者重新輸入
*
*2.使用者登入過(SQLite):
*    取出SQLite裡面紀錄的登入時間->計算時間是否超過120分鐘
*             超過->請使用者重新登入並且把登入狀態改為logout
*             在時間範圍內->執行登入程序
*                            ->跳到主選單頁面
*                            ->下載所有成員資料併存進SQLite
*                            ->紀錄帳號登入時間(SQLite)跟登入狀態
*                            ->開啟ICareService->下載openData
*                                              ->定時更新登入時間
*
*
******************************************************/

public class IC7001 extends Activity {


	//主畫面
	private RelativeLayout icLay7001;//首頁
	private Button icBtn7001,icBtn7002;//登入,註冊按鈕
	private EditText icEdt7001,icEdt7002;//輸入帳號,密碼
	
	// --------------
	private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};
	private String[] MYLOGINSTATUSCOLUMN = new String[]{"id", "loginDate","account","code", "loginStatus" };
	// ----------------
	private static final int loginTime=120;
	String TAG="tcnr6==>";
	public static  String LoginAccount=null;
	
//	public static Intent iCareService; //= new Intent(getBaseContext(), ICareService.class);//用intent連接M1104跟MyService
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ic7001);
//		iCareService = new Intent(getApplicationContext(), ICareService.class);
		
		setupViewComponent();
//        getSysTime();
	}


//	private void getSysTime() {
//		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		 Date curDate = new Date(System.currentTimeMillis()); //  獲取當前時間
//		 String getSysTime = Dateformatter.format(curDate);
//		 Log.d(TAG, "getSysTime=>"+getSysTime);
//		 Long nowTime= System.currentTimeMillis();
//		 Log.d(TAG, "getcurrentTimeMillis()=>"+nowTime);
//		 try {
//			long saveDate = Dateformatter.parse(getSysTime).getTime();
//			Log.d(TAG, "parse_getSystime=>"+saveDate);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

//<!---------------------開始檢查使用者的sqlite裡面是否有登入資料------------------>
	private void u_checkLoginStatus() {
		String loginDate=null,loginStatus=null,loginAccount=null,loginCode=null ;

		long saveDate = 0;
//		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    Long nowTime= System.currentTimeMillis();//讀取現在的系統時間
		 
//		Calendar myCalendar = Calendar.getInstance();
//		int nowMin=myCalendar.get(Calendar.MINUTE);
		
		// -------------------------
		mContRes = getContentResolver();
		// -----------------------------
		
        String[][] getValue=SQLiteWriter.getSQLiteData(mContRes,DBContentProvider.CONTENT_URI_loginStatus,MYLOGINSTATUSCOLUMN);
		if (getValue != null) {//先判斷現在SQLite裡面是否有登入紀錄資料 有->把資料取出來判斷 null->沒登入過
			loginDate = getValue[0][1];//取出上次登入時間 yyyy/MM/dd HH:mm:ss
			loginAccount = getValue[0][2];//登入過的帳號
			loginCode = getValue[0][3];//登入過的密碼
			loginStatus = getValue[0][4];//登入狀態為 login or logout
			// Log.d(TAG, "getDate====>"+loginDate);
			saveDate=SQLiteWriter.parseSaveDate(loginDate);//時間轉換為長整數型態

			Long betweenTime = (nowTime - saveDate) / (1000 * 60);//把現在的時間扣掉SQLite裡面的時間在轉換單位成分鐘
			Log.d(TAG, "betweentime=>" + betweenTime);
//			String[] sysDate=null,sysTime=null;
//			sysDate = loginDate.split("#");
//			sysTime = sysDate[1].split(":");
			if (loginStatus.trim().equals("login")) {//判斷SQLite裡面的帳號現在是否為登入狀態

				if (betweenTime < loginTime) {//判斷登入過的時間跟現在時間的間格是否超過我們設定的時間						
						
//						Intent intent = new Intent();
//						intent.setClass(IC7001.this, IC7004.class);
//						startActivity(intent);
//						Toast.makeText(getApplicationContext(), loginAccount + "歡迎回來!", Toast.LENGTH_LONG).show();

                        u_sendmySQLcheck(loginAccount, loginCode);//假如在時間內的話把sqlite裡面的登入帳號跟密碼送去跟mySQL的做比對

//						IC7001.this.finish();
					

				} else if (betweenTime > loginTime) {//假如超過設定的登入時間
					u_writeLogOutStatus(loginAccount);//更改SQLite裡面的登入狀態為 logout

					Toast.makeText(IC7001.this, "你超過" + loginTime + "分鐘未登入", Toast.LENGTH_LONG).show();
				}
			} else if (loginStatus.trim().equals("logout")) {
				Toast.makeText(IC7001.this, "你還未登入成功,請先登入", Toast.LENGTH_LONG).show();
				// return;
			}
		}else {
			Toast.makeText(IC7001.this, "你還未登入成功,請先登入", Toast.LENGTH_LONG).show();
		}


	}
//-------------------------------------------------------------------------



//<!----------------------更改SQLite裡面的登入狀態-------------------------------->
	private void u_writeLogOutStatus(String loginAccount) {//SQLite
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			// TODO: handle exception
		}
		String sysDate = SQLiteWriter.getSystemTime();//取得現在系統時間的字串yyyy/MM/dd HH:mm:ss 
		// -------------------------
		mContRes = getContentResolver();
		// -----------------------------
//		Cursor c = mContRes.query(DBContentProvider.CONTENT_URI_loginStatus, MYLOGINSTATUSCOLUMN, null, null, null);
//		c.moveToFirst(); // 一定要寫，不然會出錯
        //----SQLite update固定步驟--------
		String whereClause = "account='" + loginAccount + "'";
		ContentValues newRow = new ContentValues();
		newRow.put("loginDate", sysDate);//更改為現在的時間
		newRow.put("loginStatus", "logout");//更改登入狀態為logout
        //呼叫SQLiteWriter裡面的sqlUpdate method去執行更改
		SQLiteWriter.sqlUpdate(mContRes,DBContentProvider.CONTENT_URI_loginStatus,MYLOGINSTATUSCOLUMN,newRow,whereClause);
        //--------------------------------
	}
//<!--------------------------------------------------------------------------->

//	private void u_mySQLimport() {
//		// -------------抓取遠端資料庫設定執行續------------------------------
//		StrictMode.setThreadPolicy(new StrictMode
//				.ThreadPolicy.Builder()
//				.detectDiskReads()
//				.detectDiskWrites()
//				.detectNetwork()
//				.penaltyLog()
//				.build());
//		StrictMode.setVmPolicy(new StrictMode
//				.VmPolicy.Builder()
//				.detectLeakedSqlLiteObjects()
//				.penaltyLog()
//				.penaltyDeath()
//				.build());
//		// ---------------------------------------------------------------------
//
//		// -------------------------
//		mContRes = getContentResolver();
//		//-----------------------------		
//		Cursor c = mContRes.query(DBContentProvider.CONTENT_URI_login, MYCOLUMN, null, null, null);
//		c.moveToFirst(); // 一定要寫，不然會出錯	
//		try {
//			String result = DBConnector.executeQuery("import");
//			/**************************************************************************
//			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
//			 * jsonData = new JSONObject(result);
//			 **************************************************************************/
//			 String r = result.toString().trim();
//			   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
//			   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
//			   Log.d(TAG, "httpstate="+DBConnector.httpstate );
//			   if (DBConnector.httpstate == 200) {
//			    Uri uri = DBContentProvider.CONTENT_URI_login;
//			    mContRes.delete(uri, null, null);
//			    Toast.makeText(getBaseContext(), "已經完成由伺服器會入資料",
//			      Toast.LENGTH_LONG).show();
//			    } else {
//			    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
//			      Toast.LENGTH_LONG).show();
//			   }
//			
//			JSONArray jsonArray = new JSONArray(result);
//			// ---
//			Log.d(TAG, jsonArray.toString());
//			if(jsonArray.length()>0){
//				Uri uri = DBContentProvider.CONTENT_URI_login;
//				mContRes.delete(uri, null, null); // 刪除所有資料	
//			}
//			
//			ContentValues newRow = new ContentValues();
//			
//			
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject jsonData = jsonArray.getJSONObject(i);
//          
//			  // 取出 jsonObject 中的字段的值的空格
//				Iterator itt = jsonData.keys();
//				while (itt.hasNext()) {
//					String key = itt.next().toString();
//					String value = jsonData.getString(key);
//					if (value == null) {
//						continue;
//					} else if ("".equals(value.trim())) {
//						continue;
//					} else {
//						newRow.put(key, value.trim());
//					}
//					
//				}
//				
//				mContRes.insert(DBContentProvider.CONTENT_URI_login, newRow);				
//				
//			}
//
//		} catch (Exception e) {
//			Log.d(TAG,"error->"+ e.toString());
//		}
//		c.close();
//		
//	}
//<!-------------------各種findviewbyid--------------------->
	private void setupViewComponent() {
		// TODO Auto-generated method stub
		icLay7001=(RelativeLayout)findViewById(R.id.icLay7001);
		icLay7001.setBackgroundResource(R.raw.iclay7001);
		
		icBtn7001=(Button)findViewById(R.id.icBtn7001);
		icBtn7002=(Button)findViewById(R.id.icBtn7002);
		icEdt7001=(EditText)findViewById(R.id.icEdt7001);//帳號
		icEdt7002=(EditText)findViewById(R.id.icEdt7002);//密碼
		
		icBtn7001.setOnClickListener(icBtn700On);
		icBtn7002.setOnClickListener(icBtn700On);
		

	}

//<!------------------------當使用者手動登入的時候------------------->
	private Button.OnClickListener icBtn700On=new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent=new Intent();
			switch(v.getId()){
			case R.id.icBtn7001://按下登入按鈕,跳轉至登入畫面
//				//取得使用者輸入的帳號跟密碼
				String account=icEdt7001.getText().toString().trim();
				String inputcode=icEdt7002.getText().toString().trim();
				String DBcode=null;
				if(account.equals("")||inputcode.equals("")){//判斷是否有欄位輸入空的
					Toast.makeText(IC7001.this,"有空欄位沒填喔!!請重新填寫..", Toast.LENGTH_LONG).show();
					return;
				}else if(!account.equals("")&&!inputcode.equals("")){//使用者 帳號跟密碼都有輸入的時候
					//------將輸入的密碼轉成md5格式-------
					String md5Code=null;
					try {
						 md5Code=md5(inputcode);
					} catch (NoSuchAlgorithmException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//--------------
					u_sendmySQLcheck(account,md5Code);//將使用者輸入的帳號跟密碼送去給u_sendmySQLcheck
					
				}

				break;
			case R.id.icBtn7002://按下註冊按鈕,跳轉至註冊頁面
				intent.setClass(IC7001.this, IC7002.class);
				startActivity(intent);				
				break;
			}
		}
		
	};
	//<!---------------------確認mySQL裡面的帳號跟密碼----------------------------------------->
	private void u_sendmySQLcheck(String account, String inputcode) {

		//將得到的帳號傳去給php 然後php會回傳mySQL裡面的密碼
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		  nameValuePairs.add(new BasicNameValuePair("query_string", "check"));
		  nameValuePairs.add(new BasicNameValuePair("account", account));

		  try {
		   Thread.sleep(500); //  延遲Thread 睡眠0.5秒
		  } catch (InterruptedException e) {
		   e.printStackTrace();
		  }
		  String value=null,result=null;
		  try {   //"check"目前沒有用 主要是把nameValuePairs傳過去
			  result = DBConnector.executeCheck("check", nameValuePairs);
			  Log.d(TAG, "DBresult==>"+result);
			  if(result.equals("查無帳號")){//沒有回傳密碼  表示找不到傳入帳號
					Toast.makeText(IC7001.this, "查無帳號,請重新輸入...", Toast.LENGTH_LONG).show();
					icEdt7001.setText("");
					icEdt7002.setText("");
					return; 
			  }else {//有找到傳入帳號的密碼
				  JSONArray jsonArray = new JSONArray(result);
//				 
					  JSONObject jsonData = jsonArray.getJSONObject(0);
					  Log.d(TAG, "jsonData==>"+jsonData.toString());
					  value = jsonData.getString("code");//將密碼取出來

						Log.d(TAG, "DBvalue==>"+value);
//				
				  if(value.trim().equals(inputcode)){//假如回傳的密碼跟輸入的密碼一樣的時候
						Toast.makeText(getApplicationContext(),account+"歡迎回來!", Toast.LENGTH_LONG).show();
						icEdt7001.setText("");
						icEdt7002.setText("");
						/***************************************
						 * 這裡可以插入動畫因為登入成功要跳了       *
						 ***************************************/
						
						Intent intent=new Intent();
						intent.setClass(IC7001.this, IC7004.class);
						startActivity(intent);
						
					  improtAll_SQLData();//下載所有成員的資料
					  u_writeLoginStatus(account,value);//將帳號跟密碼寫進sqlite 的loginstatus這張表裡面
					  
					  IC7001.this.finish();
				  }else if(!value.trim().equals(inputcode)){
						Toast.makeText(IC7001.this, "密碼輸入錯誤,請重新輸入...", Toast.LENGTH_LONG).show();
						icEdt7001.setText("");
						icEdt7002.setText("");
						return;
				  }
			  }
			
		} catch (Exception e) {
			e.getStackTrace();
		}			
		 
	}
	//<!-------------------下載所有成員的資料--------------------------------------------->
	private void improtAll_SQLData() {
		try {
			Thread.sleep(2000);
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("query_string", "import"));
			
			String result = DBConnector.executeQuery("import",params);
			/**************************************************************************
			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
			 * jsonData = new JSONObject(result);
			 **************************************************************************/
			 
			String r = result.toString().trim();
			   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
			   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
			
			   Log.d(TAG, "httpstate="+DBConnector.httpstate );
			   if (DBConnector.httpstate == 200) {
			    
			    Toast.makeText(getBaseContext(), "已經完成由伺服器匯入資料",
			      Toast.LENGTH_LONG).show();
			    u_writeLoginDataToSQLite(result);//將回傳的資料傳入u_writeLoginDataToSQLite method寫入sqlite
			    
			    } else {
			    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
			      Toast.LENGTH_LONG).show();
			   }	  		
	
			
		}catch (Exception e) {
			Log.d(TAG,"error->"+ e.toString());
		}
		
	}
	private void u_writeLoginDataToSQLite(String result) {
		// -------------------------
		mContRes = getContentResolver();
		//-----------------------------		
//		Cursor c = mContRes.query(DBContentProvider.CONTENT_URI_login, MYCOLUMN, null, null, null);
//		c.moveToFirst(); // 一定要寫，不然會出錯	
		 try {
			JSONArray jsonArray = new JSONArray(result);//因為php回傳的是JSONarray所以用JSONarray裝
			
			if (jsonArray.length() > 0) {//裡面有資料的話 先清空SQLite裡面的資料 避免重複寫入

				SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN);
			}
			
			ContentValues newRow = new ContentValues();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonData = jsonArray.getJSONObject(i);//逐筆取出array裡面的資料

				// 取出 jsonObject 中的字段的值的空格
				Iterator itt = jsonData.keys();//先將JSONobject裡面所有的key都取出來
				while (itt.hasNext()) {//假如key還有下一個的話 就繼續做
					String key = itt.next().toString();//將key取出來
					String value = jsonData.getString(key);//取出相對應key的值
					if (value == null) {//假如取道空值就跳過繼續做下一個
						continue;
					} else if ("".equals(value.trim())) {//假如取道空白值就跳過繼續做下一個
						continue;
					} else {
						newRow.put(key, value.trim());//放入key and value
					}

				}

				SQLiteWriter.wirteToSQLite(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN, newRow);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//<!-------------------更改SQLite裡面登入狀態為"login"的method------------------->
	private void u_writeLoginStatus(String account, String code) {
		String sysDate=SQLiteWriter.getSystemTime();
		// -------------------------
		mContRes = getContentResolver();
		//-----------delete------------------			
	
		SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_loginStatus, MYLOGINSTATUSCOLUMN);
		// --------insert SQLite-------------------------------------------
		ContentValues newRow = new ContentValues();
		newRow.put("loginDate", sysDate);
		newRow.put("account", account);
		newRow.put("code", code);
		newRow.put("loginStatus", "login");

		SQLiteWriter.wirteToSQLite(mContRes,DBContentProvider.CONTENT_URI_loginStatus,MYLOGINSTATUSCOLUMN,newRow);
		// --------------------------------------------------------
		sendToService(account);
		LoginAccount=account;
	}
	//<!--------------------------------------------------->
	//<!--------------開啟ICareService的method----------------------------->
	private void sendToService(String loginAccount) {
		Intent iCareService = new Intent(getBaseContext(), ICareService.class);//用intent連接M1104跟MyService
		Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
		bundle.putString("action", "login");
		bundle.putString("account", loginAccount);//傳送變數 key:account value:loginAccount 放進Bundle裡面
		iCareService .putExtras(bundle);//用intent傳送Bundle
		startService(iCareService);//參考intent開啟服務;這裡是MyService
	
}

//<!------------生命週期---------------------->
	@Override
	protected void onStart() {
		u_checkLoginStatus();
		super.onStart();
	}
	@Override
	protected void onResume() {
		//u_checkLoginStatus();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		/***************************************
		 * 這裡可以插入動畫因為登入成功要跳了       *
		 ***************************************/
		
		// TODO Auto-generated method stub
//		stopService(iCareService);
		super.onDestroy();
	}
//------------------------------------------
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ic7000, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case R.id.iccareweb://與官網連結
			break;
		case R.id.mail ://連絡我們					
			break;
		case R.id.action_finish ://結束
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
   
    //密码加密 与php加密一致
	public static String md5(String input) throws NoSuchAlgorithmException {
		String result = input;
		if (input != null) {
			MessageDigest md = MessageDigest.getInstance("MD5"); // or "SHA-1"
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
		}
		return result;
	}
    

}
