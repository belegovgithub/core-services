package org.egov.pg.validator;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.pg.config.PgDetailConfigs;
import org.egov.pg.web.contract.PgDetail;
import org.egov.pg.web.contract.PgDetailRequest;
//import org.egov.tl.config.TLConfiguration;
//import org.egov.tl.repository.TLRepository;
//import org.egov.tl.service.TradeLicenseService;
//import org.egov.tl.service.UserService;
//import org.egov.tl.util.BPAConstants;
//import org.egov.tl.util.TLConstants;
//import org.egov.tl.util.TradeUtil;
//import org.egov.tl.web.models.*;
//import org.egov.tl.web.models.user.UserDetailResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

 

@Component
public class PgValidator {

 
	@Autowired
    private PgDetailConfigs config;
	public PgValidator(PgDetailConfigs config ) {
		this.config =config;
	}
 
    
    public void validateCreate(PgDetailRequest pgDetailRequest) {
    	
    	try {
    		List<String> allowedRoles = Arrays.asList(config.getAllowedCreateUserRoles().split(","));
    		User user = pgDetailRequest.getRequestInfo().getUserInfo();
    		boolean authUser =user.getRoles().stream().anyMatch(r -> allowedRoles.contains(r.getCode())) ;
    		boolean authTenant =pgDetailRequest.getPgDetail().get(0).getTenantId().contains(user.getTenantId());
    		if( authUser && authTenant) {
    			return ;
    		}
    	}catch (Exception e) {
    		throw new CustomException("NOT_AUTHORIZED", "The User is not authorized to access the resource");
		}
    	throw new CustomException("NOT_AUTHORIZED", "The User is not authorized to access the resource");
    }
    
    public void validateForSearch(PgDetailRequest pgDetailRequest) {
    	
    	try {
    		List<String> allowedRoles = Arrays.asList(config.getAllowedAccessUserRoles().split(","));
    		User user = pgDetailRequest.getRequestInfo().getUserInfo();
    		//To Cater system role
    		if(allowedRoles.stream().anyMatch(r -> r.equals(user.getType()))) {
    			return;
    		}
    		boolean authUser =user.getRoles().stream().anyMatch(r -> allowedRoles.contains(r.getCode())) ;
    		boolean authTenant =pgDetailRequest.getPgDetail().get(0).getTenantId().contains(user.getTenantId());
    		if( authUser && authTenant) {
    			return ;
    		}
    	}catch (Exception e) {
    		throw new CustomException("NOT_AUTHORIZED", "The User is not authorized to access the resource");
		}
    	throw new CustomException("NOT_AUTHORIZED", "The User is not authorized to access the resource");
    }
    
    
 
}


