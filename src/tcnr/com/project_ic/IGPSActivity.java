package tcnr.com.project_ic;

import android.location.Location;

public interface IGPSActivity {
    public void displayGPSSettingsDialog();
	public void locationChanged(Location loc, double longitude, double latitude);
}
