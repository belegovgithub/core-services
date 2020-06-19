package org.egov.web.notification.sms.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.SMSService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
@Service
@ConditionalOnProperty(value = "sms.gateway.to.use", havingValue ="DEFAULT", matchIfMissing = true)
public class TextLocalImpl implements SMSService {

	@Override
	public void sendSMS(Sms sms) {
		// TODO Auto-generated method stub
		try {
			// Construct data
			//commented this to avoid sending of sms till demo day
			//String apiKey = "apikey=" + "";
		    String apiKey = "apikey=" + "YW7j9bfyV6A-FDzcLlUcEbQMFLCkhmu5C0fz6wca3i";
			String message = "&message=" + sms.getMessage();
			String sender = "&sender=" + "TXTLCL";
			String numbers = "&numbers=" +"91"+ sms.getMobileNumber();
			// Send data"91"
			HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
			String data = apiKey + numbers + message + sender;
			System.out.println("data "+data);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
			conn.getOutputStream().write(data.getBytes("UTF-8"));
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				stringBuffer.append(line);
			}
			rd.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error SMS ");
			
		}
		
	}

}



