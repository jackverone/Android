package com.mareklatuszek.datywaznosci;

import java.text.ParseException;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mareklatuszek.datywznosci.utilities.CommonUtilities;

public class DialogPrzypomnienie extends Dialog implements android.view.View.OnClickListener {
	
	Product product;
	CommonUtilities utilities = new CommonUtilities();
	
	TextView nazwaTxt, okresTxt, dataOtwTxt, terminWazTxt, kategoriaTxt;
	ProgressBar okresProgress;

	public DialogPrzypomnienie(Activity mActivity, Product product) {
		super(mActivity);
		this.product = product;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_przypomnienie);
		
		initDialog();
	}
	
	private void initDialog() {
		nazwaTxt = (TextView) findViewById(R.id.nazwaTxt);
		okresTxt = (TextView) findViewById(R.id.okresTxt);
		dataOtwTxt = (TextView) findViewById(R.id.dataOtwTxt);
		terminWazTxt = (TextView) findViewById(R.id.terminWazTxt);
		kategoriaTxt = (TextView) findViewById(R.id.kategoriaTxt);
		okresProgress = (ProgressBar) findViewById(R.id.okresProgress);
		
		String nazwa = product.getNazwa();
		String terminWaz = product.getTerminWaznosci();
		long terminDateInMillis = 0;
		try {
			terminDateInMillis = utilities.parseDate(terminWaz);
		} catch (ParseException e) {
			Log.i("dialog przyp", "parse error");
		}
		String okres = utilities.dateToWords(terminDateInMillis);
		String dataOtw = product.getDataOtwarcia();		
		String kategoria = product.getKategoria();
		int progress = utilities.getProgress(dataOtw, terminWaz);
		
		nazwaTxt.setText(nazwa);
		okresTxt.setText(okres);
		dataOtwTxt.setText(dataOtw);
		terminWazTxt.setText(terminWaz);
		kategoriaTxt.setText(kategoria);
		okresProgress.setProgress(progress);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dodajButtonKat:
			
			break;
		}
		
	}
	

	
		
}