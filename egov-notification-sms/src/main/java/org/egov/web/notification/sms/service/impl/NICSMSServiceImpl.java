package org.egov.web.notification.sms.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.egov.web.notification.sms.config.SMSProperties;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Service
@ConditionalOnProperty(value = "sms.gateway.to.use", havingValue = "NIC", matchIfMissing = true)
@Slf4j
public class NICSMSServiceImpl implements SMSService {
    
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
        	
        	String final_data="";
        	final_data+="username="+ smsProperties.getUsername();
        	final_data+="&pin="+ smsProperties.getPassword();
        	final_data+="&message="+ sms.getMessage();
        	final_data+="&mnumber=91"+ sms.getMobileNumber();
        	final_data+="&signature="+ smsProperties.getSenderid();
        	
        	KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    		File file = new File(System.getenv("JAVA_HOME")+"/lib/security/cacerts");
            InputStream is = new FileInputStream(file);
    		trustStore.load(is, "changeit".toCharArray());
    		TrustManagerFactory trustFactory = TrustManagerFactory
    				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    		trustFactory.init(trustStore);
    		
    		TrustManager[] trustManagers = trustFactory.getTrustManagers();
    		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    		
    		sslContext.init(null, trustManagers, null);
    		SSLContext.setDefault(sslContext);
			//System.out.println("ssl check done. URL about to hit : "+smsProperties.getUrl()+final_data);
			HttpsURLConnection conn = (HttpsURLConnection) new URL(smsProperties.getUrl()).openConnection();
			conn.setSSLSocketFactory(sslContext.getSocketFactory());
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Length", Integer.toString(final_data.length()));
			conn.getOutputStream().write(final_data.getBytes("UTF-8"));
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				stringBuffer.append(line);
			}
			rd.close();
        }
        catch(Exception e) {
        	e.printStackTrace();
        	log.error("Error occurred while sending SMS to : " + sms.getMobileNumber(), e);
        }
    }
}
