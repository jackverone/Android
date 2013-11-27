package com.mareklatuszek.datywaznosci;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mareklatuszek.datywznosci.utilities.CommonUtilities;
import com.mareklatuszek.datywznosci.utilities.FinalVariables;

public class AdapterDB implements FinalVariables {
	private SQLiteDatabase db;
	private Activity mActivity;
	private HelperDB dbHelper;
	private CommonUtilities utilities = new CommonUtilities();
	
	public AdapterDB(Activity mActivity) {
	    this.mActivity = mActivity;
	}
	
	public AdapterDB open(){
	    dbHelper = new HelperDB(mActivity, DB_NAME, null, DB_VERSION);
	    try {
	        db = dbHelper.getWritableDatabase();
	    } catch (SQLException e) {
	        db = dbHelper.getReadableDatabase();
	    }
	    return this;
	}
	
	public void close() {
	    dbHelper.close();
	}
	
	public boolean insertProduct(Product product) {
	    ContentValues newProductVal = generateProductContentVal(product);
	    
	    return db.insert(DB_PRODUCT_TABLE, null, newProductVal) > 0;
	}
	
	public boolean insertCategory(String category) {
	    ContentValues newCategoryVal = new ContentValues();
	    newCategoryVal.put(DB_CAT_CATEGORY, category);
	    
	    Log.i("insertCat", category);
	    
	    return db.insert(DB_CATEGORIES_TABLE, null, newCategoryVal) > 0;
	}
	
	public boolean updateProduct(Product product, String oldCode) {
		ContentValues updateProductVal = generateProductContentVal(product);
		String where = DB_CODE + " = " + "'" + oldCode + "'";
		
	    return db.update(DB_PRODUCT_TABLE, updateProductVal, where, null) > 0;
	}
	
	public boolean updateCategory(String categoryOld, String categoryNew) {
		ContentValues updateCategoryVal = new ContentValues();
		updateCategoryVal.put(DB_CAT_CATEGORY, categoryNew);
		
		String whereCat = DB_CAT_CATEGORY + " = " + categoryOld;
		boolean categoryStatus = db.update(DB_CATEGORIES_TABLE, updateCategoryVal, whereCat, null) > 0; //aktualizacja tabeli z kategoriami
		
		ContentValues updateProductVal = new ContentValues();
		updateProductVal.put(DB_KATEGORIA, categoryNew);	
		String whereProd = DB_KATEGORIA + " = " + categoryOld;
		boolean productStatus = db.update(DB_PRODUCT_TABLE, updateProductVal, whereProd, null) > 0; //aktualizacja kategori w tebeli produkt�w
	
	    return categoryStatus & productStatus;
	}
	
	public boolean deleteProduct(Product product){
		String code = product.getCode();
	    String where = DB_CODE + "=" + "'" + code + "'";
	    return db.delete(DB_PRODUCT_TABLE, where, null) > 0;
	}
	
	public boolean deleteCategory(String categoryToDelete){
	    String whereCat = DB_CAT_CATEGORY + " =? ";
	    boolean caategoryStatus = db.delete(DB_CATEGORIES_TABLE, whereCat, new String[]{categoryToDelete}) > 0;
	    
		ContentValues updateProductVal = new ContentValues();
		updateProductVal.put(DB_KATEGORIA, "");	//ustawia pust� warto�� w kolumnie kategoria
		String whereProd = DB_KATEGORIA + " = '" + categoryToDelete + "'";
		boolean productStatus = db.update(DB_PRODUCT_TABLE, updateProductVal, whereProd, null) > 0; //aktualizacja kategori w tebeli produkt�w
	    
		Log.i("delCat", categoryToDelete);
		
	    return caategoryStatus & productStatus;
	}
	
	public boolean deletePrzypomnienie(String productCode, String alarmTimeInMillis){
		Product product = getProduct(productCode);
		ArrayList<HashMap<String, String>> przypomnienia = product.getPrzypomnienia();
		
		przypomnienia = utilities.removePrzypomnienie(przypomnienia, alarmTimeInMillis);
		product.setPrzypomnienia(przypomnienia);
		
		ContentValues updateProductVal = generateProductContentVal(product);
		String where = DB_CODE + " = " + "'" + productCode + "'";
		
	    return db.update(DB_PRODUCT_TABLE, updateProductVal, where, null) > 0;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<Product> getAllProducts() {
		ArrayList<Product> productList = new ArrayList<Product>();
		
		Cursor cursor = db.query(DB_PRODUCT_TABLE, null, null, null, null, null, null);
		mActivity.startManagingCursor(cursor);
		
		int columnNazwa =  cursor.getColumnIndex(DB_NAZWA);
		int columnDataOtwarcia =  cursor.getColumnIndex(DB_DATA_OTWARCIA);
		int columnOkresWaznosci =  cursor.getColumnIndex(DB_OKRES_WAZNOSCI);
		int columnTerminWaznosci =  cursor.getColumnIndex(DB_TERMIN_WAZNOSCI);
		int columnKategoria =  cursor.getColumnIndex(DB_KATEGORIA);
		int columnCode =  cursor.getColumnIndex(DB_CODE);
		int columnCodeFormat =  cursor.getColumnIndex(DB_CODE_FORMAT);
		int columnImage =  cursor.getColumnIndex(DB_OBRAZEK);
		int columnOpis =  cursor.getColumnIndex(DB_OPIS);
		int columnPrzypomnienia =  cursor.getColumnIndex(DB_PRZYPOMNIENIA);
		int columnDataZuzycia =  cursor.getColumnIndex(DB_DATA_ZUZYCIA);
		int columnIsScanned =  cursor.getColumnIndex(DB_IS_SCANNED);
		
		while(cursor.moveToNext()) {
			Product product = new Product();
			
			String nazwa = cursor.getString(columnNazwa);
			String dataOtwarcia = cursor.getString(columnDataOtwarcia);
			String okresWaznosci = cursor.getString(columnOkresWaznosci);
			String terminWaznosci = cursor.getString(columnTerminWaznosci);
			String kategoria = cursor.getString(columnKategoria);
			String code = cursor.getString(columnCode);
			String codeFormat = cursor.getString(columnCodeFormat);
			String image = cursor.getString(columnImage);
			String opis = cursor.getString(columnOpis);
			String przypomnienia = cursor.getString(columnPrzypomnienia);
			String dataZuz = cursor.getString(columnDataZuzycia);
			String isScanned = cursor.getString(columnIsScanned);
	        
			product.setNazwa(nazwa);
			product.setDataOtwarcia(dataOtwarcia);
			product.setOkresWaznosci(okresWaznosci);
			product.setTerminWaznosci(terminWaznosci);
			product.setKategoria(kategoria);
			product.setCode(code);
			product.setCodeFormat(codeFormat);
			product.setImage(image);
			product.setOpis(opis);
			product.setPrzypomnieniaFromDB((przypomnienia));
			product.setDataZuzycia(dataZuz);
			product.setIsScanned(isScanned);
			
			productList.add(product);
		}
		
		mActivity.stopManagingCursor(cursor);
		
		return productList;
	}
	
	@SuppressWarnings("deprecation")
	public ArrayList<String> getAllCategories() {
		ArrayList<String> categoriesList = new ArrayList<String>();
		
		Cursor cursor = db.query(DB_CATEGORIES_TABLE, null, null, null, null, null, null);
		mActivity.startManagingCursor(cursor);
		
		int columnCategory =  cursor.getColumnIndex(DB_CAT_CATEGORY);

		
		while(cursor.moveToNext()) {
			String category = cursor.getString(columnCategory);		
			categoriesList.add(category);
		}
		
		mActivity.stopManagingCursor(cursor);
		
		return categoriesList;
	}
	 
	public Product getProduct(String codeId) {
		Product product = new Product();
		
	    String where = DB_CODE + "=" + "'" + codeId + "'";
	    Cursor cursor = db.query(DB_PRODUCT_TABLE, null, where, null, null, null, null);
	    
	    if(cursor != null && cursor.moveToFirst()) {
	    	String nazwa = cursor.getString(cursor.getColumnIndex(DB_NAZWA));
			String dataOtwarcia = cursor.getString(cursor.getColumnIndex(DB_DATA_OTWARCIA));
			String okresWaznosci = cursor.getString(cursor.getColumnIndex(DB_OKRES_WAZNOSCI));
			String terminWaznosci = cursor.getString(cursor.getColumnIndex(DB_TERMIN_WAZNOSCI));
			String kategoria = cursor.getString(cursor.getColumnIndex(DB_KATEGORIA));
			String code = cursor.getString(cursor.getColumnIndex(DB_CODE));
			String codeFormat = cursor.getString(cursor.getColumnIndex(DB_CODE_FORMAT));
			String image = cursor.getString(cursor.getColumnIndex(DB_OBRAZEK));
			String opis = cursor.getString(cursor.getColumnIndex(DB_OPIS));
			String przypomnienia = cursor.getString(cursor.getColumnIndex(DB_PRZYPOMNIENIA));
			String dataZuzycia = cursor.getString(cursor.getColumnIndex(DB_DATA_ZUZYCIA));
			String isScanned = cursor.getString(cursor.getColumnIndex(DB_IS_SCANNED));
	        
			product.setNazwa(nazwa);
			product.setDataOtwarcia(dataOtwarcia);
			product.setOkresWaznosci(okresWaznosci);
			product.setTerminWaznosci(terminWaznosci);
			product.setKategoria(kategoria);
			product.setCode(code);
			product.setCodeFormat(codeFormat);
			product.setImage(image);
			product.setOpis(opis);
			product.setPrzypomnieniaFromDB(przypomnienia);
			product.setDataZuzycia(dataZuzycia);
			product.setIsScanned(isScanned);
			
	    }
	    
	    return product;
	}
	
	public boolean chckIfCategoryIsSetInProducts(String category) {
		
		Cursor cursor = db.query(DB_PRODUCT_TABLE, null, null, null, null, null, null);
		mActivity.startManagingCursor(cursor);
		
		int columnKategoria =  cursor.getColumnIndex(DB_KATEGORIA);
		
		while(cursor.moveToNext()) {
			
			String kategoria = cursor.getString(columnKategoria);
			if (kategoria.equals(category)) {
				mActivity.stopManagingCursor(cursor);
				return true;
			}
		}
		
		mActivity.stopManagingCursor(cursor);
		return false;
	}
	
	public boolean chckIfProductIsInDB(String code) {
		
		String where = DB_CODE + "=" + "'" + code + "'";
	    Cursor cursor = db.query(DB_PRODUCT_TABLE, null, where, null, null, null, null);
		mActivity.startManagingCursor(cursor);
		
		int columnCode =  cursor.getColumnIndex(DB_CODE);
		
		while(cursor.moveToNext()) {
			
			String codeDb = cursor.getString(columnCode);
			if (codeDb.equals(code)) {
				mActivity.stopManagingCursor(cursor);
				return true;
			}
		}
		
		mActivity.stopManagingCursor(cursor);
		return false;
	}
	
	private ContentValues generateProductContentVal(Product product) {
		ContentValues newProductVal = new ContentValues();
		
		String nazwa = product.getNazwa();
		String dataOtwarcia = product.getDataOtwarcia();
		String okresWaznosci = product.getOkresWaznosci();
		String terminWaznosci = product.getTerminWaznosci();
		String kategoria = product.getKategoria();
		String code = product.getCode();
		String codeFormat = product.getCodeFormat();
		String image = product.getImage();
		String opis = product.getOpis();
		String przypomnienia = product.getPrzypomnieniaToDB();
		String dataZuzycia = product.getDataZuzycia();
		String isScanned = product.getIsScannedToDB();
		
		Log.i("DBPrzypomnienia", przypomnienia);
	    
		newProductVal.put(DB_NAZWA, nazwa);
		newProductVal.put(DB_DATA_OTWARCIA, dataOtwarcia);
		newProductVal.put(DB_OKRES_WAZNOSCI, okresWaznosci);
		newProductVal.put(DB_TERMIN_WAZNOSCI, terminWaznosci);
		newProductVal.put(DB_KATEGORIA, kategoria);
		newProductVal.put(DB_CODE, code);
		newProductVal.put(DB_CODE_FORMAT, codeFormat);
		newProductVal.put(DB_OBRAZEK, image);
		newProductVal.put(DB_OPIS, opis);
		newProductVal.put(DB_PRZYPOMNIENIA, przypomnienia);
		newProductVal.put(DB_DATA_ZUZYCIA, dataZuzycia);
		newProductVal.put(DB_IS_SCANNED, isScanned);
		
		return newProductVal;
	}
	
}
