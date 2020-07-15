/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

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

import org.egov.web.notification.sms.config.SmsProperties1;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.SMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@ConditionalOnProperty(value = "sms.gateway.to.use", havingValue = "DEFAULT", matchIfMissing = true)
public class ExternalSMSService implements SMSService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSMSService.class);

    private SmsProperties1 smsProperties;

    @Autowired
    public ExternalSMSService(SmsProperties1 smsProperties, RestTemplate restTemplate) {

        this.smsProperties = smsProperties;
    }

    @Override
    public void sendSMS(Sms sms) {
        if (!sms.isValid()) {
            LOGGER.error(String.format("Sms %s is not valid", sms));
            return;
        }
        submitToExternalSmsService(sms);
    }

    private void submitToExternalSmsService(Sms sms) {
        try {
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
			String url = smsProperties.getSmsProviderURL();
			String data = smsProperties.queryParams(sms);
			System.out.println("ssl check done. URL about to hit");
			HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setSSLSocketFactory(sslContext.getSocketFactory());
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
        }
        catch(Exception e) {
        	e.printStackTrace();
        	LOGGER.error("Error occurred while sending SMS to : " + sms.getMobileNumber(), e);
        }
    }

	/*
	 * private boolean isResponseCodeInKnownErrorCodeList(ResponseEntity<?>
	 * response) { final String responseCode =
	 * Integer.toString(response.getStatusCodeValue()); return
	 * smsProperties.getSmsErrorCodes().stream().anyMatch(errorCode ->
	 * errorCode.equals(responseCode)); }
	 */

	/*
	 * private HttpEntity<MultiValueMap<String, String>> getRequest(Sms sms) { final
	 * MultiValueMap<String, String> requestBody =
	 * smsProperties.getSmsRequestBody(sms); return new HttpEntity<>(requestBody,
	 * getHttpHeaders()); }
	 */

	/*
	 * private HttpHeaders getHttpHeaders() { HttpHeaders headers = new
	 * HttpHeaders(); headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	 * return headers; }
	 */

}
