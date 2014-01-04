package pl.mareklatuszek.tpp.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import pl.mareklatuszek.tpp.MainActivity;
import pl.mareklatuszek.tpp.TPPApp;
import pl.mareklatuszek.tpp.Product;
import pl.mareklatuszek.tpp.R;
import pl.mareklatuszek.tpp.atapters.AdapterCustomSpinner;
import pl.mareklatuszek.tpp.atapters.AdapterDB;
import pl.mareklatuszek.tpp.atapters.AdapterProductList;
import pl.mareklatuszek.tpp.atapters.ExpandableListTest;
import pl.mareklatuszek.tpp.dialogs.DialogDodajProdukt;
import pl.mareklatuszek.tpp.dialogs.DialogShare;
import pl.mareklatuszek.tpp.popups.PopupOverflow;
import pl.mareklatuszek.tpp.premium.PremiumUtilities;
import pl.mareklatuszek.tpp.utilities.CommonUtilities;
import pl.mareklatuszek.tpp.utilities.FinalVariables;
import pl.mareklatuszek.tpp.views.CustomSpinner;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FragmentProdukty extends SherlockFragment implements FinalVariables, OnClickListener {
	
	String spinnerTitle = "";
	
	AdapterDB dbAdapter;
	AdapterProductList listAdapter;
//	ExpandableListTest listAdapter;
	AdapterCustomSpinner adapterKategorieSpinner;
	ArrayList<Product> products = new ArrayList<Product>();
	ArrayList<Product> productsTemp = new ArrayList<Product>();
	ArrayList<String> categories = new ArrayList<String>();
	CommonUtilities utilities = TPPApp.getUtilities();
	FragmentManager fM;
	
//	ExpandableListView productsList;
	ListView productsList;
	View rootView;
	LinearLayout dodajLay, scanLay;
	CustomSpinner kategorieDropDown;
	PopupOverflow popupOverflow;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		fM = getFragmentManager();
		
		String title = getString(R.string.frag_products_title);
		utilities.setActionBarTitle(title, getSherlockActivity());
		
		spinnerTitle = getString(R.string.spinner_title_category);		
		dbAdapter = new AdapterDB(getActivity());

		rootView = inflater.inflate(R.layout.fragment_produkty, container, false);
		dodajLay = (LinearLayout) rootView.findViewById(R.id.dodajLay);
		scanLay = (LinearLayout) rootView.findViewById(R.id.scanLay);
		
		dodajLay.setOnClickListener(this);
		scanLay.setOnClickListener(this);

		new InitList().execute();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    android.view.MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.popup_product_list, menu);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		super.onContextItemSelected(item);
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
	    int pos = info.position;
	    
	    Product product = products.get(pos);
    	switch (item.getItemId()) {
    	case R.id.udostepnijPopup:
    		if (PremiumUtilities.APP_VERSION_NONE) {
    			String message = getString(R.string.toast_must_have_premium);
      		  	Toast.makeText(getActivity(), message, 2000).show();
      	  	} else {
      	  		DialogShare dialogShare = new DialogShare(getActivity(), product);
      	  		dialogShare.show();
      	  	} 
    		break;
    	case R.id.edytujPopup:
    		switchToEditFragment(product);
    		break;
    	case R.id.usunPopup:
    		deleteProduct(product);
    		break;
    	}
	    return true;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.items_product_list, menu);
      try {
    	  boolean deviceHasMenuButton = ViewConfiguration.get(getActivity()).hasPermanentMenuKey();
          if (!deviceHasMenuButton) {
        	  menu.removeItem(R.id.shareMenuButton);
        	  menu.removeItem(R.id.scanMenuButton);
        	  menu.removeItem(R.id.addMenuButton);
          }
      } catch (NoSuchMethodError e) {
    	  Log.i("Fragment Produkty", "onCreateOptionsMenu ERROR");
      }

      super.onCreateOptionsMenu(menu, inflater);
    }
            
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case R.id.shareMenuButton:
        	 onShare();
            break;
          case R.id.scanMenuButton:
        	  scanCode();
            break;
          case R.id.addMenuButton:
        	  selectFragmentDodaj();
            break;
          case R.id.overflow_product_list:        	  
        	  popupOverflow = new PopupOverflow(getActivity(), item, new OverflowListener());
        	  popupOverflow.showPopup();
        	  break;
       }
       return true;
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dodajLay:
			DialogDodajProdukt dialog = new DialogDodajProdukt(getActivity());
			dialog.show();
			break;
		case R.id.scanLay:
			scanCode();
		}
	}
	
	private class SpinnerListener implements OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
			kategorieDropDown.callOnItemClickListener(arg0, view, arg2, arg3);
			
			String choice = (String) view.getTag();
			products = getSortedList(choice);
			listAdapter = new AdapterProductList(getActivity(), products, fM, getId(), productsList);
//			listAdapter = new ExpandableListTest(FragmentProdukty.this, productsList, products);
			productsList.setAdapter(listAdapter);
		}		
	};
	
	private class OverflowListener implements OnClickListener {
		// listener menu actionbara
		@Override
		public void onClick(View v) {
			popupOverflow.dismiss();
			
			switch (v.getId()) {
			case 0:
				 onShare();
				break;
			case 1:
				scanCode();
				break;
			case 2:
				selectFragmentDodaj();
				break;	
			case 3:
				selectFragmentOAplikacji();
				break;
			}
		}
	};
	
	private ArrayList<Product> getSortedList(String choice) {
		if (choice.equals(spinnerTitle)) {
			return productsTemp;
		} else {
			ArrayList<Product> sorted = new ArrayList<Product>();
			for (int i = 0; i < productsTemp.size(); i++) {
				Product product = productsTemp.get(i);
				String category = product.getKategoria();
				if (category.equals(choice)) {
					sorted.add(product);
				}
			}
			return sorted;
		}		
	}

	private class InitSort extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected void onPreExecute() {			
			kategorieDropDown = (CustomSpinner) rootView.findViewById(R.id.kategorieDropDown);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				
				dbAdapter.open();
				categories = dbAdapter.getAllCategories();
				dbAdapter.close();
				
			} catch (Exception e) {
				// TODO
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			try {
				
				categories.add(0, spinnerTitle);			
				adapterKategorieSpinner = new AdapterCustomSpinner(getActivity(), categories);
				
				kategorieDropDown.setText(spinnerTitle);
				kategorieDropDown.setAdapter(adapterKategorieSpinner);
				kategorieDropDown.setOnItemSelectedListener(new SpinnerListener());
				
			} catch (NullPointerException e) {
				//TODO
			}
			
		}
	}
    
    private class InitList extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			productsList = (ListView) rootView.findViewById(R.id.productsList);
//			productsList = (ExpandableListView) rootView.findViewById(R.id.productsList);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				dbAdapter.open();
				products = dbAdapter.getAllProducts();
				productsTemp = products;
				dbAdapter.close();
			} catch (Exception e) {
				// TODO
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			if (!products.isEmpty()) {
				listAdapter = new AdapterProductList(getActivity(), products, fM, getId(), productsList);
//				listAdapter = new ExpandableListTest(FragmentProdukty.this, productsList, products);
				productsList.setAdapter(listAdapter);
				
				registerForContextMenu(productsList);
			}
			
			new InitSort().execute();// lista kategorii
		}
	}

	private void switchToEditFragment(Product product) {
		((MainActivity) getActivity()).selectFragmentToEditProduct(product);
	}
			
	public void deleteProduct(Product product) {
		dbAdapter.open();
		boolean deleteStatus = dbAdapter.deleteProduct(product);
		dbAdapter.close();
		
		if (deleteStatus) {
			products.remove(product);
			productsTemp.remove(product);
			listAdapter.notifyDataSetChanged();
			
			ArrayList<HashMap<String, String>> przypomnienia = product.getPrzypomnienia();
			String productId = product.getProductId();
			removeAlarms(przypomnienia, productId);
			
			listAdapter = new AdapterProductList(getActivity(), products, fM, getId(), productsList);
//			listAdapter = new ExpandableListTest(this, productsList, products);
			productsList.setAdapter(listAdapter);
			
			String message = getString(R.string.toast_delete_succes);
			Toast.makeText(getActivity(), message + " " + product.getNazwa(), 2000).show();
		} else {
			String message = getString(R.string.toast_delete_error);
			Toast.makeText(getActivity(), message, 2000).show();
		}
	}
	
	private void removeAlarms(ArrayList<HashMap<String, String>> przypomnienia, String productId) {
		utilities.cancelAlarms(przypomnienia, productId);
	}
		
	public void selectFragmentDodaj() {
		((MainActivity) getActivity()).selectFragment(SELECTION_ADD);
	}
	
	public void selectFragmentOAplikacji() {
		((MainActivity) getActivity()).selectFragment(SELECTION_ABOUT);
	}
	
	
	private void scanCode() {
		((MainActivity) getActivity()).selectFragment(SELECTION_SCAN);
	}
	
	private void onShare() {
		 if (products.size() > 0) {
   		  DialogShare dialogShare = new DialogShare(getActivity(), products);
         	  dialogShare.show();
   	  } else {
   		  String message = getString(R.string.toast_no_products_to_share);
   		  Toast.makeText(getActivity(), message, 2000).show();
   	  }
	}
}