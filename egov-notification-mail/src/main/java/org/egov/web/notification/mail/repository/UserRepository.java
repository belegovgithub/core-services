package org.egov.web.notification.mail.repository;

import java.util.ArrayList;
import java.util.List;
import org.egov.tracer.model.CustomException;
import org.egov.web.notification.mail.config.ApplicationConfiguration;
import org.egov.web.notification.mail.consumer.contract.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

@Service
public class UserRepository {

	private ServiceRequestRepository serviceRequestRepository;

	private ApplicationConfiguration config;

	private ObjectMapper objectMapper;

	@Autowired
	public UserRepository(ServiceRequestRepository serviceRequestRepository, ApplicationConfiguration config,
			ObjectMapper objectMapper) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.config = config;
		this.objectMapper = objectMapper;
	}

	public List<String> getUserDetails(String tenantId, String mobileNo,String uuid) {
		List<String> emails = null;
		try {
			String rcvData = objectMapper.writeValueAsString(fetchUser(tenantId, mobileNo,uuid));
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(rcvData);
			emails = JsonPath.read(document, "$.user[?(@.emailId != null)].emailId");
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		} catch (JsonProcessingException e) {
			throw new CustomException("EMAIL_NOTIFICATION_USER_SEARCH_FAILED",
					"Deserialization failed, for user response");
		}
		return emails;
	}
	

	private Object fetchUser(String tenantId, String mobileNo,String uuid) {
		String url = config.getUserHost().concat(config.getUserContextPath()).concat(config.getUserSearchEndpoint());
		UserSearchRequest searchRequest = UserSearchRequest.builder().mobileNumber(mobileNo).tenantId(tenantId).build();
		if(uuid!=null) {
			ArrayList<String> uuids =new ArrayList<String>();
			uuids.add(uuid);
			searchRequest.setUuid(uuids);
		}
		return serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
	}
	
	

}
