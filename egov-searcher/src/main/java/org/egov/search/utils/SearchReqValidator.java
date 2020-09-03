package org.egov.search.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.SearchApplicationRunnerImpl;
import org.egov.search.model.Definition;
import org.egov.search.model.Params;
import org.egov.search.model.SearchDefinition;
import org.egov.search.model.SearchParams;
import org.egov.search.model.SearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchReqValidator {
		
	@Autowired
	private SearchApplicationRunnerImpl runner;
	
	@Autowired
	private SearchUtils searchUtils;
	
	@Autowired
	private ObjectMapper mapper;
	@Value("${citizen.restrict.search.result}")
	private String citizenRestrictSearchResult;
	@Value("${citizen.restrict.search.result.keyname}")
	private String citizenRestrictSearchResultKeyName;
	
	public void validate(SearchRequest searchRequest, String moduleName, String searchName) {
		log.info("Validating search request....");
		Map<String, SearchDefinition> searchDefinitionMap = runner.getSearchDefinitionMap();
		Definition searchDefinition = searchUtils.getSearchDefinition(searchDefinitionMap, moduleName, searchName);
		//Add restriction on mobile number for citizen search. 
		try {
		if(!StringUtils.isEmpty(citizenRestrictSearchResult)){
			if(searchRequest.getRequestInfo()!=null && searchRequest.getRequestInfo().getUserInfo().getType().equals("CITIZEN")) {
				List<String> restrictedReportUrl = Arrays.asList(citizenRestrictSearchResult.split(","));
				LinkedHashMap<String,String> obj=(LinkedHashMap<String, String>) searchRequest.getSearchCriteria();
				if(obj.values().stream().anyMatch(val -> restrictedReportUrl.contains(val))) {
					obj.put(citizenRestrictSearchResultKeyName, searchRequest.getRequestInfo().getUserInfo().getUserName());
				}
			}
		}
		}catch (Exception e) {
			log.error("Error in adding restriction  "+e.getMessage());
		}
		
		
		validateSearchDefAgainstReq(searchDefinition, searchRequest);
	}
	
	public void validateSearchDefAgainstReq(Definition searchDefinition, SearchRequest searchRequest) {
		SearchParams searchParams = searchDefinition.getSearchParams();
		Map<String, String> errorMap = new HashMap<>();
		if(null == searchParams) {
			errorMap.put("MISSING_PARAM_CONFIGS", "Missiing Parameter Configurations for: "+searchDefinition.getName());
			throw new CustomException(errorMap);
		}
		if(!CollectionUtils.isEmpty(searchParams.getParams())) {
			List<Params> params = searchParams.getParams().stream().filter(param -> param.getIsMandatory()).collect(Collectors.toList());
			try {
				String request = mapper.writeValueAsString(searchRequest);
				params.forEach(entry -> {
					Object paramValue = null;
					try {
						paramValue = JsonPath.read(request, entry.getJsonPath());
					}catch(Exception e) {
						errorMap.put("MISSING_MANDATORY_EXCEPTION", "Missiing Mandatory Property: "+entry.getJsonPath());
					}
					if(null == paramValue) {
						errorMap.put("MISSING_MANDATORY_VALUE", "Missiing Mandatory Property: "+entry.getJsonPath());
					}
				});
			}catch(Exception e) {
				log.error("An exception has occured while validating: ",e);
				errorMap.put("VALIDATION_EXCEPTION", "An exception has occured while validating");
			}
		}		
		if(!errorMap.isEmpty())
			throw new CustomException(errorMap);
		
		
	}
}
