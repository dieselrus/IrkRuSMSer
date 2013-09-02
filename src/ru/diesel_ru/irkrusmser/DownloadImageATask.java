package ru.diesel_ru.irkrusmser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;

public class DownloadImageATask extends AsyncTask<String, Void, Bitmap> {

	private String strCaptcha0 = "";
	private String _cookie = "";
	
    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() 
     * Љак использовать этот класс
     * public void onClick(View v) {
     * 	new DownloadImageTask().execute("http://example.com/image.png");
     * }*/
	
	//MainActivity ma = new MainActivity();
	
   protected Bitmap doInBackground(String... urls) {
       try {
       	// Џолучаем изображение капчи в отдельном потоке
       	strCaptcha0 = GetCaptchaPath(urls[0]);
       	MainActivity.setCaptcha0(strCaptcha0);
			//return getImageByUrl("http://irk.ru/captcha/image/" + strCaptcha0);
       	Bitmap img = getImageByUrl("http://irk.ru/captcha/image/" + strCaptcha0);
			
       	if(MainActivity.getBlClean()){
       		//imgCaptcha.setBackgroundColor(Color.WHITE);
       		//imgCaptcha.setImageBitmap(invert(CleanImage(CleanImage(adjustedContrast(invert(result),300)))));
       		//new getCapthaTask().execute(img);
       		return invert(CleanImage(CleanImage(adjustedContrast(invert(img),300))));
       	}
       	else{
       		return img;
       	}
       	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
   }
   
   private Bitmap getImageByUrl(String url) throws IOException,
		MalformedURLException {
		//‚от так можно получить изображение по url
		Bitmap image = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
		return image;
	}

   
   // Очистка изображения
   public Bitmap CleanImage(Bitmap src) { 	    		
		Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		int pixelColor;
		int height = src.getHeight();
		int width = src.getWidth();

		float[] pixelHSV = new float[3];
	     /*
	      * pixelHSV[0] : Hue (0 .. 360) 
	      * pixelHSV[1] : Saturation (0...1) 
	      * pixelHSV[2] : Value (0...1)
	      */
		
	    for (int y = 1; y < height - 1; y++) {
	        for (int x = 1; x < width - 1; x++) {
	        	int count = 0;
	            pixelColor = src.getPixel(x, y);
	            Color.colorToHSV(pixelColor, pixelHSV);
	            //System.out.println("pixelHSV[2]: " + pixelHSV[2]);
	            
	            if(pixelHSV[2] == 0){
//	            	System.out.println("pixelHSV[2] (x,y): " + pixelHSV[2]);
//	            	pixelHSV = new float[3];
	            	Color.colorToHSV(src.getPixel(x, y - 1), pixelHSV);
	            	//System.out.println("pixelHSV[2] (x,y - 1): " + pixelHSV[2]);
	                if (pixelHSV[2] == 0)
	                    count = count + 1;
	                
//	                pixelHSV = new float[3];
	                Color.colorToHSV(src.getPixel(x, y + 1), pixelHSV);
		            if (pixelHSV[2] == 0)
		                count = count + 1;
		            
//		            pixelHSV = new float[3];
		            Color.colorToHSV(src.getPixel(x - 1, y), pixelHSV);
	                if (pixelHSV[2] == 0)
	                    count = count + 1;
	                
//	                pixelHSV = new float[3];
	                Color.colorToHSV(src.getPixel(x + 1, y), pixelHSV);
	                if (pixelHSV[2] == 0)
	                    count = count + 1;
	            }
//	            System.out.println("count: " + count);
	            
	            if(count > 2)
	                //imgc.putpixel((x,y), 0)
	                output.setPixel(x, y, Color.argb(255, 255, 255, 255));	           
	        }
	    }

	    return output;
	} 
   
   // Инвентирование изображения
   public Bitmap invert(Bitmap src) {
		Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		int A, R, G, B;
		int pixelColor;
		int height = src.getHeight();
		int width = src.getWidth();

	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            pixelColor = src.getPixel(x, y);
	            A = Color.alpha(pixelColor);
	            
	            R = 255 - Color.red(pixelColor);
	            G = 255 - Color.green(pixelColor);
	            B = 255 - Color.blue(pixelColor);
	            
	            output.setPixel(x, y, Color.argb(A, R, G, B));
	        }
	    }

	    return output;
	}  
   
   // Изменение контраста
   private Bitmap adjustedContrast(Bitmap src, double value)
   {
       // image size
       int width = src.getWidth();
       int height = src.getHeight();
       // create output bitmap

       // create a mutable empty bitmap
       Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

       // create a canvas so that we can draw the bmOut Bitmap from source bitmap
       Canvas c = new Canvas();
       c.setBitmap(bmOut);

       // draw bitmap to bmOut from src bitmap so we can modify it
       c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));


       // color information
       int A, R, G, B;
       int pixel;
       // get contrast value
       double contrast = Math.pow((100 + value) / 100, 2);

       // scan through all pixels
       for(int x = 0; x < width; ++x) {
           for(int y = 0; y < height; ++y) {
               // get pixel color
               pixel = src.getPixel(x, y);
               A = Color.alpha(pixel);
               // apply filter contrast for every channel R, G, B
               R = Color.red(pixel);
               R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
               if(R < 0) { R = 0; }
               else if(R > 255) { R = 255; }

               G = Color.green(pixel);
               G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
               if(G < 0) { G = 0; }
               else if(G > 255) { G = 255; }

               B = Color.blue(pixel);
               B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
               if(B < 0) { B = 0; }
               else if(B > 255) { B = 255; }

               // set new pixel color to output bitmap
               bmOut.setPixel(x, y, Color.argb(A, R, G, B));
           }
       }
       return bmOut;
   }
       		
	// ”ункциЯ получениЯ ссылки на картинку капчи
	public String GetCaptchaPath(String urlsite) 
	{
		String matchtemper = "";
		try
		{
			// загрузка страницы
			URL url = new URL(urlsite);
			URLConnection conn = url.openConnection();

			// Save Cookie
			String headerName = null;
			//_cookies.clear();
			if (MainActivity.getCoockie() == "") {
				for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
					if (headerName.equalsIgnoreCase("Set-Cookie")) 
					{    
						String cookie = conn.getHeaderField(i);
						this._cookie += cookie.substring(0,cookie.indexOf(";")) + "; ";
					}
				}
				
				MainActivity.setCoockie(_cookie);
			}
               
			InputStreamReader rd = new InputStreamReader(conn.getInputStream());
			StringBuilder allpage = new StringBuilder();
			int n = 0;
			char[] buffer = new char[40000];
			while (n >= 0)
			{
				n = rd.read(buffer, 0, buffer.length);
				if (n > 0)
				{
					allpage.append(buffer, 0, n);                    
				}
			}
			// работаем с регулЯрками
			final Pattern pattern = Pattern.compile ("/captcha/image/([a-z0-9]+)/");
			Matcher matcher = pattern.matcher(allpage.toString());
			if (matcher.find())
			{    
				matchtemper = matcher.group(1);            
			}        
			return matchtemper;
		}
		catch (Exception e)
		{
               
		}
		return matchtemper; 
	};  
	/*
	@Override
   protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        // Џоказать диалог
        showDialog(PROGRESS_DLG_ID);
   }
	*/
   protected void onPostExecute(Bitmap result) {
   	//pd.dismiss();        
   	
//   	if(ma.getBlClean()){
//   		imgCaptcha.setBackgroundColor(Color.WHITE);
//   		//imgCaptcha.setImageBitmap(invert(CleanImage(CleanImage(adjustedContrast(invert(result),300)))));
//   	}
//   	else{
//   		imgCaptcha.setBackgroundColor(Color.alpha(0));       		
//   	}
//   	
//   	imgCaptcha.setImageBitmap(result);
	   
	   MainActivity.setBitmapCaptcha(result);
	   
//       imgStatus.setVisibility(View.INVISIBLE);
    // “ничтожить окно диалого
   	//dismissDialog(PROGRESS_DLG_ID);
   }

}
