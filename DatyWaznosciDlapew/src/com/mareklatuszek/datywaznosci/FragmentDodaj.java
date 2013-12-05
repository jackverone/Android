package com.mareklatuszek.datywaznosci;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.mareklatuszek.utilities.BitmapLoader;
import com.mareklatuszek.utilities.CommonUtilities;
import com.mareklatuszek.utilities.FinalVariables;

public class FragmentDodaj extends SherlockFragment implements OnClickListener, OnKeyListener, FinalVariables {
	
	boolean takePictureStat = false;
	boolean dodatkoweIsVisible = false;
	boolean isFullVersion = true;
	boolean isScanned = false;
	int tempSpinnOkresPos = 0;
	String currentDate = "";
	String code = "";
	String codeFormat = "";
	
	AdapterDB dbAdapter;
	CommonUtilities utilities = new CommonUtilities();
	ArrayAdapter<String> spinnerAdapter;
	ArrayList<String> kategorie;
	AdapterSpinnerOkres adapterSpinnerOkres;
	
	View rootView;
	ImageView barcodeImage, obrazekImage;
	Button dataOtwButton, terminWazButton, zapiszButton, dodatkoweButton, kategorieButton, dataZuzButton;
	EditText nazwaTextBox, okresWazTextBox, opisTxtBox;
	Spinner okresWazSpinner, kategorieSpinner;
	LinearLayout podstawowe, dodatkowe, przypLayout, latDodatkoweEdit;
	View przypRow;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_dodaj, container, false);
		currentDate = utilities.getCurrentDate();
		dbAdapter = new AdapterDB(getActivity());
		
		getSherlockActivity().getSupportActionBar().setTitle("Dodaj produkt");
		
		initPodstawowe();
		initDodatkowe();
		
		if(savedInstanceState == null) {
			Bundle extras = getArguments();
		
			if (extras != null) {//TODO
				Product bundleProduct = (Product) extras.getSerializable("product");
				setViewsFromProduct(bundleProduct);
				showDodatkowe();
			}
			
		} else {
			Product savedStateProduct = (Product) savedInstanceState.getSerializable("product");
			boolean ifDodatkoweExpand = savedInstanceState.getBoolean("dodatkowe");
			isScanned = savedInstanceState.getBoolean("isScanned");
			tempSpinnOkresPos = savedInstanceState.getInt("tempSpinnOkresPos");
			
			setViewsFromProduct(savedStateProduct);
			
			if (ifDodatkoweExpand) { 
				showDodatkowe();
			}
		}
	
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
	
		Product productToSave = prepareDataToStore();
		
		if (dodatkoweIsVisible) { 
			bundle.putBoolean("dodatkowe", true);

		} else {
			bundle.putBoolean("dodatkowe", false);
		}
		
		bundle.putBoolean("isScanned", isScanned);
		bundle.putInt("tempSpinnOkresPos", tempSpinnOkresPos);
	    bundle.putSerializable("product", productToSave);     
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    android.view.MenuInflater inflater = getActivity().getMenuInflater();
	    
	    switch (v.getId()) {
	    case R.id.barcodeImage:
	    	inflater.inflate(R.menu.popup_get_code, menu);
	    	break;
	    case R.id.obrazekImage:
	    	inflater.inflate(R.menu.popup_get_image, menu);
	    	break;
	    }
	    
	    
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		super.onContextItemSelected(item);
	    
	    switch (item.getItemId()) {
    	case R.id.scanPopup:
    			((MainActivity)getActivity()).startScanner();
    		break;
    	case R.id.generatePopup:
    		if (!checkFormIsFill()) {
    			Toast.makeText(getActivity(), "Należy podać nazwę i okres ważności", 1500).show();
    		} else {
        		Product product = prepareDataToStore();
        		
				DialogGeneruj dialogGen = new DialogGeneruj(getActivity(),product , getFragmentManager(), getId());
				dialogGen.show();
    		}	
    		break;
    	case R.id.gallery:
    		pickImageFromGallery();
    		break;
    		
    	case R.id.camera:
    		takePhoto();
    		break; 
    	}
	    return true;
	}
	 	
	@Override
	public void onClick(View view) {
		DialogDatePicker dialogDatePicker = new DialogDatePicker(getActivity(), view, getFragmentManager(), getId());
		
		switch (view.getId()) {
		case R.id.barcodeImage:
			view.showContextMenu();
			break;
		case R.id.dataOtwButton:
        	dialogDatePicker.show();
			break;
		case R.id.terminWazButton:
        	dialogDatePicker.show();
			break;
		case R.id.dataZuzButton:
			dialogDatePicker.show();
			break;
		case R.id.zapiszButton:
			save();
			break;
		case R.id.dodatkoweButton:
			showDodatkowe();
			break;	
		case R.id.obrazekImage:
			view.showContextMenu();
			break;
		case R.id.kategorieButton:
			DialogKategorie dialogKategorier = new DialogKategorie(getActivity(), kategorieSpinner, getFragmentManager(), getId());
			dialogKategorier.show();
			break;
		}		
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch (v.getId()) {
		case R.id.okresWazTextBox:
	    	setTerminWaz();	    		
			break;
		}
		return false;
	}
						
	private void initPodstawowe() {		

		podstawowe = (LinearLayout) rootView.findViewById(R.id.podstawowe);
		barcodeImage = (ImageView) rootView.findViewById(R.id.barcodeImage);
		nazwaTextBox = (EditText) rootView.findViewById(R.id.nazwaTextBox);
		okresWazTextBox = (EditText) rootView.findViewById(R.id.okresWazTextBox);
		okresWazSpinner = (Spinner) rootView.findViewById(R.id.okresWazSpinner);
		zapiszButton = (Button) rootView.findViewById(R.id.zapiszButton);
		dodatkoweButton = (Button) rootView.findViewById(R.id.dodatkoweButton);
		
		Bundle extras = getArguments(); // TODO jesli zeskanuje
		if (extras != null) {
			code = extras.getString("scanResultCode");
			codeFormat = extras.getString("scanResultCodeFormat");
			setDataFromScan(code, codeFormat); 
		}

		registerForContextMenu(barcodeImage);
		barcodeImage.setOnClickListener(this);
		zapiszButton.setOnClickListener(this);	
		dodatkoweButton.setOnClickListener(this);
		okresWazTextBox.setOnKeyListener(this);
		
		adapterSpinnerOkres = new AdapterSpinnerOkres(getActivity(), MainActivity.currentFragmentPos, getId());
		okresWazSpinner.setAdapter(adapterSpinnerOkres);
	}
	
	private void initDodatkowe() {		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		latDodatkoweEdit = (LinearLayout) inflater.inflate(R.layout.lay_dodatkowe_edit, null);		
		dodatkowe = (LinearLayout) rootView.findViewById(R.id.dodatkowe);	
		dodatkowe.addView(latDodatkoweEdit);
		
		dataOtwButton = (Button) dodatkowe.findViewById(R.id.dataOtwButton);
		terminWazButton = (Button) dodatkowe.findViewById(R.id.terminWazButton);
		kategorieButton = (Button) dodatkowe.findViewById(R.id.kategorieButton);
		kategorieSpinner = (Spinner) dodatkowe.findViewById(R.id.kategorieSpinner);
		opisTxtBox = (EditText) dodatkowe.findViewById(R.id.opisTxtBox);
		obrazekImage = (ImageView) dodatkowe.findViewById(R.id.obrazekImage);
		przypLayout = (LinearLayout) dodatkowe.findViewById(R.id.przypomnieniaLayout);
		dataZuzButton = (Button) dodatkowe.findViewById(R.id.dataZuzButton);
		
		registerForContextMenu(obrazekImage);
		obrazekImage.setOnClickListener(this);
		dataOtwButton.setOnClickListener(this);
		terminWazButton.setOnClickListener(this);
		kategorieButton.setOnClickListener(this);
		dataZuzButton.setOnClickListener(this);
		
//		initObrazek();
		dataOtwButton.setText(currentDate);
		
		initKategorie();
		initPrzypomnienia();
	}
	
	private void initKategorie() {
		kategorie = new ArrayList<String>();
		
		dbAdapter.open();
		kategorie.addAll(dbAdapter.getAllCategories());
		kategorie.add("Brak kategorii");
		Collections.reverse(kategorie);
		dbAdapter.close();
	
		spinnerAdapter= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, kategorie);
		kategorieSpinner.setAdapter(spinnerAdapter);
	}
	
	private void initPrzypomnienia() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final LinearLayout row = (LinearLayout) inflater.inflate(R.layout.listview_add_powiadomienia, null);
		Button removePrzypButton = (Button) row.findViewById(R.id.removePrzypButton);
		Button addPrzypButton = (Button) row.findViewById(R.id.addPrzypButton);
		Button godzButton = (Button) row.findViewById(R.id.godzButton);
		
		removePrzypButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				przypLayout.removeView(row);
				int rowsCount = przypLayout.getChildCount();
				
				if (rowsCount == 0) {
					initPrzypomnienia();
				} else {
					View viv = przypLayout.getChildAt(rowsCount - 1);
					Button przypButton = (Button) viv.findViewById(R.id.addPrzypButton);
					przypButton.setVisibility(View.VISIBLE);
				}
			}
		});
		
		godzButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogTimePicker dialogTimePicker = new DialogTimePicker(getActivity(), v);
				dialogTimePicker.show();				
			}
		});
		
		addPrzypButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				initPrzypomnienia();	
				View viv =przypLayout.getChildAt(przypLayout.getChildCount() - 2);
				Button przypButton = (Button) viv.findViewById(R.id.addPrzypButton);
				przypButton.setVisibility(View.GONE);
			}
		});
		przypLayout.addView(row);
	}
	
	private void initObrazek() {
		if(MainActivity.imageUri.toString().equals("")) {
			Log.i("uri", "null");
		} else {
			String path = utilities.getRealPathFromURI(MainActivity.imageUri, getActivity());
			Bitmap bm = BitmapLoader.loadBitmap(path, 100, 100);
			obrazekImage.setImageBitmap(bm);
		}
	}
			
	private void showDodatkowe() {
		dodatkoweIsVisible = true;
        utilities.expandLinearLayout(dodatkowe);
        dodatkoweButton.setVisibility(View.GONE);
	}
		
	public void takePhoto() {	
		File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"TPP");
		directory.mkdirs();
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment.getExternalStorageDirectory(), "TPP" + File.separator 
        		+ (System.currentTimeMillis()/1000) + ".jpg");
        MainActivity.imageUri = Uri.fromFile(f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        getActivity().startActivityForResult(intent, CAMERA_ADD_RQ_CODE);
    }
	
	public void pickImageFromGallery() {
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		getActivity().startActivityForResult(i, GALLERY_ADD_RQ_CODE);
	}
		
	private Product prepareDataToStore() {			
		Product product = new Product();
		
		String nazwa = nazwaTextBox.getText().toString();
		String okresWaznosci = getPeriodFromBoxAndSpinner(okresWazTextBox, okresWazSpinner);
		String kod = code;
		String typKodu = codeFormat;
		
		product.setNazwa(nazwa);
		product.setOkresWaznosci(okresWaznosci);			
		product.setCode(kod);
		product.setCodeFormat(typKodu);	
		product.setIsScanned(isScanned);

		String dataOtwarcia = dataOtwButton.getText().toString();			
		String terminWaznosci = getTerminWaznosci();
		String kategoria = getKategoria();
		String dataZuz = dataZuzButton.getText().toString();
		String image = utilities.getRealPathFromURI(MainActivity.imageUri, getActivity());
		String opis = opisTxtBox.getText().toString();
		ArrayList<HashMap<String, String>> przypomnienia = getPrzypomnienia();
		
		product.setDataOtwarcia(dataOtwarcia);	
		product.setTerminWaznosci(terminWaznosci);
		product.setKategoria(kategoria);
		product.setDataZuzycia(dataZuz);
		product.setImage(image);
		product.setOpis(opis);
		product.setPrzypomnienia(przypomnienia);
	
		return product;
	}
	
	private void saveData() {
		new AsyncTask<Void, Void, Void>() {
			ProgressDialog progressDialog;
				
			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(getActivity(), "Dodaję", "Dodawanie do bazy");
			}

			@Override
			protected Void doInBackground(Void... params) {
				storeAllToDatabase();	
				return null;
			}
			
			@Override
			protected void onPostExecute(Void v) {
				//TODO zrobic okienko jesli niepowodzenie
				progressDialog.dismiss();
				MainActivity.imageUri = Uri.parse("");
				((MainActivity) getActivity()).selectFragment(2); // prze��cza a ekran listy produkt�w
			}
		}.execute();
	}
	
	public void saveCodeFromDialogGeneruj(String code, String codeFormat) {
		this.code = code;
		this.codeFormat = codeFormat;
	}
	
	private boolean storeAllToDatabase() {
		Product product = prepareDataToStore();
		utilities.createThumb(product.getImage());
		dbAdapter.open();		
		boolean storeStatus = dbAdapter.insertProduct(product);
		dbAdapter.close();
		
		if (storeStatus) {
			ArrayList<HashMap<String, String>> przypomnienia = product.getPrzypomnienia();
			String nazwa = product.getNazwa();
			String code = product.getCode();
			setAlarms(przypomnienia, nazwa, code);
			isScanned = false;
		}
		
		return storeStatus; //je�li zapisze do poprawnie
	}
	
	private void setViewsFromProduct(Product product) {
	
		String nazwa = product.getNazwa();
		String okresWaznosci = product.getOkresWaznosci();
		String kod = product.getCode();
		String typKodu = product.getCodeFormat();
		String okresText = utilities.getFirstValue(okresWaznosci);
		String okresSpinnVal = utilities.getSecondValue(okresWaznosci);
		int okresSpinPos = utilities.getPosInSpinner(okresSpinnVal, okresWazSpinner);
		
		nazwaTextBox.setText(nazwa);
		okresWazTextBox.setText(okresText);
		okresWazSpinner.setSelection(okresSpinPos);
		if (!kod.equals("")) {
			setCodeImage(kod, typKodu);
		}	
		
		String dataOtwarcia = product.getDataOtwarcia();			
		String terminWaznosci = product.getTerminWaznosci();
		String kategoria = product.getKategoria();
		int kategoriaId = utilities.getPosInSpinner(kategoria, kategorieSpinner);
		String dataZuz = product.getDataZuzycia();
		String imagePath = product.getImage();
		Bitmap imageBmp = BitmapLoader.loadBitmap(imagePath, 100, 100);
		String opis = product.getOpis();
		ArrayList<HashMap<String, String>> przypomnienia = product.getPrzypomnienia();
		
		dataOtwButton.setText(dataOtwarcia);
		terminWazButton.setText(terminWaznosci);
		kategorieSpinner.setSelection(kategoriaId);
		Log.i("set image", "setViews");
		obrazekImage.setImageBitmap(imageBmp);
		opisTxtBox.setText(opis);
		setPrzypomnienia(przypomnienia);	
		dataZuzButton.setText(dataZuz);
	}
	
	private void setPrzypomnienia(ArrayList<HashMap<String, String>> przypomnienia) { //TODO naprawić, dubluje sie
		
		int przypCount = przypomnienia.size();
		
		if (przypCount > 0) {
			przypLayout.removeAllViews();
		}
		
		for(int i = 0; i < przypCount; i++) {
			
			HashMap<String, String> przypomnienie = przypomnienia.get(i);
			
			LayoutInflater inflater =  getActivity().getLayoutInflater();		
			String boxTxt = przypomnienie.get(PRZYP_TEXT_BOX);
			String spinner = przypomnienie.get(PRZYP_SPINNER);
			int spinnerPos = Integer.parseInt(spinner);
			String przypHour = przypomnienie.get(PRZYP_HOUR);//TODO	
						
			final LinearLayout row = (LinearLayout) inflater.inflate(R.layout.listview_add_powiadomienia, null);
			EditText przypTextBox = (EditText) row.findViewById(R.id.przypTextBox);
			Spinner przypSpinner = (Spinner) row.findViewById(R.id.przypSpinner);
			Button removePrzypButton = (Button) row.findViewById(R.id.removePrzypButton);
			Button przypButton = (Button) row.findViewById(R.id.addPrzypButton);
			Button godzButton = (Button) row.findViewById(R.id.godzButton);
			
			przypTextBox.setText(boxTxt);
			przypSpinner.setSelection(spinnerPos);
			godzButton.setText(przypHour);
					
//			EditText temp = new EditText(getActivity());
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(54, LayoutParams.WRAP_CONTENT, 1);
//			temp.setLayoutParams(params);
//			temp.setText(boxTxt);
//			row.addView(temp);		
						
			removePrzypButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					przypLayout.removeView(row);
					int rowsCount = przypLayout.getChildCount();
					
					if (rowsCount == 0) {
						initPrzypomnienia();
					} else {
						View viv = przypLayout.getChildAt(rowsCount - 1);
						Button przypButton = (Button) viv.findViewById(R.id.addPrzypButton);
						przypButton.setVisibility(View.VISIBLE);
					}				
				}
			});
			
			if (i < (przypCount - 1)) {
				przypButton.setVisibility(View.GONE);
			} else {
				przypButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						initPrzypomnienia();
						View rowBefore = przypLayout.getChildAt(przypLayout.getChildCount() - 2);
						Button buttonBefore = (Button) rowBefore.findViewById(R.id.addPrzypButton);
						buttonBefore.setVisibility(View.GONE);				
					}
				});				
			}
			
			godzButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					DialogTimePicker dialogTimePicker = new DialogTimePicker(getActivity(), v);
					dialogTimePicker.show();				
				}
			});
			
			przypLayout.addView(row);	
		}	
	}
	
	private void setTerminWaz() {
		String peroid = getPeriodFromBoxAndSpinner(okresWazTextBox, okresWazSpinner);
		String date = utilities.parseOkresToDate(peroid);
		terminWazButton.setText(date);
	}
	
	public void setTerminWazFromAdapter(String spinnText) {
		String txtBoxOkres = okresWazTextBox.getText().toString();
		String peroid = txtBoxOkres + ":" + spinnText;
		String date = utilities.parseOkresToDate(peroid);
		terminWazButton.setText(date);
	}
	
	public void setDataFromScan(String code, String codeFormat) {		
		dbAdapter.open();
		boolean isInDB = dbAdapter.chckIfProductIsInDB(code);
		dbAdapter.close();
		
		if(isInDB) {
			showChoiceDialog(code, codeFormat);
		} else {
			setValidateCode(code, codeFormat);
		}
		
		
	}
	
	private void setCodeImage(String code, String codeFormat) {
		
		Bitmap bmp = utilities.encodeCodeToBitmap(code, codeFormat, getActivity());
		if (bmp != null) {
			this.code = code;
			this.codeFormat = codeFormat;
			barcodeImage.setImageBitmap(bmp);
		} else {
			// jesli niepoprawny kod
			this.code = "";
			this.codeFormat = "";
			isScanned = false;
			bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.zxinglib_icon); 
			barcodeImage.setImageBitmap(bmp);
	    	Toast.makeText(getActivity(), "Błąd skanowania lub niepoprawny kod", 2000).show();
		}
		
	}
	
	private void setValidateCode(String code, String codeFormat) {
		isScanned = true;
		if (utilities.validateCode(code, codeFormat)) { //jeśli kod zawiera produkt

			Product product = utilities.parseCodeToProduct(code);
			product.setCode(code);
			product.setCodeFormat(codeFormat);
			setViewsFromProduct(product);
						
			Log.i("Validate", "true");
		} else {
			Log.i("Validate", "false");
			setCodeImage(code, codeFormat);
		}
	}
		
	public void setCameraResult(Uri selectedImage) { 
        getActivity().getContentResolver().notifyChange(selectedImage, null);
        ContentResolver cr = getActivity().getContentResolver();
        Bitmap bitmap;
        try {
        	String path = utilities.getRealPathFromURI(selectedImage, getActivity());
        	bitmap = BitmapLoader.loadBitmap(path, 100, 100);
            obrazekImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Błąd aparatu!", Toast.LENGTH_SHORT).show();
            Log.e("Camera", e.toString());
        }
        
        takePictureStat = false;
	}
	
	public void setOkresWaz(String choosenDate) {
		okresWazTextBox.setOnKeyListener(null);
		
		String okres = utilities.parseDateToOkres(choosenDate);
		String box = utilities.getFirstValue(okres);
		String spinnItem = utilities.getSecondValue(okres);
		int spinnPos = utilities.getPosInSpinner(spinnItem, okresWazSpinner);
		
		okresWazTextBox.setText(box);
		okresWazSpinner.setSelection(spinnPos);	
		
		okresWazTextBox.setOnKeyListener(this);
	}
	
	private void setAlarms(ArrayList<HashMap<String, String>> przypomnienia, String nazwa, String productCode) {
		utilities.startAlarms(przypomnienia, nazwa, productCode, getActivity());
	}
	
	private String getKategoria() {
		int chosenKategoriaPos = kategorieSpinner.getSelectedItemPosition();
		String chosenKategoria = (String) kategorieSpinner.getItemAtPosition(chosenKategoriaPos);
		
		return chosenKategoria;
	}
	
	private String getTerminWaznosci() {

		return terminWazButton.getText().toString();
	}
	
	private ArrayList<HashMap<String, String>> getPrzypomnienia() {
		ArrayList<HashMap<String, String>> przypomnienia = new ArrayList<HashMap<String,String>>();
		int przypCount = przypLayout.getChildCount();
		
		for(int i = 0; i < przypCount; i++) {
			HashMap<String, String> przypomnienie = new HashMap<String, String>();
			View row = przypLayout.getChildAt(i);
			
			EditText przypTextBox = (EditText) row.findViewById(R.id.przypTextBox);
			Spinner przypSpinner = (Spinner) row.findViewById(R.id.przypSpinner);
			Button godzButton = (Button) row.findViewById(R.id.godzButton);
			
			String boxTxt = przypTextBox.getText().toString();
			
			if (boxTxt.length() != 0 & !boxTxt.equals("0")) {
				String spinnerPos = String.valueOf(przypSpinner.getSelectedItemPosition());
				String przypHour = godzButton.getText().toString();
				String terminWaz = terminWazButton.getText().toString();
				String przypDate = "0";
				try {
					long dateInMillis = utilities.parsePrzypmnienieToDate(boxTxt, spinnerPos, terminWaz, przypHour);
					przypDate = String.valueOf(dateInMillis);
				} catch (ParseException e) {
					Log.i("getPrzypomnienia", "parse to date error");
				}
				
				przypomnienie.put(PRZYP_TEXT_BOX, boxTxt);
				przypomnienie.put(PRZYP_SPINNER, spinnerPos);
				przypomnienie.put(PRZYP_HOUR, przypHour);
				przypomnienie.put(PRZYP_DATE, przypDate);
				
				przypomnienia.add(przypomnienie);
			}			
		}
		
		return przypomnienia;
	}
	
	private String getPeriodFromBoxAndSpinner(EditText box, Spinner spinner) {
		String[] formatArray = getResources().getStringArray(R.array.array_date);
		int chosenFormatPos = spinner.getSelectedItemPosition();
		
		String value = box.getText().toString();
		String format = formatArray[chosenFormatPos];
		
		String peroid = value + ":" + format;
		
		return peroid;
	}
							
	private void save() {		
		if (!checkFormIsFill()) {
			Toast.makeText(getActivity(), "Należy podać nazwę i okres ważności", 1500).show();
		} else if (code.equals("")) {	
			Product product = prepareDataToStore();
//			DialogGeneruj dialogGen = new DialogGeneruj(getActivity(),product , getFragmentManager(), getId());
//			dialogGen.show();	
			
//			TODO wyłączony dialog z generowaniem, za miast niego mozna dać opcję zapytania o wygenerowanie
//			aktualnie generuje kod w tle
			
			this.code = utilities.getJsonFromProduct(product);
			this.codeFormat = product.getCodeFormat();
			save();
		} else {
			saveData();
		}	
	
	}
	
	public void refreshKategorieSpinner(String category) {
		kategorie.remove(category);
		spinnerAdapter.notifyDataSetChanged();
	}
	
	private void showChoiceDialog(final String code, final String codeFormat) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setMessage("Produkt o takim kodzie znajduje się już w bazie. Możesz przejść do jego edycji lub do podglądu");
		dialog.setPositiveButton("Edytyj",new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dbAdapter.open();
				Product product = dbAdapter.getProduct(code);
				dbAdapter.close();
				switchToShowFragment(product);
			}
			
		});

		dialog.setNegativeButton("Podgląd",new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dbAdapter.open();
				Product product = dbAdapter.getProduct(code);
				dbAdapter.close();
				switchToEditFragment(product);
			}
			
		});

		dialog.show();
	}
	
	private boolean checkFormIsFill() {
		String nazwa = nazwaTextBox.getText().toString();
		String termin = terminWazButton.getText().toString();
		
		if (nazwa.equals("") || termin.equals("") || termin.equals("Wprowadź datę")) {
			return false;
		} else {	
			return true;
		}
	}
	
	private void switchToEditFragment(Product product) {
		((MainActivity) getActivity()).selectFragmentToEditProduct(product);
	}
	
	private void switchToShowFragment(Product product) {
		((MainActivity) getActivity()).selectFragmentToShowProduct(product);
	}
}
