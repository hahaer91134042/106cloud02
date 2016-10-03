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

import static tcnr.com.project_ic.CommonUtilities.SENDER_ID;
import static tcnr.com.project_ic.CommonUtilities.displayMessage;
import static tcnr.com.project_ic.CommonUtilities.EXTRA_MESSAGE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import tcnr.com.project_ic.providers.DBContentProvider;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private String[] MYCOLUMN_GCMmsg = new String[] {"id","msgDate","GCMmsg" };
	private static ContentResolver mContRes;
	private String mTAG="tcnr6==>";
	
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";
    

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(mTAG, "Device registered: regId = " + registrationId);
        displayMessage(context, "註冊GCM成功!!"+"\n");
        ServerUtilities.register(context, registrationId); //向user sever註冊;傳送registrationId給user server
    }

    @Override//向google GCM註冊不成功後執行
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(mTAG, "Device unregistered");
        displayMessage(context, "開始取消GCM註冊..."+"\n");
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(mTAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(mTAG, "Received message");
       Bundle bData=intent.getExtras();
        // notifies user
        generateNotification(context, bData);
        
        String getmsg=bData.getString(EXTRA_MESSAGE);
//        displayMessage(context, msg);
        Log.i(mTAG, "getmsg=>"+getmsg);
        u_saveGCMmsg(getmsg.trim());
    }

   
    protected void onDeletedMessages(Context context, Intent intent) {
        Log.i(mTAG, "Received deleted messages notification");
        
      //接受GCM SERVER 傳來的訊息
        Bundle bData = intent.getExtras();
        
        
        // notifies user
        generateNotification(context, bData);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(mTAG, "Received error: " + errorId);
        displayMessage(context, "GCM發生錯誤:"+ errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(mTAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, Bundle data) {
    	String getmsg="";
    	getmsg=data.getString("message");   	
    	
    	int icon = android.R.drawable.star_big_on;
        long when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent ni = new Intent(context, IC4000gcm.class);        
        
        ni.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, ni, 0);
        // set intent so it does not start a new activity
        Log.e("MAP", data.toString());
        Notification noti = new Notification.Builder(context)
        		.setContentTitle("收到 GCM 通知囉")
        		.setContentText(getmsg)
        		.setContentIntent(intent)
        		.setDefaults(Notification.DEFAULT_ALL)
        		.setSmallIcon(icon)
        		.setWhen(when)
        		.build();
        nm.notify(0, noti);
        
        
//      DemoActivity.getUserMessage(getmsg); 
//        try {
//			Thread.sleep(250);
//		} catch (Exception e) {
//			
//		}
//        
//        CommonUtilities.displayMessage(context, getmsg);
        
    }

	private void u_saveGCMmsg(String getmsg) {
        String sysTime=SQLiteWriter.getSystemTime();
		// -------------------------
		mContRes = getContentResolver();
		// -----------------------------

		// --------insert SQLite-------------------------------------------
		ContentValues newRow = new ContentValues();
        newRow.put("msgDate", sysTime);
		newRow.put("GCMmsg", getmsg);
        SQLiteWriter.wirteToSQLite(mContRes, DBContentProvider.CONTENT_URI_GCMmsg,MYCOLUMN_GCMmsg, newRow);
	
		// --------------------------------------------------------
		
	}

}
