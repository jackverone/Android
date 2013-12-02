package com.mareklatuszek.datywaznosci;

import jim.h.common.android.lib.zxing.integrator.IntentIntegrator;
import jim.h.common.android.lib.zxing.integrator.IntentResult;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mareklatuszek.utilities.FinalVariables;


public class MainActivity extends SherlockFragmentActivity implements FinalVariables {

	ListView menuList;
	AdapterMenu menuAdapter;
	AdapterDB adapterDb;
	SlidingMenu menu;
	
	boolean backIsDoublePressed = false;
	public static int currentFragmentPos = 2;
	public static int currentFragmentId;
	public static Uri imageUri = new Uri.Builder().build();
	public static boolean notification = false;
	
	Fragment fragmentDodaj = new FragmentDodaj();
	Fragment fragmentProdukty = new FragmentProdukty();
	Fragment fragmentKategorie = new FragmentKategorie();
	Fragment fragmentPrzypomnienia = new FragmentPrzypomnienia();
	Fragment fragmentProdukt = new FragmentProdukt();
	Fragment fragmentEdytuj;
	
    @Override   
    public void onResume()
    {
    	super.onResume();
    	
    	if(notification)
    	{
    		String productCode = getIntent().getStringExtra("productCode");
            String timeInMillis = getIntent().getStringExtra("timeInMillis");
            notification = false;
            if (productCode != null & timeInMillis != null) {
            	
        		onNotification(productCode, timeInMillis);
            } else {
            	//TODO
            }
    	}
    	
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		adapterDb = new AdapterDB(this);
				
		setContentView(R.layout.main_frame);
	
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		initMenu();
		
		if (savedInstanceState == null) {
			selectFragment(2);	
			menu.toggle();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			menu.toggle();
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {   
		if (resultCode == RESULT_OK) {
			if (requestCode == CAMERA_ADD_RQ_CODE) {
				imageUri = intent.getData();
				setPictureInFragmentDodaj();
			} else if (requestCode == GALLERY_ADD_RQ_CODE) {
				imageUri = intent.getData();
				setPictureInFragmentDodaj();
			} else if (requestCode == CAMERA_EDIT_RQ_CODE) {
				imageUri = intent.getData();
				setPictureInFragmentEdytuj();
			} else if (requestCode == GALLERY_EDIT_RQ_CODE) {
				imageUri = intent.getData();
				setPictureInFragmentEdytuj();
	        } else {
	        	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		        if (scanResult != null) {
		        	String code = scanResult.getContents();
		        	String codeFormat = scanResult.getFormatName();
		        	if(code != null & codeFormat != null) {
		        		selectFragmentToStoreCode(code, codeFormat);  
		        	}     	       	
		        }
	        }
		}

	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
	    super.onAttachFragment(fragment);
	    currentFragmentId = fragment.getId();
	}
	
	@Override
    public void onBackPressed() {		
		if (currentFragmentPos == 6) { // jesli fragment edytuj			
			Bundle extras = fragmentEdytuj.getArguments();
			
			if (extras != null) {
				Product product = (Product) extras.getSerializable("product");
				selectFragmentToShowProduct(product);
			}			
		} else if (currentFragmentPos == 5) { //jesli fragment produkt			
			selectFragment(2);			
		} else {
			
	        if (backIsDoublePressed) {
	            super.onBackPressed();
	            return;
	        }
	        
	        backIsDoublePressed = true;
	        Toast.makeText(this, "Naciśnij wstecz drugi raz aby wyjść", 1500).show();
	        new Handler().postDelayed(new Runnable() {

	            @Override
	            public void run() {
	             backIsDoublePressed = false;   
	            }
	        }, 2000);
		}
    } 
	
	private void initMenu() {
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(getMenuList());
	}
	

	private class MenuItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
			selectFragment(position);
			menu.toggle();
		}
	}

	public void selectFragment(int position) {		
		currentFragmentPos = position;

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch (position) {
		case 0:
			startScanner();
			break;
		case 1:
			fragmentDodaj = new FragmentDodaj();
			ft.replace(R.id.content_frame, fragmentDodaj);
			break;
		case 2:
			ft.replace(R.id.content_frame, fragmentProdukty);
			break;
		case 3:
			ft.replace(R.id.content_frame, fragmentKategorie);
			break;
		case 4:
			ft.replace(R.id.content_frame, fragmentPrzypomnienia);
			break;
		case 5:
			ft.replace(R.id.content_frame, fragmentProdukt);
			ft.commit();
			return;
		case 6:
			ft.replace(R.id.content_frame, fragmentEdytuj);
			ft.commit();
			return;
		}
		ft.commit();	
	}
	
	private void onNotification(String productId, String alarmTime) {	
		adapterDb.open();
		Product product = adapterDb.getProduct(productId);
		adapterDb.close();
		
		DialogPrzypomnienie dialogPrzypomnienie = new DialogPrzypomnienie(this, product);
		dialogPrzypomnienie.show();
		
//		removePrzypomnienie(productId, alarmTime);
	}
	
	public void startScanner() {
		if (currentFragmentPos != 1) {
            selectFragment(1);
        }		
		IntentIntegrator.initiateScan(MainActivity.this);
	}
	
	private void selectFragmentToStoreCode(String code, String codeFormat) {
		Log.i("kod", code);
		Log.i("fromat kodu", codeFormat);
		       
        if (currentFragmentPos == 1) {
        	FragmentManager fragmentManager = getSupportFragmentManager();
        	FragmentDodaj actualFragment = (FragmentDodaj) fragmentManager.findFragmentById(currentFragmentId);
        	actualFragment.setDataFromScan(code, codeFormat);

        } else {
        	Bundle data = new Bundle();
            data.putString("scanResultCode", code);
            data.putString("scanResultCodeFormat", codeFormat);
            
        	fragmentDodaj.setArguments(data);
            selectFragment(1);
        }		
	}
	
	private void setPictureInFragmentDodaj() {
		Log.i("picture", "taken");
	       
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentDodaj actualFragment = (FragmentDodaj) fragmentManager.findFragmentById(currentFragmentId);
        actualFragment.setCameraResult();      
	}
	
	private void setPictureInFragmentEdytuj() {
		Log.i("picture", "taken");
	       
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentEdytuj actualFragment = (FragmentEdytuj) fragmentManager.findFragmentById(currentFragmentId);
        actualFragment.setCameraResult();      
	}
	

	private FrameLayout getMenuList() {
		String[] title = getResources().getStringArray(R.array.array_menu_titles);
		String[] subtitle = getResources().getStringArray(R.array.array_menu_subtitles);
		int[] icon = new int[] { R.drawable.collections_cloud, R.drawable.collections_cloud,
				R.drawable.collections_cloud, R.drawable.collections_cloud, R.drawable.collections_cloud };
		
		LayoutInflater li = getLayoutInflater();
		FrameLayout menuFrame = (FrameLayout) li.inflate(R.layout.menu_frame, null);

		menuAdapter = new AdapterMenu(MainActivity.this, title, subtitle, icon);
		
		menuList = (ListView) menuFrame.findViewById(R.id.listview_drawer);
		menuList.setAdapter(menuAdapter);
		menuList.setOnItemClickListener(new MenuItemClickListener());
		menuList.setDivider(null);
				
		return menuFrame;
	}
		
	public void selectFragmentToShowProduct(Product product) {
		Bundle data = new Bundle();
        data.putSerializable("product", product);

        fragmentProdukt.setArguments(data);
        selectFragment(5);		
	}
	
	public void selectFragmentToEditProduct(Product product) {
		Bundle data = new Bundle();
        data.putSerializable("product", product);
        fragmentEdytuj = new FragmentEdytuj();
        fragmentEdytuj.setArguments(data);
        selectFragment(6);		
	}
	
	public void removeFragmentEdytuj() {
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.remove(fragmentEdytuj).commit();
	}
	
	private void removePrzypomnienie(String productId, String alarmTime) {
		adapterDb.open();
		boolean removeStatus = adapterDb.deletePrzypomnienie(productId, alarmTime);
		adapterDb.close();	
	}
	
	private boolean chcekIfCodeIsInDB(String code) {
		adapterDb.open();
		boolean status = adapterDb.chckIfProductIsInDB(code);
		adapterDb.close();
		return status;
	}
			
}
