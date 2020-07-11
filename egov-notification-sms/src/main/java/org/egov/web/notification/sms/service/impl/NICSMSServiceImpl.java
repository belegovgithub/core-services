/*
 * package org.egov.web.notification.sms.service.impl;
 * 
 * import java.io.BufferedReader; import java.io.FileInputStream; import
 * java.io.InputStream; import java.io.InputStreamReader; import java.net.URL;
 * import java.security.KeyStore;
 * 
 * import javax.net.ssl.HttpsURLConnection; import javax.net.ssl.SSLContext;
 * import javax.net.ssl.TrustManager; import javax.net.ssl.TrustManagerFactory;
 * 
 * import org.egov.web.notification.sms.models.Sms; import
 * org.egov.web.notification.sms.service.SMSService; import
 * org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
 * import org.springframework.stereotype.Service;
 * 
 * @Service
 * 
 * @ConditionalOnProperty(value = "sms.gateway.to.use", havingValue = "DEFAULT",
 * matchIfMissing = true) public class NICSMSServiceImpl implements SMSService {
 * 
 * @Override public void sendSMS(Sms sms) { try { String username = "username="
 * + "echdgde.sms"; String pin = "&pin=" + "C3%26hM0%24kS1"; String message =
 * "&message=" + sms.getMessage(); String mnumber = "&mnumber=91" +
 * sms.getMobileNumber(); String signature = "&signature=" + "Chawni";
 * 
 * String data = username + pin + message + mnumber + signature;
 * System.out.println("data " + data);
 * 
 * InputStream trustStream = new
 * FileInputStream("src/main/resources/nickeystore.jks"); char[] trustPassword =
 * "@bstc123".toCharArray();
 * 
 * KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
 * trustStore.load(trustStream, trustPassword);
 * 
 * TrustManagerFactory trustFactory = TrustManagerFactory
 * .getInstance(TrustManagerFactory.getDefaultAlgorithm());
 * trustFactory.init(trustStore); TrustManager[] trustManagers =
 * trustFactory.getTrustManagers();
 * 
 * SSLContext sslContext = SSLContext.getInstance("SSL"); sslContext.init(null,
 * trustManagers, null); SSLContext.setDefault(sslContext); HttpsURLConnection
 * conn = (HttpsURLConnection) new
 * URL("https://smsgw.sms.gov.in/failsafe/HttpLink?") .openConnection();
 * conn.setSSLSocketFactory(sslContext.getSocketFactory());
 * 
 * conn.setDoOutput(true); conn.setRequestMethod("POST");
 * conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
 * conn.getOutputStream().write(data.getBytes("UTF-8")); final BufferedReader rd
 * = new BufferedReader(new InputStreamReader(conn.getInputStream())); final
 * StringBuffer stringBuffer = new StringBuffer(); String line; while ((line =
 * rd.readLine()) != null) { stringBuffer.append(line); } rd.close();
 * 
 * } catch (Exception e) { e.printStackTrace();
 * System.out.println("Error SMS ");
 * 
 * }
 * 
 * }
 * 
 * }
 */