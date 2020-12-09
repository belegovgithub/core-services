package org.egov.web.notification.sms.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.egov.web.notification.sms.config.SMSProperties;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.BaseSMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
@Service
@ConditionalOnProperty(value = "sms.provider.class", havingValue ="TEXTLOCAL", matchIfMissing = true)
@Slf4j
public class TextLocalSmsServiceImpl extends BaseSMSService {
	
	@Autowired
	private SMSProperties smsProperties;

	protected void submitToExternalSmsService(Sms sms) {
		try {
			
		    String apiKey = "apikey=" + smsProperties.getSecureKey();
			String message = "&message=" + smsProperties.getSmsMsgAppend() + sms.getMessage();
			String sender = "&sender=" + smsProperties.getSenderid();
			String numbers = "&numbers=" +"91"+ sms.getMobileNumber();
			String data = apiKey + numbers + message + sender;
			if(smsProperties.isSmsEnabled()) {
				HttpURLConnection conn = (HttpURLConnection) new URL(smsProperties.getUrl()).openConnection();
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
				if(smsProperties.isDebugMsggateway())
				{
					log.info("sms response: " + stringBuffer.toString());
					log.info("sms data: " + data);
				}
				rd.close();
			}
			else {
				log.info("SMS Data: "+data);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error occurred while sending SMS to " + sms.getMobileNumber(), e);
		}
		
	}

}



