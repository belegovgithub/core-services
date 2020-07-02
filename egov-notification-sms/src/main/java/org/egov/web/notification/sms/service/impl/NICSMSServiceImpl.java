
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
  
  @ConditionalOnProperty(value = "sms.gateway.to.use", havingValue ="NIC_SMS",
  matchIfMissing = true) public class NICSMSServiceImpl implements SMSService {
  

  
  @Override public void sendSMS(Sms sms) {
  
  
  
  try { String username = "username=" + "echdgde.sms"; 
  String pin= "&pin=" + "C3%26hM0%24kS1";
  String message = "&message=" +sms.getMessage();
  String mnumber = "&mnumber=91" + sms.getMobileNumber();
  String signature = "&signature=" + "Chawni";
  
  HttpURLConnection conn = (HttpURLConnection) new
  URL("https://smsgw.sms.gov.in/failsafe/HttpLink?").openConnection();
  
  String data = username + pin + message + mnumber + signature;
  System.out.println("data " + data);
  
  conn.setDoOutput(true); conn.setRequestMethod("POST");
  conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
  conn.getOutputStream().write(data.getBytes("UTF-8")); final BufferedReader rd
  = new BufferedReader(new InputStreamReader(conn.getInputStream())); final
  StringBuffer stringBuffer = new StringBuffer(); String line; while ((line =
  rd.readLine()) != null) { stringBuffer.append(line); } rd.close();
  
  } catch (Exception e) { e.printStackTrace();
  System.out.println("Error SMS ");
  
  }
  
  }
  
  }
  
  
  
 