package org.egov.persistence.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.domain.exception.OtpNumberNotPresentException;
import org.egov.domain.exception.TokenGenerationAttemptsOverException;
import org.egov.domain.model.OtpRequest;
import org.egov.persistence.contract.OtpResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.egov.common.contract.response.Error;

@Service
@Slf4j
public class OtpRepository {

    private final String otpCreateUrl;
    private RestTemplate restTemplate;

    public OtpRepository(RestTemplate restTemplate,
                         @Value("${otp.host}") String otpHost,
                         @Value("${otp.create.url}") String otpCreateUrl) {
        this.restTemplate = restTemplate;
        this.otpCreateUrl = otpHost + otpCreateUrl;
    }

    public String fetchOtp(OtpRequest otpRequest) {
    	try {
    		final org.egov.persistence.contract.OtpRequest request =
                    new org.egov.persistence.contract.OtpRequest(otpRequest);
            final OtpResponse otpResponse =
                    restTemplate.postForObject(otpCreateUrl, request, OtpResponse.class);

            if(isOtpNumberAbsent(otpResponse)) {
                throw new OtpNumberNotPresentException();
            }
            return otpResponse.getOtpNumber();
    	}
    	
    	catch (HttpClientErrorException e) {    		   		
    		if(HttpStatus.CONFLICT.equals(e.getStatusCode()))
    		   throw new TokenGenerationAttemptsOverException();
    		else
    			 throw new OtpNumberNotPresentException();
		}
    	catch(HttpStatusCodeException  e) {
			throw new CustomException(e.getStatusCode().toString(),e.getStatusText());
		} catch (Exception e) {
            throw new OtpNumberNotPresentException();
        }
    }

    private boolean isOtpNumberAbsent(OtpResponse otpResponse) {
        return otpResponse == null || otpResponse.isOtpNumberAbsent();
    }
}
