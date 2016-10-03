package tcnr.com.project_ic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

public class Finduserip {
 static String IPaddress;
 Boolean IPValue;
 static ConnectivityManager CM = null;
 static WifiManager wm = null;
 static String TAG = "tcnr6==>";

 // ------------------------------------
 //Check if Internet Network is active
 public static String NetwordDetect(ConnectivityManager icM, WifiManager iwm) {
  boolean WIFI = false;
  boolean MOBILE = false;
  CM = icM;
  wm = iwm;
  NetworkInfo[] networkInfo = CM.getAllNetworkInfo();
  for (NetworkInfo netInfo : networkInfo) {
   if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
    if (netInfo.isConnected())
     WIFI = true;
   if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
    if (netInfo.isConnected())
     MOBILE = true;
  }

  if (WIFI == true) {
   IPaddress = GetDeviceipWiFiData();
  }

  if (MOBILE == true) {
   IPaddress = GetDeviceipMobileData();
  }
  return IPaddress;
 }

 // ---------------------------------------------------------
 public static String GetDeviceipMobileData() {
  try {
   for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
    NetworkInterface networkinterface = en.nextElement();
    for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr
      .hasMoreElements();) {
     InetAddress inetAddress = enumIpAddr.nextElement();
     if (!inetAddress.isLoopbackAddress()) {
      return inetAddress.getHostAddress().toString();
     }
    }
   }
  } catch (Exception ex) {
   Log.d(TAG, "Current IP:" + ex.toString());
  }
  return null;
 }

 // --------------------------------------------------
 public static String GetDeviceipWiFiData() {
  // WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
  @SuppressWarnings("deprecation")
  String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
  // String ip = String.valueOf(wm.getConnectionInfo().getIpAddress());
  return ip;
 }

}
