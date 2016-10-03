package tcnr.com.project_ic;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;
import tcnr.com.projectic.MjpegActivity;


public class IC7004 extends Activity {



	private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};
	private String[] MYLOGINSTATUSCOLUMN = new String[]{"id", "loginDate","account","code", "loginStatus" };
	private String[] MYMSGCOLUMN = new String[] {"id","msgDate","GCMmsg" };
	private String[] MYCOLUMN_OpenData = new String[] {"id","weather","hospital","TaKaChia" };
	
	private Button icBtn7008,icBtn7009,icBtn7010;//位置追踨,監控畫面,生理紀錄
	
	private Handler handler=new Handler();
	private Dialog picture, editCode;
	String[][] getOpenData;
	
	String TAG="tcnr6==>";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContRes=getContentResolver();
		setContentView(R.layout.ic7004);
		setupViewComponent();
		picture=new Dialog(IC7004.this);
		picture.setContentView(R.layout.progress);	
		
		 getOpenData=SQLiteWriter.getSQLiteData(mContRes,DBContentProvider.CONTENT_URI_OpenData , MYCOLUMN_OpenData);
//		sendToService(IC7001.LoginAccount);
		
         //開始檢查ICareService.checkSave是否=0
		 if (getOpenData==null||ICareService.checkSave==0) {
			handler.post(startProgress);
			handler.post(stopProgress);
		 }


		

	}
	Runnable startProgress=new Runnable() {//開啟dialog
		
		@Override
		public void run() {
			
				 picture.setTitle("資料Loading中...");			 		   
				 picture.setCancelable(false);			 
				 picture.show();
			
		}
	};
	Runnable stopProgress=new Runnable() {//每隔1秒檢查一次
		
		@Override
		public void run() {
			if (ICareService.checkSave==1) {
				picture.cancel();
				handler.removeCallbacks(this);
			}else
			handler.postDelayed(this,1000);
			
		}
	};
	
//	private void improtAll_SQLData() {
//		try {
//			Thread.sleep(2000);
//			
//			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//			params.add(new BasicNameValuePair("query_string", "import"));
//			
//			String result = DBConnector.executeQuery("import",params);
//			/**************************************************************************
//			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
//			 * jsonData = new JSONObject(result);
//			 **************************************************************************/
//			 
//			String r = result.toString().trim();
//			   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
//			   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
//			
//			   Log.d(TAG, "httpstate="+DBConnector.httpstate );
//			   if (DBConnector.httpstate == 200) {
//			    
//			    Toast.makeText(getBaseContext(), "已經完成由伺服器會入資料",
//			      Toast.LENGTH_LONG).show();
//			    u_writeLoginData(result);
//			    
//			    } else {
//			    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
//			      Toast.LENGTH_LONG).show();
//			   }	  		
//	
//			
//		}catch (Exception e) {
//			Log.d(TAG,"error->"+ e.toString());
//		}
//		
//	}
//	private void u_writeLoginData(String result) {
//		// -------------------------
//		mContRes = getContentResolver();
//		//-----------------------------		
////		Cursor c = mContRes.query(DBContentProvider.CONTENT_URI_login, MYCOLUMN, null, null, null);
////		c.moveToFirst(); // 一定要寫，不然會出錯	
//
//		 try {
//			 
//			JSONArray jsonArray = new JSONArray(result);
//			
//			if (jsonArray.length() > 0) {
//
//				SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN);
//				
//				ContentValues newRow = new ContentValues();
//				for (int i = 0; i < jsonArray.length(); i++) {
//					JSONObject jsonData = jsonArray.getJSONObject(i);
//
//					// 取出 jsonObject 中的字段的值的空格
//					Iterator itt = jsonData.keys();
//					while (itt.hasNext()) {
//						String key = itt.next().toString();
//						String value = jsonData.getString(key);
//						if (value == null) {
//							continue;
//						} else if ("".equals(value.trim())) {
//							continue;
//						} else {
//							newRow.put(key, value.trim());
//						}
//
//					}
//
//					SQLiteWriter.wirteToSQLite(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN, newRow);
//				}
//				
//			}
//			
//
//			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	private void sendToService(String loginAccount) {
//		Intent iCareService = new Intent(getBaseContext(), ICareService.class);//用intent連接M1104跟MyService
//		Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
//		bundle.putString("account", loginAccount);//傳送變數 String action="showtime"放進Bundle裡面
//		iCareService .putExtras(bundle);//用intent傳送Bundle
//		startService(iCareService);//參考intent開啟服務;這裡是MyService
//	
//}

	private void setupViewComponent() {
		// TODO Auto-generated method stub
		icBtn7008=(Button)findViewById(R.id.icBtn7008);
		icBtn7009=(Button)findViewById(R.id.icBtn7009);
		icBtn7010=(Button)findViewById(R.id.icBtn7010);
		
		icBtn7008.setOnClickListener(icBtn700On);
		icBtn7009.setOnClickListener(icBtn700On);
		icBtn7010.setOnClickListener(icBtn700On);
	}
	
	private Button.OnClickListener icBtn700On=new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it=new Intent();
			switch(v.getId()){
			case R.id.icBtn7008://按下'位置追踨'按鈕
				it.setClass(IC7004.this, IC4000track.class);
				startActivity(it);
				break;
			case R.id.icBtn7009://按下'監控畫面'按鈕
				it.setClass(IC7004.this, MjpegActivity.class);
				startActivity(it);
				break;
			case R.id.icBtn7010://按下'生理紀錄'按鈕
				it.setClass(IC7004.this, IC4000.class);
				startActivity(it);
				break;
			
			}
		}
		
	};
	
	private void u_logOutDialog() {
		// TODO Auto-generated method stub
		//彈出對話框
		MyAlertDialog myAltDlg = new  MyAlertDialog(IC7004.this);
		myAltDlg.setTitle("登出系統?");//為對話框設置標題
		myAltDlg.setMessage("確定要登出系統?所有的訊息將會刪除...");//為對話框設置內容
		myAltDlg.setIcon(R.raw.logo);//為對話框設置圖標
		myAltDlg.setCancelable(false);
		myAltDlg.setButton(DialogInterface.BUTTON_POSITIVE,"是",dlon);//-1左邊
		myAltDlg.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",dlon);//-2右邊		
		myAltDlg.show();
		
	}
	protected static final int BUTTON_POSITIVE=-1;//對話框,是=-1,右邊
	protected static final int BUTTON_NEGATIVE=-2;//對話框,取消=-2,左邊
	//對話框的監聽
	private DialogInterface.OnClickListener dlon =new DialogInterface.OnClickListener(){//按下彈出視窗按鈕的監聽動作

		@Override
		public void onClick(DialogInterface dialog, int id) {
			// TODO Auto-generated method stub
			switch(id){
			case BUTTON_POSITIVE://按下'是'
				// -------------------------
				mContRes = getContentResolver();
				//-----------------------------	
				SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN);
				SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_loginStatus, MYLOGINSTATUSCOLUMN);
				SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_GCMmsg, MYMSGCOLUMN);
				u_logInDialog();
                ICareService.checkSave=0;
                Intent iCareService= new Intent(getBaseContext(), ICareService.class);
                stopService(iCareService);

				break;
			case BUTTON_NEGATIVE://按下'取消',彈出訊息
				Toast.makeText(getApplicationContext(), "你取消登出...", Toast.LENGTH_LONG).show();
				break;
				
			}
		}
//		Intent intent=new Intent();
//		intent.setClass(IC7004.this, IC7003.class);
//		startActivity(intent);
//		IC7004.this.finish();
	};
	private void u_logInDialog() {
		// TODO Auto-generated method stub
		//彈出對話框
		MyAlertDialog myAltDlg = new  MyAlertDialog(IC7004.this);
		myAltDlg.setTitle("登入系統?");//為對話框設置標題
		myAltDlg.setMessage("您目前已經登出系統,是否重新登入?\n(系統未登入使用功能將受到限制..)");//為對話框設置內容
		myAltDlg.setIcon(R.raw.logo);//為對話框設置圖標
		myAltDlg.setCancelable(false);
		myAltDlg.setButton(DialogInterface.BUTTON_POSITIVE,"是",login);//-1左邊
		myAltDlg.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",login);//-2右邊		
		myAltDlg.show();
		
	}
	private DialogInterface.OnClickListener login =new DialogInterface.OnClickListener(){//按下彈出視窗按鈕的監聽動作

		@Override
		public void onClick(DialogInterface dialog, int id) {
			// TODO Auto-generated method stub
			switch(id){
			case BUTTON_POSITIVE://按下'是' 跳回登入頁面
				Intent intent=new Intent();
				intent.setClass(IC7004.this, IC7001.class);
				startActivity(intent);
				IC7004.this.finish();
				break;
			case BUTTON_NEGATIVE://按下'取消',彈出訊息
				Toast.makeText(getApplicationContext(), "你將以訪客身分使用本系統...", Toast.LENGTH_LONG).show();
				break;
				
			}
		}

	};
	

	@Override
	protected void onStart() {
//		improtAll_SQLData();
		super.onStart();
	}

	@Override
	protected void onDestroy() {
//		stopService(IC7001.iCareService);
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.ic7001, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		mContRes = getContentResolver();
		String[][] getAccount = SQLiteWriter.getSQLiteData(mContRes, DBContentProvider.CONTENT_URI_loginStatus,
				MYLOGINSTATUSCOLUMN);
		int id = item.getItemId();
		switch (id) {
		case R.id.action_finish://結束
			finish();
			break;	
		case R.id.icGcm7101:
			Intent it=new Intent();
			it.setClass(IC7004.this, IC4000gcm.class);
			startActivity(it);
			break;
		case R.id.icLogOut7102:			
			if(getAccount!=null)
			u_logOutDialog();
			else
			u_logInDialog();
			
			break;
		case R.id.icEditCode7103:

			if(getAccount!=null)
			{
				u_editCode();
			}else {
				Toast.makeText(IC7004.this,"請先登入才能使用本功能...", Toast.LENGTH_LONG).show();
			}			
			
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private EditText icEdiTex4501,icEdiTex4502;
    private String inputCode;
	private void u_editCode() {//設定修改密碼dialog
		editCode = new Dialog(IC7004.this);
		editCode.setContentView(R.layout.ic4000changecode);
		editCode.setTitle("請修改您的密碼...");

		editCode.setCancelable(false);
		editCode.show();

		icEdiTex4501 = (EditText) editCode.findViewById(R.id.icEdiTex4501);
		icEdiTex4502 = (EditText) editCode.findViewById(R.id.icEdiTex4502);
		icEdiTex4501.setText("");
		icEdiTex4501.setHint("請在此輸入密碼...");
		icEdiTex4502.setText("");
		icEdiTex4502.setHint("請再次輸入密碼...");
		Button icBtn4503 = (Button) editCode.findViewById(R.id.icBtn4503);// 確認
		Button icBtn4504 = (Button) editCode.findViewById(R.id.icBtn4504);// 放棄

		icBtn4503.setOnClickListener(editOn);
		icBtn4504.setOnClickListener(editOn);
	}
	
	private Button.OnClickListener editOn=new Button.OnClickListener()
	{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.icBtn4503:
				checkChangeCode();

				break;

			case R.id.icBtn4504:
				editCode.cancel();
				break;
			}

		}
	};
	// EditText icEdiTex4501,icEdiTex4502
	private void checkChangeCode() {
		 inputCode=null;
		 inputCode = icEdiTex4501.getText().toString().trim();
		String confirmCode = icEdiTex4502.getText().toString().trim();

		if (inputCode.equals(confirmCode)) //判斷輸入密碼跟確認密碼一致
		{
			checkUpdateDialog();
			editCode.cancel();
		}else if (!inputCode.equals(confirmCode))
		{
			icEdiTex4501.setText("");
			icEdiTex4501.setHint("請在此輸入密碼...");
			icEdiTex4502.setText("");
			icEdiTex4502.setHint("請再次輸入密碼...");
			Toast.makeText(IC7004.this, "請輸入一樣的密碼...",Toast.LENGTH_LONG).show();
			return;
		}

	}
	private void checkUpdateDialog() {//再次跳出dialog告訴使用者訊息
		MyAlertDialog myAltDlg = new  MyAlertDialog(IC7004.this);
		myAltDlg.setTitle("確認修改?");//為對話框設置標題
		myAltDlg.setMessage("即將修改您的密碼,請確認是否修改?\n(修改完成後,上一個密碼將無法使用...)");//為對話框設置內容
		myAltDlg.setIcon(R.raw.logo);//為對話框設置圖標
		myAltDlg.setCancelable(false);
		myAltDlg.setButton(DialogInterface.BUTTON_POSITIVE,"是",updatecode);//-1左邊
		myAltDlg.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",updatecode);//-2右邊		
		myAltDlg.show();

	}
	
	private DialogInterface.OnClickListener updatecode =new DialogInterface.OnClickListener()//按下彈出視窗按鈕的監聽動作
			{

				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					switch(which){
					case BUTTON_POSITIVE://按下'是'
						if (inputCode!=null) {
							sendSQLupdateCode(inputCode);//確認之後把使用者輸入的密碼傳給mySQL修改
//							Toast.makeText(IC7004.this, "修改密碼中,請稍後...",Toast.LENGTH_LONG).show();
						}else if(inputCode==null) {
							Toast.makeText(IC7004.this, "請重新輸入....",Toast.LENGTH_LONG).show();
							u_editCode();
						}


						break;
					case BUTTON_NEGATIVE://按下'取消',彈出訊息
						
						break;
						
					}
					
				}
		
			};

	private void sendSQLupdateCode(String inputCode) {
		mContRes = getContentResolver();
		String[][] getAccount = SQLiteWriter.getSQLiteData(mContRes, DBContentProvider.CONTENT_URI_loginStatus,
				MYLOGINSTATUSCOLUMN);//取出loginStatus資料表裡面的帳號
		//-------------------傳到mySQL的php來修改-----------
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		  nameValuePairs.add(new BasicNameValuePair("query_string", "update"));
		  nameValuePairs.add(new BasicNameValuePair("account", getAccount[0][2]));
		  nameValuePairs.add(new BasicNameValuePair("code", inputCode));
		  
		  String result=DBConnector.executeUpdate("update", nameValuePairs);//php會回傳flag判斷是否OK
		  
		  if (result.equals("OK"))//mySQL修改OK 
		  {
			  updateSQLiteCode(getAccount[0][2], inputCode);//開始修改SQLite裡面的資料
			Toast.makeText(IC7004.this,"修改密碼成功...",Toast.LENGTH_SHORT).show();
		  }else {
			Toast.makeText(IC7004.this,"修改密碼失敗...",Toast.LENGTH_SHORT).show();
		  }

	}
	
	private void updateSQLiteCode(String updateAccount,String inputCode) {
		mContRes=getContentResolver();
		String md5code=null;
		String whereClause = "account='" + updateAccount + "'";//用帳號當條件來尋找
		try {
			 md5code=SQLiteWriter.md5(inputCode);//將密碼用md5加密
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ContentValues newRow = new ContentValues();		
		newRow.put("code", md5code);//包裝密碼
		//--------------開始修改
		SQLiteWriter.sqlUpdate(mContRes,DBContentProvider.CONTENT_URI_loginStatus,MYLOGINSTATUSCOLUMN,newRow,whereClause);
		SQLiteWriter.sqlUpdate(mContRes,DBContentProvider.CONTENT_URI_login,MYCOLUMN,newRow,whereClause);
		
	}

}
