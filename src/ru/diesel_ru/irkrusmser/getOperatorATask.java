package ru.diesel_ru.irkrusmser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.util.Log;

public class getOperatorATask extends AsyncTask<String, Void, String> {
    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() 
     * Љак использовать этот класс
     * public void onClick(View v) {
     * 	new DownloadImageTask().execute("http://example.com/image.png");
     * }*/
	
	final String LOG_TAG = "myLogs";
	
   protected String doInBackground(String... String) {
       // Џолучаем изображение капчи в отдельном потоке
		//String strPhoneNumber = getOperator(String[0]);
		//return getOperator(strPhoneNumber);
		return getOperator(String[0]);
   }

   private String getOperator(String _phone) {
			try {
   			// загрузка страницы
//   			URL url = new URL("http://www.irk.ru/sms/check/?number=" + _phone);
//   			URLConnection conn = url.openConnection();
//
//   			InputStreamReader rd = new InputStreamReader(conn.getInputStream());
//   			StringBuilder allpage = new StringBuilder();
//   			int n = 0;
//   			char[] buffer = new char[60];
//   			while (n >= 0)
//   			{
//   				n = rd.read(buffer, 0, buffer.length);
//   				if (n > 0)
//   				{
//   					allpage.append(buffer, 0, n);                    
//   				}
//   			}
   			
   			String url = "http://www.irk.ru/sms/check/?number=" + _phone;
   			 
   			HttpClient client = new DefaultHttpClient();
   			HttpGet request = new HttpGet(url);
   		 
   			// add request header
   			request.addHeader("User-Agent", MainActivity.USER_AGENT);
   			HttpResponse response = client.execute(request);
   		 
//   			System.out.println("Response Code : "  + response.getStatusLine().getStatusCode());
   		 
   			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
   		 
   			StringBuffer result = new StringBuffer();
   			String line = "";
   			while ((line = rd.readLine()) != null) {
   				result.append(line);
   			}
   			
   			//Log.d(LOG_TAG, "allpage = " + allpage.toString());
   			//return allpage.toString();
//   			System.out.println(result.toString());
   			return result.toString();
   			
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				//Log.d(LOG_TAG, "e1 = " + e.getMessage());
				return e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				//Log.d(LOG_TAG, "e2 = " + e.getMessage());
				return e.getMessage();
			}

   }
    
	/*
	@Override
   protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        // Џоказать диалог
        showDialog(PROGRESS_DLG_ID);
   }
	*/
   protected void onPostExecute(String result) {
	   //pdPNum.dismiss();
       //txtError.setText(result);
//   	if (result != null)
//   		MAX_LENGTH_SMS = Integer.parseInt(result);
       Log.d(LOG_TAG, "result = " + result);
	    try {
	        JSONObject json= (JSONObject) new JSONTokener(result).nextValue();
	        //String strOperator = (String) json.getString("operator");
	        String strLimit = (String) json.getString("limit");
	        //MAX_LENGTH_SMS = Integer.parseInt(strLimit);
	        MainActivity.setMaxLeghtSMS(Integer.parseInt(strLimit));
	        //Log.d(LOG_TAG, "Operator = " + strOperator);
	        //Log.d(LOG_TAG, "Limit = " + strLimit);
	    }
	    catch (JSONException e) {
	        e.printStackTrace();
	        //Log.i(LOG_TAG,e.toString());
	    }
	    
	    try {
	        JSONObject json= (JSONObject) new JSONTokener(result).nextValue();
	        String strError = (String) json.getString("error");
	        //Log.d(LOG_TAG, "Error = " + strError);
//	        txtPhoneNumber.setText("");
//	        txtError.setText(strError);
	        MainActivity.setError(strError);
	    }
	    catch (JSONException e) {
	        e.printStackTrace();
	    }
    // “ничтожить окно диалого
   	//dismissDialog(PROGRESS_DLG_ID);
   }
}
