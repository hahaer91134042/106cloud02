package tcnr.com.project_ic;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPS {

    private IGPSActivity main;

    // Helper for GPS-Position
    private LocationListener mlocListener;
    private LocationManager mlocManager;
//    String provider;
	private long minTime = 1000;// ms
	private float minDist = 0.0f;// meter
    private boolean isRunning;

    public GPS(IGPSActivity main) {
        this.main = main;
//<!--------------GPS取得位置的步驟------------------------------------>
        // GPS Position先宣告一個LocationManager來取得系統定位服務
        mlocManager = (LocationManager) ((Activity) this.main).getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();//繼承下面的MyLocationListener
//        Criteria criteria = new Criteria();//行動準則
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		 provider = mlocManager.getBestProvider(criteria, true);//GPS提供者
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime, minDist, mlocListener);//開啟監聽
                                        //(provoder,更新時間,最少移動距離才更新,監聽者)
//        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
        // GPS Position END
        this.isRunning = true;
    }

    public void stopGPS() {
        if(isRunning) {
            mlocManager.removeUpdates(mlocListener);//取消監聽
            this.isRunning = false;
        }
    }

    public void resumeGPS() {
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime, minDist, mlocListener);//重新開啟監聽
        this.isRunning = true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public class MyLocationListener implements LocationListener {

        private final String TAG = MyLocationListener.class.getSimpleName();

        @Override
        public void onLocationChanged(Location loc) {
            GPS.this.main.locationChanged(loc,loc.getLongitude(), loc.getLatitude());//設定監聽到的經緯度傳到IGPSActivity類別
        }

        @Override
        public void onProviderDisabled(String provider) {
            GPS.this.main.displayGPSSettingsDialog();//設定沒有GPS失去提供者的接口給IGPSActivity類別裡的displayGPSSettingsDialog()
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

}
