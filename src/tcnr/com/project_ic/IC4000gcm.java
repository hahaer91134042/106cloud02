/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tcnr.com.project_ic;

import static tcnr.com.project_ic.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static tcnr.com.project_ic.CommonUtilities.EXTRA_MESSAGE;
import static tcnr.com.project_ic.CommonUtilities.SENDER_ID;
import static tcnr.com.project_ic.CommonUtilities.SERVER_URL;
import static tcnr.com.project_ic.CommonUtilities.API_Key;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;


/**
 * Main UI for the demo app.
 */
public class IC4000gcm extends Activity {


	private TextView icTexVie4200,icTexVie4201;//4200:註冊訊息 4201:成員訊息
    private Button icBtn4211,icBtn4212,icBtn4213,icBtn4214;//4211:單人發送 4212:清除訊息 4213:全員發送 4214: 
    private EditText icEdiTex4201;//輸入要傳送的訊息
    AsyncTask<Void, Void, Void> mRegisterTask;
    private String TAG="tcnr6==>";

    private ListView icLisVie4220;
    List<Map<String, Object>> mList;
    String[] reg_id;
    private String getReg_id;
    
    private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ic4000gcm);
        
        setupviewcomponent();
        u_onRegisterGCM();
        u_listSQLdata();
//        sendToService(IC7001.LoginAccount);
       
    }
	private void sendToService(String loginAccount) {
		Intent iCareService = new Intent(getBaseContext(), ICareService.class);//用intent連接M1104跟MyService
		Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
		bundle.putString("account", loginAccount);//傳送變數 String action="showtime"放進Bundle裡面
		iCareService .putExtras(bundle);//用intent傳送Bundle
		startService(iCareService);//參考intent開啟服務;這裡是MyService
	
}


	private void setupviewcomponent() {
		
		icTexVie4200 = (TextView) findViewById(R.id.icTexVie4200);
		
    	icBtn4211=(Button)findViewById(R.id.icBtn4211);//單人發送
    	icBtn4212=(Button)findViewById(R.id.icBtn4212);//清除
    	icBtn4213=(Button)findViewById(R.id.icBtn4213);//全員
    	icBtn4214=(Button)findViewById(R.id.icBtn4214);//歷史
    	icEdiTex4201=(EditText)findViewById(R.id.icEdiTex4201);
    	
    	icBtn4211.setOnClickListener(icBtnOn);
    	icBtn4212.setOnClickListener(icBtnOn);
    	icBtn4213.setOnClickListener(icBtnOn);
    	icBtn4214.setOnClickListener(icBtnOn);
    	
	}
    
	private void u_onRegisterGCM() {
    	checkNotNull(SERVER_URL, "SERVER_URL");//呼叫下面的method
        checkNotNull(SENDER_ID, "SENDER_ID");//
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);//
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);//
        
        
        registerReceiver(mHandleMessageReceiver,//改道onResume開啟
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
        final String regId = GCMRegistrar.getRegistrationId(IC4000gcm.this);
        if (regId.equals("")) {
            // Automatically registers application on startup.
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            // Device is already registered on GCM, check server.
            if (GCMRegistrar.isRegisteredOnServer(IC4000gcm.this)) {
                // Skips registration.
            	icTexVie4200.append("你已經註冊過GCM了!!" + "\n");
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered =
                                ServerUtilities.register(context, regId);
                        // At this point all attempts to register with the app
                        // server failed, so we need to unregister the device
                        // from GCM - the app will try to register again when
                        // it is restarted. Note that GCM will send an
                        // unregistered callback upon completion, but
                        // GCMIntentService.onUnregistered() will ignore it.
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute(null, null, null);
            }
        }
		
	}
	
	private String[] u_listSQLdata() {
		icLisVie4220=(ListView)findViewById(R.id.icLisVie4220);
		icTexVie4201=(TextView)findViewById(R.id.icTexVie4201);//成員訊息

		// -------------------------
		mContRes = getContentResolver();
		//-----------------------------	
	
        String [][] getSQLiteLoginData=SQLiteWriter.getSQLiteData(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN);
				
	  		mList = new ArrayList<Map<String, Object>>();
	  		
	  		reg_id = new String[getSQLiteLoginData.length];
			// ---
		
			for (int i = 0; i < getSQLiteLoginData.length; i++) {
				
				Map<String, Object> item = new HashMap<String, Object>();
				
				reg_id[i] = getSQLiteLoginData[i][5]; //储存reg_id給發送message時用
				
	  			item.put("imgView", R.drawable.member1);
	     	    item.put("txtView", "帳號:"+getSQLiteLoginData[i][1]+"\n電話:"+getSQLiteLoginData[i][3]+" \nIP : " + getSQLiteLoginData[i][6] );  //需拆字				
	     	   mList.add(item);
			}
			
	  		SimpleAdapter adapter = new SimpleAdapter(this, mList,
	  				R.layout.ic4000list_view_style, new String[] { "imgView", "txtView" },
	  				new int[] { R.id.imgView, R.id.txtView });
	  		
	  		icLisVie4220.setAdapter(adapter);
	  		icLisVie4220.setTextFilterEnabled(true);
	  		icLisVie4220.setOnItemClickListener(listviewOnItemClkLis);
	  		
	  		icTexVie4201.setText("成員名單： 共" + getSQLiteLoginData.length + " 筆");

	
		return reg_id;
	}
	
	AdapterView.OnItemClickListener listviewOnItemClkLis = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// When clicked, show a toast with the TextView text
			getReg_id=null;
			String s = "你選擇 :  "+ ((TextView) view.findViewById(R.id.txtView)).getText().toString();
			icTexVie4201.setText(s);
			getReg_id = reg_id[position]; //储存reg_id給發送message時用
			
		}
		
	};
	
	private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }
    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            icTexVie4200.append(newMessage + "\n");
        }
    };
    private void u_getMessage(String msg, String reg_id) {

		if(!msg.trim().equals("")){
			if(!reg_id.trim().equals("")){
				  sendGCMmsg(msg,reg_id.trim());
				 
			}else if(reg_id.trim().equals("")){
				Toast.makeText(IC4000gcm.this,"你還沒選擇成員!!",Toast.LENGTH_SHORT).show();
				return;
			}
		}else if(msg.trim().equals("")){
			Toast.makeText(IC4000gcm.this,"你還沒輸入訊息喔!!", Toast.LENGTH_SHORT).show();
		}
		//如果輸入法打開則關閉，如果沒打開則打開
		InputMethodManager m=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);	
		
	}

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

		// // -------------抓取遠端資料庫設定執行續------------------------------
		// StrictMode.setThreadPolicy(new StrictMode
		// .ThreadPolicy.Builder()
		// .detectDiskReads()
		// .detectDiskWrites()
		// .detectNetwork()
		// .penaltyLog()
		// .build());
		// StrictMode.setVmPolicy(new StrictMode
		// .VmPolicy.Builder()
		// .detectLeakedSqlLiteObjects()
		// .penaltyLog()
		// .penaltyDeath()
		// .build());
		// //
		// ---------------------------------------------------------------------
		// DBConnector.sendGCM_Message(msg);//呼叫DBConnector裡面的method
		// sendGCM_Message把得到的字串傳入

	}
private Button.OnClickListener icBtnOn=new Button.OnClickListener(){
//	GCMRegistrar.unregister(IC4000gcm.this);
//	GCMRegistrar.register(IC4000gcm.this, SENDER_ID);
//	registerReceiver(mHandleMessageReceiver,
//            new IntentFilter(DISPLAY_MESSAGE_ACTION));
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.icBtn4211://單人發送
			String msg=null;
			msg=icEdiTex4201.getText().toString().trim();//去空格
			
			u_getMessage(msg,getReg_id);
			
			if(!msg.equals("")){
				 icTexVie4200.append(msg+"\n");
				 icEdiTex4201.setText("");
			}
			
			break;
		case R.id.icBtn4212://更新資料表
			u_listSQLdata();
			break;
		case R.id.icBtn4213://全員發送
			String msg2="";
			msg2=icEdiTex4201.getText().toString().trim();//去空格
			
			String[] regid = u_listSQLdata();  //回傳MySQL內所有regid
			
			for(int i=0;i<regid.length;i++){
				u_getMessage(msg2,regid[i]);			
			}
			if(!msg2.equals("")){
				 icTexVie4200.append(msg2+"\n");
				 icEdiTex4201.setText("");
			}
			break;
		case R.id.icBtn4214://歷史訊息
			Intent it=new Intent();
			it.setClass(IC4000gcm.this, IC4000msg_list.class);
			startActivity(it);
			
			break;
		}
		
	}
	
};
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ic7000, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            /*
            case R.id.options_register:
                GCMRegistrar.register(this, SENDER_ID);
                return true;
            case R.id.options_unregister:
                GCMRegistrar.unregister(this);
                return true;
             */
        case R.id.iccareweb://與官網連結
			break;
		case R.id.mail ://連絡我們					
			break;
		case R.id.action_finish ://結束
			IC4000gcm.this.finish();
			break;               
        }
        return super.onOptionsItemSelected(item);
    }
  //<!---------------生命週期-------------------------------->
    @Override
	protected void onResume() {
//    	this.registerReceiver(mHandleMessageReceiver,
//                new IntentFilter(DISPLAY_MESSAGE_ACTION));
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	protected void onStop() {
		

		super.onStop();
	}

    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        unregisterReceiver(mHandleMessageReceiver);
//        Log.d(TAG, "onStop()Receiver->"+mHandleMessageReceiver);
//        GCMRegistrar.onDestroy(IC4000gcm.this);
//        stopService(IC7001.iCareService);
        super.onDestroy();
    }
//<!------------------------------------------------------------>

}