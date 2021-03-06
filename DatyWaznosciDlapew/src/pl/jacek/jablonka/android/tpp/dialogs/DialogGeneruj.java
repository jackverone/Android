package pl.jacek.jablonka.android.tpp.dialogs;

import pl.jacek.jablonka.android.tpp.Product;
import pl.jacek.jablonka.android.tpp.R;
import pl.jacek.jablonka.android.tpp.TPPApp;
import pl.jacek.jablonka.android.tpp.atapters.AdapterDB;
import pl.jacek.jablonka.android.tpp.fragments.FragmentDodaj;
import pl.jacek.jablonka.android.tpp.fragments.FragmentEdytuj;
import pl.jacek.jablonka.android.tpp.fragments.FragmentProdukt;
import pl.jacek.jablonka.android.tpp.utilities.CommonUtilities;
import pl.jacek.jablonka.android.tpp.views.TextViewBariol;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DialogGeneruj extends Dialog implements android.view.View.OnClickListener {
	
	Fragment mFragment;
	Activity mActivity;
	Product product;
	CommonUtilities utilities = TPPApp.getUtilities();
	AdapterDB adapterDb;
	
	TextViewBariol nazwaTxt, dataTxt, terminWazTxt, pozostaleTxt, buttonTxt;;
	ImageView codeImage, viewToSet;
	ListView pozostaleList;
	LinearLayout generujButton;
	String codeFormat;
	String code;
	Bitmap bmp;
	
	boolean isGenerated = false;

	public DialogGeneruj(Fragment mFragment, Product product, ImageView viewToSet) {
		super(mFragment.getActivity());
		this.mFragment = mFragment;
		this.mActivity = mFragment.getActivity();
		this.product = product;
		this.viewToSet = viewToSet;
		adapterDb = new AdapterDB(mActivity);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_generuj);

		initPodstawowe();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.generujButton:
			if (isGenerated) {
				dismiss();
				saveProduct();
			} else {
				generateCode();
			}
			break;
		}
	}
	
	private void initPodstawowe() {		
		nazwaTxt = (TextViewBariol) findViewById(R.id.nazwaTxt);
		dataTxt = (TextViewBariol) findViewById(R.id.dataTxt);
		terminWazTxt = (TextViewBariol) findViewById(R.id.terminWazTxt);
		codeImage = (ImageView) findViewById(R.id.codeImage);
		generujButton = (LinearLayout) findViewById(R.id.generujButton);
		buttonTxt = (TextViewBariol) findViewById(R.id.buttonTxt);
		
		String nazwa = product.getNazwa();
		String data = utilities.getCurrentDate();
		String terminWaz = product.getTerminWaznosci();

		nazwaTxt.setText(nazwa);
		dataTxt.setText(data);
		terminWazTxt.setText(terminWaz);
		
		generujButton.setOnClickListener(this);
	}
		
	private void saveProduct() {
		viewToSet.setImageBitmap(bmp);
		if (mFragment instanceof FragmentProdukt) {	
			
			product.setCode(code);
			product.setCodeFormat(codeFormat);
			
			adapterDb.open();
			adapterDb.updateProduct(product);
			adapterDb.close();
			
		} else if (mFragment instanceof FragmentDodaj) {
			
			((FragmentDodaj) mFragment).code = code;
			((FragmentDodaj) mFragment).codeFormat = codeFormat;
			
		} else if (mFragment instanceof FragmentEdytuj) {
			
			((FragmentEdytuj) mFragment).code = code;
			((FragmentEdytuj) mFragment).codeFormat = codeFormat;
		}
	}
	
	private void generateCode() {
		code = utilities.getJsonFromProduct(product);
		codeFormat = product.getCodeFormat();
		bmp = utilities.encodeCodeToBitmap(code, codeFormat);
		codeImage.setImageBitmap(bmp);
		
		RelativeLayout relative = (RelativeLayout) findViewById(R.id.relative);
		View viewToRemove = findViewById(R.id.withoutBarcode);
		relative.removeView(viewToRemove);
		
		String save = mActivity.getString(R.string.save_button);
		buttonTxt.setText(save);
		isGenerated = true;
		
		String message = mActivity.getString(R.string.toast_generate_succes);
		Toast.makeText(mActivity, message, 2000).show();
	}
}