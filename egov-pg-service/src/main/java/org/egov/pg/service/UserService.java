package org.egov.pg.service;

import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.config.AppProperties;
import org.egov.pg.repository.ServiceCallRepository;
import org.egov.pg.web.models.UserDetailResponse;
import org.egov.pg.web.models.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class UserService {


 

    private ServiceCallRepository serviceCallRepository;

    private ObjectMapper mapper;

    private AppProperties appProperties;
    @Autowired
    public UserService( AppProperties appProperties, ServiceCallRepository serviceCallRepository, ObjectMapper mapper) {
        this.appProperties = appProperties;
        this.serviceCallRepository = serviceCallRepository;
        this.mapper = mapper;
    }


    /**
     * Calls search api of user to fetch the user for the given username
     * @param username : user name 
     * @return OwnerInfo of the user with the given uuid
     */
    public User  searchSystemUser(RequestInfo requestInfo,String userName ){
        org.egov.pg.web.models.UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setUserName(userName);
        userSearchRequest.setTenantId(appProperties.getTenantId());
        StringBuilder url = new StringBuilder(appProperties.getUserHost());
        url.append(appProperties.getUserSearchEndpoint());
        Optional<Object> response =  serviceCallRepository.fetchResult(url.toString(), userSearchRequest);
		if(response.isPresent()) {
			try {
				UserDetailResponse paymentResponse = mapper.convertValue(response.get(), UserDetailResponse.class);
				if(!CollectionUtils.isEmpty(paymentResponse.getUser()))
					return paymentResponse.getUser().get(0);
				else
					throw new CustomException("INVALID USER", "No user found for the user name "+userName);						
			}catch(Exception e) {
				log.error("Failed to parse the payment response: ",e);
				throw new CustomException("RESPONSE_PARSE_ERROR", "Failed to parse the user response");
			}

		}else {
			throw new CustomException("USER_SEARCH_FAILED", "Failed to search user");
		}
    }
}
