package com.mareklatuszek.datywaznosci;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(
		formKey = "",
        mailTo = "marek.lat@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
        )
public class MyApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
        ACRA.init(this);
	}
	

}