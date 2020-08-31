package org.egov.user.domain.model;

import lombok.*;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.exception.InvalidUserSearchCriteriaException;
import org.egov.user.domain.model.enums.UserType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.egov.common.contract.request.Role;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class UserSearchCriteria {

    private List<Long> id;
    private List<String> uuid;
    private String userName;
    private String name;
    private String mobileNumber;
    private String emailId;
    private boolean fuzzyLogic;
    private Boolean active;
    private Integer offset;
    private Integer limit;
    private List<String> sort;
    private UserType type;
    private String tenantId;
    private List<String> roleCodes;
    private boolean isSuperUser;

    public void validate(boolean isInterServiceCall) {
        if (validateIfEmptySearch(isInterServiceCall) || validateIfTenantIdExists(isInterServiceCall)) {
            throw new InvalidUserSearchCriteriaException(this);
        }
    }

    private boolean validateIfEmptySearch(boolean isInterServiceCall) {
        /*
            for "InterServiceCall" ->
                at least one is compulsory --> 'userName' or 'name' or 'mobileNumber' or 'emailId' or 'uuid' or 'id' or 'roleCodes'

            and for calls from outside->
                at least one is compulsory --> 'userName' or 'name' or 'mobileNumber' or 'emailId' or 'uuid'
         */
        if (isInterServiceCall)
            return isEmpty(userName) && isEmpty(name) && isEmpty(mobileNumber) && isEmpty(emailId) &&
                    CollectionUtils.isEmpty(uuid) && CollectionUtils.isEmpty(id) && CollectionUtils.isEmpty(roleCodes);
        else
            return isEmpty(userName) && isEmpty(name) && isEmpty(mobileNumber) && isEmpty(emailId) &&
                    CollectionUtils.isEmpty(uuid);
    }

    private boolean validateIfTenantIdExists(boolean isInterServiceCall) {
        /*
            for calls from outside->
                tenantId is compulsory if one of these is non empty--> 'userName' or 'name', 'mobileNumber'  or 'roleCodes'
            and for "InterServiceCall" ->
                tenantId is compulsory if one of these is non empty --> 'userName' or 'name' or 'mobileNumber'
         */
        if (isInterServiceCall)
            return (!isEmpty(userName) || !isEmpty(name) || !isEmpty(mobileNumber) ||
                    !CollectionUtils.isEmpty(roleCodes))
                    && isEmpty(tenantId);
        else
            return (!isEmpty(userName) || !isEmpty(name) || !isEmpty(mobileNumber))
                    && isEmpty(tenantId);

    }
    
    public void vaidateSearch(boolean isInterServiceCall, RequestInfo requestInfo) {
    	if(isInterServiceCall && !StringUtils.isEmpty(requestInfo) &&  !StringUtils.isEmpty(requestInfo.getUserInfo())) {
     	   String userType = requestInfo.getUserInfo().getType();
     	   if(userType.equalsIgnoreCase(UserType.CITIZEN.toString()))
     	   {
     		   if(!isEmpty(tenantId) && !requestInfo.getUserInfo().getTenantId().equalsIgnoreCase(tenantId)) {
     			   throw new CustomException("Invalid","Not authorised to search!!");
     		   }
     	   }
     	   else if(userType.equalsIgnoreCase(UserType.EMPLOYEE.toString())) {
     		   if(!isEmpty(tenantId) && tenantId.contains(".") && !requestInfo.getUserInfo().getTenantId().equalsIgnoreCase(tenantId)) {
     			   throw new CustomException("Invalid","Not authorised to search!!");
     		   }
     		   List<Role> roles = requestInfo.getUserInfo().getRoles();
     		   for (Role role : roles) {
     			  if( role.getCode().equalsIgnoreCase("SUPERUSER")) {
     				  isSuperUser = true;
     				  break;
     			  }
     		   }
     		   
     	   }
        }
        if(StringUtils.isEmpty(requestInfo) || StringUtils.isEmpty(requestInfo.getUserInfo())){
     	   isSuperUser = true;
        }
     }
    
}
