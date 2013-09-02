package ru.diesel_ru.irkrusmser;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FavContList extends ListActivity  {
	final String LOG_TAG = "myLogs";
	public final static String PHONE_NUMBER = "ru.diesel_ru.irkrusmser.PHONE_NUMBER";  
	ArrayList<String> favGroupId=new ArrayList<String>();
	ArrayList<String> favPhoneNum=new ArrayList<String>();
	AlertDialog.Builder  builder;
	String[] phone;
    // находим список
    //ListView lvMain = (ListView) findViewById(R.id.lvMain);
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getFavContacts();
    }

	//Получение избранных контактов 
	private void getFavContacts(){
		try {
			Cursor cursor = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, "starred=?", new String[] {"1"}, null);
	
	        while (cursor.moveToNext()) {
	        //String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
	        //Log.d(LOG_TAG,id);
	
	        String gTitle = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        //Log.d(LOG_TAG,gTitle);
	        favGroupId.add(gTitle);
	        }
	        
	        if(cursor != null && !cursor.isClosed()){
	        	cursor.close();
	        } 
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, favGroupId);
	        setListAdapter(adapter);
		}
		finally
		{
			
		}		
	}
	
	//Обработка выбора контакта
    @SuppressWarnings("deprecation")
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	try {
	        String item = (String) getListAdapter().getItem(position);
	        //Toast.makeText(this, item + " выбран", Toast.LENGTH_LONG).show();
	        Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?", new String[] {item}, null); 
	        while (phones.moveToNext()) { 
	        	String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));                 
	         	//Log.d(LOG_TAG,phoneNumber);
	         	favPhoneNum.add(phoneNumber);        	
	           	//Toast.makeText(this, phoneNumber, Toast.LENGTH_LONG).show();
	       	} 
	      	phones.close();
	      	showDialog(1);
		}
		finally
		{
			
		}
    }
    
    protected Dialog onCreateDialog(int id) {
		builder = new AlertDialog.Builder(this);
		//String[] phone;
		builder.setTitle("Выбирите номер"); // заголовок для диалога
		phone = new String[favPhoneNum.size()];
		//System.out.println("Total Item is: "+_onlineUsers.size());
		phone = favPhoneNum.toArray(phone);
		//System.out.println("USERS :"+_onlineUsers.toArray(users));
		builder.setItems(phone, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getApplicationContext(),"Выбранный # " + phone[item],Toast.LENGTH_SHORT).show();
				//Log.d(LOG_TAG,phone[item]);
				Intent answerInent = new Intent();
				answerInent.putExtra(PHONE_NUMBER, phone[item]);
				setResult(RESULT_OK, answerInent);
				finish();
			}
		});
		
		builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
    			//dialog.cancel();
    			builder.setCancelable(true);
    		}
		});
		builder.setCancelable(false);
		return builder.create();
    }
}
