package tcnr.com.project_ic;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import static tcnr.com.project_ic.IC7001.LoginAccount;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;


import static tcnr.com.project_ic.CommonUtilities.SENDER_ID;
/*********************************************
*註冊流程:
*1.使用者輸入完資料->將資料送至mySQL插入->判斷php回傳的flag
*                      ->失敗->使用者重新輸入
*                      ->成功->執行登入程序
*                                ->下載opendata
*                                ->下載成員資料
*                                ->開啟自動更新登入時間
*                                ->轉跳到IC7003
*
*
*
*
***********************************************/


public class IC7002 extends Activity {
	private EditText icEdt7003,icEdt7004,icEdt7005,icEdt7006;//帳號,密碼,電話,電郵
	private Button icBtn7003;//註冊按鈕
	protected static final int BUTTON_POSITIVE=-1;//對話框,是=-1,右邊
	protected static final int BUTTON_NEGATIVE=-2;//對話框,取消=-2,左邊
	
	// --------------
	private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};
	private String[] MYLOGINSTATUSCOLUMN = new String[]{"id", "loginDate","account","code", "loginStatus" };

	// ----------------
	private String TAG="tcnr6==>",showip=null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ic7002);
		setupViewComponent();
		 u_registerGCM();
	}
//----------先取得google裡面是否有這台手機的註冊ID  沒有的話  註冊一個----------------
	private void u_registerGCM() {
		// TODO Auto-generated method stub
		final String regId = GCMRegistrar.getRegistrationId(IC7002.this);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, SENDER_ID);
		}
	}
//------------------------------------------
	private void setupViewComponent() {
		
		icEdt7003=(EditText)findViewById(R.id.icEdt7003);
		icEdt7004=(EditText)findViewById(R.id.icEdt7004);
		icEdt7005=(EditText)findViewById(R.id.icEdt7005);
		icEdt7006=(EditText)findViewById(R.id.icEdt7006);
		
		icBtn7003=(Button)findViewById(R.id.icBtn7003);
		icBtn7003.setOnClickListener(icBtn700On);
		
		// ----------------------------------------------------
		mContRes = getContentResolver();
		// ----------------------------------------------------
		showip = NetwordDetect();
	
	}
	//------------------取得手機的ip----
	private String NetwordDetect() {
		 ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		 WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		 String IPaddress = Finduserip.NetwordDetect(CM, wm);
		 return IPaddress;
	}
	//註冊按鈕的監聽
	private Button.OnClickListener icBtn700On=new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			String sysDate=SQLiteWriter.getSystemTime();
			String account,code,phone,mail;
          //---------------取得使用者輸入的 資料並檢查是否有空白--------
			account=icEdt7003.getText().toString().trim();
			code=icEdt7004.getText().toString().trim();
			phone=icEdt7005.getText().toString().trim();
			mail=icEdt7006.getText().toString().trim();
			if (account.equals("") || code.equals("")||phone.equals("")||mail.equals("")) {
				Toast.makeText(IC7002.this,"有空白欄位還沒填喔",Toast.LENGTH_SHORT).show();
			    return;
			}

		 //--------------------------------------------------------
			//如果輸入法打開則關閉，如果沒打開則打開
			InputMethodManager m=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			
			mySQL_insert(account,code,phone,mail);//insert mySQL資料上傳
				
			
			icEdt7003.setText("");
			icEdt7004.setText("");
			icEdt7005.setText("");
			icEdt7006.setText("");
			

		}

		private void mySQL_insert(String account, String code, String phone, String mail) {
			String sysDate=SQLiteWriter.getSystemTime();
			final String regId = GCMRegistrar.getRegistrationId(IC7002.this);//取得這手機的註冊ID
			Log.d(TAG, "reg_id=>"+regId);
			
             //----------將取得的資料對應欄位上傳到mySQL-----
			 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			  nameValuePairs.add(new BasicNameValuePair("query_string", "insert"));
			  nameValuePairs.add(new BasicNameValuePair("account", account));
			  nameValuePairs.add(new BasicNameValuePair("code", code));//這邊因為php那邊有加密的方法 所以直接傳輸入的就好
			  nameValuePairs.add(new BasicNameValuePair("phone", phone));
			  nameValuePairs.add(new BasicNameValuePair("mail", mail));
			  nameValuePairs.add(new BasicNameValuePair("reg_id", regId));
			  nameValuePairs.add(new BasicNameValuePair("ip", showip));

			  String result = DBConnector.executeInsert("insert", nameValuePairs);//取得mySQL的回傳直 判斷是否插入成功
			  Log.d(TAG, "getLoginResult=>"+result);
			  
            if(result.trim().equals("OK")){
            	//------將輸入密碼轉成md5--------------
            	String md5code=null;
            	try {
					md5code=md5(code);//要先將使用者輸入的密碼轉成md5編碼才能寫進SQLite
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	//-----------------------------------------
            	u_registerDialog();//開啟dialog
            	sendToService(account);//開啟service
            	
//            	insertSQLiteLogin(account,md5code,regId);//寫入SQLite的login資料表//現在改成IC7003整批匯入
            	
            	
//            	LoginAccount=account;//設定登入帳號為註冊帳號 現在直接重SQLite裡面撈
            	//把登入狀態寫進SQLite
    			SQLiteWriter.sqlDelete(mContRes,DBContentProvider.CONTENT_URI_loginStatus, MYLOGINSTATUSCOLUMN);// 刪除所有資料

              //--------insert SQLite-------------------------------------------
    			ContentValues newRow=new ContentValues();
    			newRow.put("loginDate", sysDate);
    			newRow.put("account", account);
    			newRow.put("code", md5code);
    			newRow.put("loginStatus", "login");
    			SQLiteWriter.wirteToSQLite(mContRes,DBContentProvider.CONTENT_URI_loginStatus, MYLOGINSTATUSCOLUMN, newRow);
    		  //----------------------------------------------------------
            }else {
				Toast.makeText(IC7002.this,"帳號重複,請重新註冊", Toast.LENGTH_SHORT).show();
				return;
			}		  			  
			 
		}

//		private void insertSQLiteLogin(String account, String code, String regId) {
//			// private String[] MYCOLUMN = new String[] { "id", "account",
//			// "code","reg_id","user_ip","account_status"};
//			
//			// ----------------------------------------------------
//			mContRes = getContentResolver();
//			// ----------------------------------------------------
//			SQLiteWriter.sqlDelete(mContRes,DBContentProvider.CONTENT_URI_login, MYCOLUMN);
//			// --------insert SQLite-------------------------------------------
//			ContentValues newRow = new ContentValues();
//			
//			newRow.put("account", account);
//			newRow.put("code", code);
//			newRow.put("reg_id", regId);
//			newRow.put("user_ip", showip);
//			newRow.put("account_status", "1");
//			SQLiteWriter.wirteToSQLite(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN, newRow);
//			sendToService(account);
//		}
		//這要改到IC7003開啟service 這裡直接開
		private void sendToService(String loginAccount) {
			Intent iCareService = new Intent(getBaseContext(), ICareService.class);//用intent連接M1104跟MyService
			Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
			bundle.putString("action", "login");
			bundle.putString("account", loginAccount);//傳送變數 String action="showtime"放進Bundle裡面
			iCareService .putExtras(bundle);//用intent傳送Bundle
			startService(iCareService);//參考intent開啟服務;這裡是MyService
		
	}

		private void u_registerDialog() {
			// TODO Auto-generated method stub
			//彈出對話框
			MyAlertDialog myAltDlg = new  MyAlertDialog(IC7002.this);
			myAltDlg.setTitle("註冊訊息");//為對話框設置標題
			myAltDlg.setMessage("恭禧你完成個人資料設定,請繼續執行設備設定");//為對話框設置內容
			myAltDlg.setIcon(R.raw.logo);//為對話框設置圖標
			myAltDlg.setCancelable(false);
			myAltDlg.setButton(DialogInterface.BUTTON_POSITIVE,"是",dlon);//-1左邊
			myAltDlg.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",dlon);//-2右邊		
			myAltDlg.show();
			
		}
		
	};
	//對話框的監聽
	private DialogInterface.OnClickListener dlon =new DialogInterface.OnClickListener(){//按下彈出視窗按鈕的監聽動作

		@Override
		public void onClick(DialogInterface dialog, int id) {
			// TODO Auto-generated method stub
			switch(id){
			case BUTTON_POSITIVE://按下'是',跳轉至設備設定
				Intent intent=new Intent();
				intent.setClass(IC7002.this, IC7003.class);
				startActivity(intent);
				IC7002.this.finish();
				break;
			case BUTTON_NEGATIVE://按下'取消',彈出訊息
				Toast.makeText(getApplicationContext(), "你尚未註冊成功", Toast.LENGTH_LONG).show();
				break;
				
			}
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
//		getMenuInflater().inflate(R.menu.ic7001, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
//		switch (id) {
//		case R.id.action_finish://結束
//			finish();
//			break;		
//		}
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
