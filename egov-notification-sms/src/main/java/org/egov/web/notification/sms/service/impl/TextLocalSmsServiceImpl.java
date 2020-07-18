package org.egov.web.notification.sms.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.egov.web.notification.sms.config.SMSProperties;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
@Service
@ConditionalOnProperty(value = "sms.gateway.to.use", havingValue ="TEXTLOCAL_SMS", matchIfMissing = true)
@Slf4j
public class TextLocalSmsServiceImpl implements SMSService {
	
	@Autowired
	private SMSProperties smsProperties;

	@Override
	public void sendSMS(Sms sms) {
		
		if (!sms.isValid()) {
			log.error(String.format("Sms %s is not valid", sms));
			return;
		}
		submitToExternalSmsService(sms);
	}

	private void submitToExternalSmsService(Sms sms) {
		try {
			
		    String apiKey = "apikey=" + smsProperties.getSecureKey();
			String message = "&message=" + sms.getMessage();
			String sender = "&sender=" + smsProperties.getSenderid();
			String numbers = "&numbers=" +"91"+ sms.getMobileNumber();
			
			HttpURLConnection conn = (HttpURLConnection) new URL(smsProperties.getUrl()).openConnection();
			String data = apiKey + numbers + message + sender;
			//System.out.println("data "+data);
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
			log.error("Error occurred while sending SMS to " + sms.getMobileNumber(), e);
		}
		
	}

}



