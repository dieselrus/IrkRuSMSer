package ru.diesel_ru.irkrusmser;

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MainActivity extends Activity {
	protected static final int PICK_RESULT = 0;
	protected static final int ReqCodeContact = 0;
	static final private int PHONE_NUMBER = 3;
	static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2"; 
	
	TextView txtPhoneNumber;
	TextView txtSMSText;
	static TextView txtCaptcha1;
	static TextView txtError;
	TextView tvMessageText;
	Button buttonSend;
	Button buttonClean;
	Button buttonSelectContact;
	Button buttonSelectFavoritesContact;
	Button buttonSendFriend;
	static ImageView imgCaptcha;
	static ImageView imgStatus;
	
	AdView adView;
	
    private static String _cookie = "";
    private static String strCaptcha0 = "";
    private String strMyName = "";
    private static boolean blClean = false;
    private boolean blCleaningCache = false;
    private static int MAX_LENGTH_SMS = 120;
    
    //Диалоговое окно прогресс бара
    final int PROGRESS_DLG_ID = 666;
    //Диалог ожиданиЯ
    private static ProgressDialog pd;
    private static ProgressDialog pdSMS;
    private static ProgressDialog pdPNum;
    
    //ДлЯ сохранениЯ настроек
    SharedPreferences sp;
    final String LOG_TAG = "myLogs";
    
	// Определяем подключены ли к интернету
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo nInfo = cm.getActiveNetworkInfo();
	    if (nInfo != null && nInfo.isConnected()) {
//	        Log.i(LOG_TAG, "ONLINE");
	        return true;
	    }
	    else {
//	        Log.i(LOG_TAG, "OFFLINE");
	        return false;
	    }
	}
	
	// Удаление дириктории кэша
	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}
	
	// Получение папки кэша приложения
	public void clearApplicationData() {
		File cache = getCacheDir();
		File appDir = new File(cache.getParent());
		if (appDir.exists()) {
			String[] children = appDir.list();
			for (String s : children) {
				if (!s.equals("lib") & !s.equals("shared_prefs")) {
					deleteDir(new File(appDir, s));
					//Log.i(LOG_TAG, "**************** File /data/data/APP_PACKAGE/" + s + " DELETED appDir = " + appDir);
				}
			}
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);       
        
        /*  Аагружаем настройки. Если настроек с таким именем нету - 
        возвращаем второй аргумент. ‚ данном случае пробел.  */		
        
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        strMyName = sp.getString("Name","");
        blClean = sp.getBoolean("Clean", false);
        blCleaningCache = sp.getBoolean("CleaningCache", false);
        
        // Очищаем кэш приложения
        if(blCleaningCache)     
	        clearApplicationData();
        
        new ConnectivityReceiver();
        //Создание adView
        adView = new AdView(this, AdSize.BANNER, "a1510fa3b8c4d5e");
        										    
        // Поиск в LinearLayout (предполагается, что был назначен
        // атрибут android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout)findViewById(R.id.admobLayout);

        // Добавление adView
        layout.addView(adView);

        // Инициирование общего запроса на загрузку вместе с объявлением
        adView.loadAd(new AdRequest());
        
        //isOnline();
        // найдем View-элементы
        txtPhoneNumber = (TextView) findViewById(R.id.editPhoneNumber);
        txtSMSText = (TextView) findViewById(R.id.editMessage);
        //txtName = (TextView) findViewById(R.id.editName);
        txtCaptcha1 = (TextView) findViewById(R.id.editCaptcha1);
        txtError = (TextView) findViewById(R.id.textError);
        tvMessageText = (TextView) findViewById(R.id.textView2);
        
        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSelectContact = (Button) findViewById(R.id.btnContacts);
        buttonSelectFavoritesContact = (Button) findViewById(R.id.btnFContacts);
        buttonClean = (Button) findViewById(R.id.btnClean);
        buttonSendFriend = (Button) findViewById(R.id.sendFriend);
        imgCaptcha = (ImageView) findViewById(R.id.imageCaptcha1);
        imgStatus = (ImageView) findViewById(R.id.imageStatus);        
        
        // Установка максимальной длины строки для текста СМС
        int maxLength = MAX_LENGTH_SMS - strMyName.length();
        
        Intent localIntent = getIntent();
        if (localIntent.getAction().contains("android.intent.action.SENDTO")){
        	txtPhoneNumber.setText(Uri.decode(localIntent.getData().toString()).replace("smsto:", "").replace("sms:", "").replace("+7", "8").replace("-", "").replace(" ", ""));
        	//txtPhoneNumber.setText(Uri.decode(localIntent.getData().toString()).replace("sms:", ""));
        }
        
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        txtSMSText.setFilters(fArray);
        
        // Очищаем кэш приложения
        //clearApplicationData();
        
        // Проверка на подключение к Интернет
        if (isOnline() == true){
        	pd = ProgressDialog.show(MainActivity.this, "Подождите...", "Получение пин-кода", true, false);
        	// Получаем капчу
        	//new DownloadImageTask().execute("http://irk.ru/sms");
        	new DownloadImageATask().execute("http://irk.ru/sms");
        }
        else{
        	txtError.setText("Вы не подключены к сети Интернет.");
        }  
        // Обработчик нажатиЯ на кнопку отправить
        buttonSend.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (txtPhoneNumber.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "Введите номер телефона!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtSMSText.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "Введите текст СМС!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtCaptcha1.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "Введите пин-код!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (isOnline() == true){
	        		String data_s = "csrfmiddlewaretoken=" + GetToken(_cookie) + "&number=" + txtPhoneNumber.getText() + "&message=" + txtSMSText.getText() + "\n" + strMyName + "&captcha_0=" + strCaptcha0 + "&captcha_1=" + txtCaptcha1.getText();
	        		imgStatus.setVisibility(View.INVISIBLE);
	        		pdSMS = ProgressDialog.show(MainActivity.this, "Подождите...", "Отправка СМС", true, false);
	        		//new SendSMSTask().execute("http://irk.ru/sms/?", data_s);
	        		new SendSMSATask().execute("http://irk.ru/sms/?", data_s);
	        		//new SendSMSTask().execute("http://irk.ru/sms/?", GetToken(_cookie), txtPhoneNumber.getText().toString(), txtSMSText.getText().toString() + "\n" + strMyName, strCaptcha0, txtCaptcha1.getText().toString());
	        			//Log.v("status", "SEND");
	        		//pd = ProgressDialog.show(MainActivity.this, "Подождите...", "Џолучение пин-кода", true, false);
	        		// Получаем капчу
	        		//new DownloadImageTask().execute("http://irk.ru/sms");
	        		new DownloadImageATask().execute("http://irk.ru/sms");
	        		
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "Вы не подключены к сети Интернет.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // Обработчик нажатия на капчу (обновление капчи)
        imgCaptcha.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (isOnline() == true){
        			pd = ProgressDialog.show(MainActivity.this, "Подождите...", "Получение пин-кода", true, false);
	        		// Џолучаем капчу
        			//new DownloadImageTask().execute("http://irk.ru/sms");
        			new DownloadImageATask().execute("http://irk.ru/sms");
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "Вы не подключены к сети Интернет.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // Обработчик выбора контакта
        buttonSelectContact.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		// Выбор только контактов звонков (без почтовых)
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(pickIntent, PICK_RESULT);
                imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
        
        // Обработчик выбора избранного контакта
        buttonSelectFavoritesContact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, FavContList.class);
			    startActivityForResult(intent, PHONE_NUMBER);
			    imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
        
        // Обработчик нажатия на кнопку очистки
        buttonClean.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		txtSMSText.setText("");
        		imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
         
        // Обработчик нажатия на кнопку отправки другу
        buttonSendFriend.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		txtSMSText.setText("Отправляй СМС бесплатно! https://play.google.com/store/apps/details?id=ru.diesel_ru.irkrusmser");
        	}
        });
        
        //Обработка ввода символов в текстовое поле для текста СМС
//		tvMessageText.setText("@+id/editPhoneNumber" + " (" + String.valueOf(200 - strMyName.length() - txtSMSText.length()) + ")");
        txtSMSText.addTextChangedListener(new TextWatcher()  {
			@Override
			public void afterTextChanged(Editable s) {
				tvMessageText.setText(getResources().getString(R.string.MessageText) + " (" + String.valueOf(MAX_LENGTH_SMS - strMyName.length() - txtSMSText.length()) + ")");
			}
 
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
	
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

        });       
        // Обработка поления фокуса полем для текста СМС
        txtSMSText.setOnFocusChangeListener(new OnFocusChangeListener() {		
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//Log.i(LOG_TAG, "hasFocus = " + hasFocus);
				if ((txtPhoneNumber.length() > 0) & hasFocus){
					pdPNum = ProgressDialog.show(MainActivity.this, "Подождите...", "Идет определение оператора", true, false);
					//new getOperatorTask().execute(txtPhoneNumber.getText().toString());
					new getOperatorATask().execute(txtPhoneNumber.getText().toString());
					//new getOperatorTask().execute("9149506721");
				}
				imgStatus.setVisibility(View.INVISIBLE);
			}
		});
    }
  
    // Закрытие приложениЯ
	@Override
    protected void onStop(){
       super.onStop();
    }
    
	@Override
    protected void onResume() {
		//Log.d(LOG_TAG, "onResume");
        strMyName = sp.getString("Name", "");
        blClean = sp.getBoolean("Clean", false);
        blCleaningCache = sp.getBoolean("CleaningCache", false);
        
		super.onResume();
    }

    @Override
    protected void onPause() {
		//Log.d(LOG_TAG, "onPause");
		super.onPause();
    }
    
    @Override
    protected void onStart() {
		//Log.d(LOG_TAG, "onStart");
		strMyName = sp.getString("Name", "");
		blClean = sp.getBoolean("Clean", false);
		blCleaningCache = sp.getBoolean("CleaningCache", false);
		
		super.onStart();
    }
    
    // Обработка выбора контакта
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	//Log.v("requestCode", String.valueOf(requestCode));
    	//Log.v("resultCode", String.valueOf(requestCode));
    	//Log.v("data", String.valueOf(requestCode));
    	// Получаем номер из избранных контактов
    	if (requestCode == PHONE_NUMBER) {
    		if (resultCode == RESULT_OK) {
    			String thiefname = data.getStringExtra(FavContList.PHONE_NUMBER);
    			txtPhoneNumber.setText(thiefname.replace("+7", "8").replace("-", "").replace(" ", ""));
    		}else {
    			txtPhoneNumber.setText(""); // стираем текст
    		}
    	}
    	// Получаем номер из активити контактов
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                	c = getContentResolver().query(uri, new String[]{ 
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE},
                        null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        showSelectedNumber(type, number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    // присваивание значениЯ номера телефона 
    public void showSelectedNumber(int type, String number) {
    	txtPhoneNumber.setText(number.replace("+7", "8").replace("-", "").replace(" ", ""));      
    }

	// Џолучаем токен из куки
	public String GetToken(String data)
	{
		String matchtoken = "";
		// работаем с регулЯрками
		//Log.v("coocies",data);
		final Pattern pattern = Pattern.compile ("csrftoken=([a-zA-Z0-9]+);");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find())
		{    
			matchtoken = matcher.group(1);            
		} 
		//Log.v("coocies_t",matchtoken);
		return matchtoken;
	}
    
	// Создание меню	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //menu.add("menu1");
        //return true;
	      MenuItem mi = menu.add(0, 1, 0, "Настройки");
	      mi.setIntent(new Intent(this, PrefActivity.class));
	      return super.onCreateOptionsMenu(menu);
    }
	
	// Устанавливаем куки
	public static void setCoockie (String cookie){
		_cookie = cookie;		
	}
	
	// Получаем куки
	public static String getCoockie (){
		return _cookie;		
	}
	
	// Проверяем включена ли очистка капчи
	public static boolean getBlClean (){
		return blClean;		
	}
	
	// Устанавливаем изображение капчи
	public static void setBitmapCaptcha (Bitmap img){
	   	if(blClean){
	   		imgCaptcha.setBackgroundColor(Color.WHITE);
	   		//imgCaptcha.setImageBitmap(invert(CleanImage(CleanImage(adjustedContrast(invert(result),300)))));
	   	}
	   	else{
	   		imgCaptcha.setBackgroundColor(Color.alpha(0));       		
	   	}
	   	
	   	pd.dismiss();
	   	setError("");
	   	imgCaptcha.setImageBitmap(img);	
	}
	
	/**
	 * Процедура устанавливает статус отправленного сообщения
	 */
	public static void setMessageStatus(Boolean _status){
		// Уничтожить окно диалого
    	pdSMS.dismiss();
    	
		if (_status)
		{    
			//matchtoken = matcher.group(1); 
			imgStatus.setVisibility(View.VISIBLE);
			//Log.v("matcher", matchtoken);
			setError("Сообщение доставлено на сервер.");
		} 
		else
		{
			setError("Возможно сообщение не доставлено на сервер.");
//			imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.error));
//			imgStatus.setVisibility(View.VISIBLE);
		}
		
    	txtCaptcha1.setText("");
	}

	// Установить переменную капчи
	public static void setCaptcha0(String str) {
		strCaptcha0 = str;
	}
	
	public static void setMaxLeghtSMS(int parseInt) {
		pdPNum.dismiss();
		MAX_LENGTH_SMS = parseInt;
	}

	public static void setError(String strError) {
		txtError.setText(strError);
	}
}
