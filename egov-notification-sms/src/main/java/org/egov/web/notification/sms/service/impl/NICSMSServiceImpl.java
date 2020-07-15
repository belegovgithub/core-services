/*
 * package org.egov.web.notification.sms.service.impl;
 * 
 * import java.io.BufferedReader; import java.io.InputStreamReader; import
 * java.net.URL; import java.security.SecureRandom; import
 * java.security.cert.X509Certificate;
 * 
 * import javax.net.ssl.HostnameVerifier; import
 * javax.net.ssl.HttpsURLConnection; import javax.net.ssl.SSLContext; import
 * javax.net.ssl.SSLSession; import javax.net.ssl.TrustManager; import
 * javax.net.ssl.X509TrustManager;
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
 * 
 * TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
 * public X509Certificate[] getAcceptedIssuers() { return new
 * X509Certificate[0]; }
 * 
 * public void checkClientTrusted(X509Certificate[] certs, String authType) { }
 * 
 * public void checkServerTrusted(X509Certificate[] certs, String authType) { }
 * } };
 * 
 * HostnameVerifier hv = new HostnameVerifier() { public boolean verify(String
 * hostname, SSLSession session) { return true; } }; SSLContext sc =
 * SSLContext.getInstance("SSL"); sc.init(null, trustAllCerts, new
 * SecureRandom());
 * 
 * HttpsURLConnection conn = (HttpsURLConnection) new
 * URL("https://smsgw.sms.gov.in/failsafe/HttpLink?") .openConnection();
 * conn.setSSLSocketFactory(sc.getSocketFactory());
 * conn.setHostnameVerifier(hv); conn.setDoOutput(true);
 * conn.setRequestMethod("POST"); conn.setRequestProperty("Content-Length",
 * Integer.toString(data.length()));
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