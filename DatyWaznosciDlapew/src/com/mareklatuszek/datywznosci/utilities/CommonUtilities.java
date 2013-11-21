package com.mareklatuszek.datywznosci.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.zxing.WriterException;
import com.mareklatuszek.datywaznosci.Product;
import com.mareklatuszek.datywaznosci.R;

public class CommonUtilities implements FinalVariables {
	
	public int getProgress(String dataOtw, String terminWaz) {
		
		int progress = 100;
		
		try {
			
			double start = parseDate(dataOtw);
			double end = parseDate(terminWaz);
			double current = (new Date()).getTime();
			
			double pr = (((end - current) / (end - start))) * 100;
			
			progress = (int) pr;
			
		} catch (ParseException e) {
			return 100;
		}
		
		return progress;		
	}
	
	public long parsePrzypmnienieToDate(String boxVal, String spinnerVal, String terminWaz, String notifHour) throws ParseException {
		if(boxVal.equals("")) {
			return 0;
		} else {
			int dateVal = Integer.parseInt(boxVal);
			int dateFormatVal = Integer.parseInt(spinnerVal);
			long endDate = parseDate(terminWaz);
			
			Calendar notifTime = Calendar.getInstance();
			notifTime.setTimeInMillis(endDate);
			
			if (dateFormatVal == 0) { //jesli dzien (kolejnosc z resource arrays "array_date" - trzyma� si� tej kolejnosci!)
				notifTime.add(Calendar.DAY_OF_YEAR, -dateVal);
			} else if (dateFormatVal == 1) { //jesli miesi�c
				notifTime.add(Calendar.MONTH, -dateVal);
			} else if (dateFormatVal == 2) { //jesli rok
				notifTime.add(Calendar.YEAR, -dateVal);
			}
			
			if (notifHour.equals("")) {
				notifTime.add(Calendar.HOUR_OF_DAY, 14);
			} else {
				int hour = Integer.parseInt(notifHour);// format to 0-23
				notifTime.add(Calendar.HOUR_OF_DAY, hour);
			}
			
			long przypomnienieDate = notifTime.getTimeInMillis();
			
			return przypomnienieDate;
		}
	}
	
	public long parseDate(String dateToParse) throws ParseException {
		String toParse = dateToParse;
		SimpleDateFormat formatter = new SimpleDateFormat("d/M/yyyy");
		Date date = formatter.parse(toParse);
		long millis = date.getTime();
		
		return millis;
	}
	
	public String parseDateToOkres(String date) {
		Calendar currCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		currCal.set(Calendar.HOUR_OF_DAY, 13);
		long currentTime = currCal.getTimeInMillis();
		long endTime = 0;
		try {
			endTime = parseDate(date);
			endCal.setTimeInMillis(endTime);
			endCal.set(Calendar.HOUR_OF_DAY, 14);
			endTime = endCal.getTimeInMillis();
			
			long difference = endTime - currentTime;
			long diffDay = difference / 86400000L;
			long diffMonth = difference / 2592000000L;
			long diffYear = difference / 31536000000L;
			
			if(diffYear >= 1L) {
				int i = (int) diffYear;
				String okres = i + ":" + SPINNER_DATE_YEAR;
				return okres;

			} else if (diffMonth >= 1L) {
				int i = (int) diffMonth;
				String okres = i + ":" + SPINNER_DATE_MONTH;
				return okres;

			} else if (diffDay >= 1L) {
				int i = (int) diffDay;
				String okres = i + ":" + SPINNER_DATE_DAY;
				return okres;
			} else {
				return "";
			}
			
		} catch (ParseException e) {
			Log.i("utils", "parseDateToOkres");
			return "";
		}		
	}
	
	public String parseOkresToDate(String okres) {
		Calendar cal = Calendar.getInstance();
		String box = getTextFromOkresWaz(okres);
		String okresDate = "";
		int val = 0;
		
		if (!box.equals("")) {
			val = Integer.parseInt(box);
		} else {
			return "";
		}
		
		String format = getSpinnerItemFromOkresWaz(okres);		
		
		if(format.contains(SPINNER_DATE_DAY)) {
			cal.add(Calendar.DAY_OF_YEAR, val);
		} else if (format.contains(SPINNER_DATE_MONTH)) {
			cal.add(Calendar.MONTH, val);
		} else if (format.contains(SPINNER_DATE_YEAR)) {
			cal.add(Calendar.YEAR, val);
		}
		
		long okresInMillis = cal.getTimeInMillis();
		okresDate = parseMillisToDate(okresInMillis);
		
		return okresDate;
	}
	
	public Product parseCodeToProduct(String code){
		Product product = new Product();
		
		try {
			JSONObject jObj = new JSONObject(code);
			
			String nazwa = jObj.getString(DB_NAZWA);
			String dataOtwarcia = jObj.getString(DB_DATA_OTWARCIA);
			String kategoria = jObj.getString(DB_KATEGORIA);
			String okresWaznosci = jObj.getString(DB_OKRES_WAZNOSCI);
			String opis = jObj.getString(DB_OPIS);
			String przypomnieniaJson = jObj.getString(DB_PRZYPOMNIENIA);
			String terminWaznosci = jObj.getString(DB_TERMIN_WAZNOSCI);
			
			product.setCodeFormat("QR_CODE");
			product.setNazwa(nazwa);
			product.setDataOtwarcia(dataOtwarcia);			
			product.setKategoria(kategoria);		
			product.setOkresWaznosci(okresWaznosci);		
			product.setOpis(opis);
			product.setPrzypomnieniaFromDB(przypomnieniaJson);
			product.setTerminWaznosci(terminWaznosci);
			
			return product;
		} catch (JSONException e) {
			return product;
		}			
	}
	
	public String dateToWords(long startTimeInMillis)
	{
		String time = "";
		long startTimeInSec = startTimeInMillis / 1000;
		long currentTimeInSec = (int) (System.currentTimeMillis() / 1000L);
		long difference = startTimeInSec - currentTimeInSec;
		
		if(difference < 3600) {
			time = "za godzinę";
			return time;
		} else if (difference >= 3600 & difference < 14400) {
			time = "za " + (difference/3600) + " godziny";
			return time;
		} else if (difference >= 14400 & difference < 75600) {
			time = "za " + (difference/3600) + " godzin";
			return time;
		} else if (difference >= 75600 & difference < 86400) {
			time = "za " + (difference/3600) + " godziny";
			return time;
		} else if (difference >= 86400 & difference < 172800) {
			time = "jutro";
			return time;
		} else if (difference >= 172800 & difference < 259200) {
			time = "pojutrze";
			return time;
		} else if (difference >= 259200 & difference < 604800) {
			time = "za " + (difference/86400) + " dni";
			return time;
		} else if (difference >= 604800 & difference < 1209600) {
			time = "za tydzień";
			return time;
		} else if (difference >= 1209600 & difference < 2419200) {
			time = "za " + (difference/604800) + " tygodnie";
			return time;
		} else if (difference >= 2419200 & difference < 4838400) {
			time = "za miesiąc";
			return time;
		} else if (difference >= 4838400 & difference < 9676800) {
			time = "za " + (difference/2419200) + " miesiące";
			return time;
		} else if (difference >= 9676800 & difference < 29030400) {
			time = "za " + (difference/2419200) + " miesięcy";
			return time;
		} else if (difference >= 29030400 & difference < 58060800) {
			time = "za rok";
			return time;
		} else if (difference >= 58060800 & difference < 145152000) {
			time = "za " + (difference/29030400) + " lata";
			return time;
		} else {
			time = (difference/29030400) + "lat";
			return time;
		}
		
	}
	
	public ArrayList<HashMap<String, String>> sortPrzypomnieniaAll(ArrayList< HashMap< String,String >> toSort) {
		
	    Collections.sort(toSort, new Comparator<HashMap< String,String >>() {

	        @Override
	        public int compare(HashMap<String, String> first, HashMap<String, String> second) {
	        	//sortuje wg daty przypomenia
	        	String firstValue = first.get(PRZYP_DATE);
	            String secondValue = second.get(PRZYP_DATE);
	            return firstValue.compareTo(secondValue);
	        }
	    });
	    
		return toSort;
	}
	
	public String getCurrentDate() {
		String dateFormat = "dd/MM/yyyy";
		String currentDate = "";
		
	    DateFormat formatter = new SimpleDateFormat(dateFormat);
	    Calendar calendar = Calendar.getInstance();
	    currentDate = formatter.format(calendar.getTime());
	    
	    return currentDate;
	}
	
	public String parseMillisToDate(long timeInMillis) {
		String dateFormat = "dd/MM/yyyy";
		String date = "";
		
	    DateFormat formatter = new SimpleDateFormat(dateFormat);
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(timeInMillis);
	    date = formatter.format(calendar.getTime());
	    
	    return date;
	}
	
	@SuppressWarnings("deprecation")
	public void expandLinearLayout(final LinearLayout v) {
	    v.measure(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    v.getLayoutParams().height = 0;
	    v.setVisibility(View.VISIBLE);
	    
	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) {
	        	int width = LinearLayout.LayoutParams.FILL_PARENT;
	        	int height = LinearLayout.LayoutParams.WRAP_CONTENT;
	        	LayoutParams params = new LinearLayout.LayoutParams(width, height);// rozmiary
	        	
	        	v.setLayoutParams(params);
	            v.requestLayout();
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    a.setDuration(500);
	    v.startAnimation(a);	    
	}
	
	public Bitmap encodeCodeToBitmap(String code, String codeFormat, FragmentActivity mActivity)
	{
		//konwersja zeskanowanego kodu na obrazek, na podstawie kodu i jego formatu
				
	    int qrCodeDimention = 150; //rozmiar obrazka

	    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(code, null, Contents.Type.TEXT, codeFormat, qrCodeDimention);

	    try {
	        Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
	        return bitmap;
	    } catch (WriterException e) {
	    	//w razie niepowodzenia - domy�lna grafika
	    	Bitmap bitmap= BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.zxinglib_icon); 
	        return bitmap;
	    }	
	}
	
	public int getPosInSpinner(String category, Spinner spinner) {
		
		int spinnSize = spinner.getCount();
		
		for (int i = 0; i < spinnSize; i++) {
			String item = (String) spinner.getItemAtPosition(i);
			if (item.contains(category)) {
				return i;
			}
		}
		
		return 0;
	}
	
	public String getTextFromOkresWaz(String okres) {
		String box = "";
		Pattern pattern = Pattern.compile(":");
		
		Matcher matcher = pattern.matcher(okres);
		if (matcher.find()) {
			box = okres.substring(0, matcher.start());
		}
		
		return box;
	}
	
	public String getSpinnerItemFromOkresWaz(String okres) {
		String spinn = "";	
		Pattern pattern = Pattern.compile(":");
		
		Matcher matcher = pattern.matcher(okres);
		if (matcher.find()) {
			spinn = okres.substring(matcher.end());
		}
				
		return spinn;
	}
	
	public String getJsonFromProduct(Product product) {
		String json = "";
		try {
			String nazwa = product.getNazwa();
			String okres = product.getOkresWaznosci();
			String codeFormat = product.getCodeFormat();
			String dataOtw = product.getDataOtwarcia();
			String terminWaz = product.getTerminWaznosci();
			String kategoria = product.getKategoria();
			String opis = product.getOpis();
			String image = product.getImage();
			JSONArray przypoimnienia = new JSONArray(product.getPrzypomnieniaToDB());
			
			JSONObject jObj = new JSONObject();
			jObj.put(DB_NAZWA, nazwa);
			jObj.put(DB_OKRES_WAZNOSCI, okres);
			jObj.put(DB_CODE_FORMAT, codeFormat);
			jObj.put(DB_DATA_OTWARCIA, dataOtw);
			jObj.put(DB_TERMIN_WAZNOSCI, terminWaz);
			jObj.put(DB_KATEGORIA, kategoria);
			jObj.put(DB_OPIS, opis);
			jObj.put(DB_PRZYPOMNIENIA, przypoimnienia);
		
			json = jObj.toString();
		} catch (JSONException e) {
			Log.i("utils", "getJsonFromProduct");
		}
		return json;
	}
	
	public boolean validateCode(String code, String codeFormat) {
		if(codeFormat.equals("QR_CODE")){
			try {
				Product product = new Product();
				String productJson = getJsonFromProduct(product);
				
				JSONObject jObjCode = new JSONObject(code);
				JSONObject jObjEmpty = new JSONObject(productJson);
				JSONArray emptyNames = jObjEmpty.names();
							
				for (int i = 0; i < emptyNames.length(); i++) {
					if (!jObjCode.has(emptyNames.getString(i))) {
						return false;
					}
				}
				return true;
			} catch (JSONException e) {
				Log.i("Utils", "code is not product");
				return false;
			}	
		} else {
			return false;
		}
	}
}