package org.egov.pg.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pg.config.AppProperties;
import org.egov.pg.repository.ServiceCallRepository;
import org.egov.pg.web.models.Demand;
import org.egov.pg.web.models.DemandResponse;
import org.egov.pg.web.models.RequestInfoWrapper; 
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class DemandService {

	@Autowired
	private ServiceCallRepository repository;
	
	@Autowired
	private AppProperties props;
	
    /**
     * Creates demand Search url based on tenanatId,businessService and ConsumerCode
     * @return demand search url
     */
    public String getDemandSearchURL(){
        StringBuilder url = new StringBuilder(props.getDemandHost());
        url.append(props.getDemandSearchEndpoint());
        url.append("?");
        url.append("tenantId=");
        url.append("{1}");
        url.append("&");
        url.append("businessService=");
        url.append("{2}");
        url.append("&");
        url.append("consumerCode=");
        url.append("{3}");
        return url.toString();
    }

    @Autowired
    private ServiceCallRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper mapper;

       
    /**
     * Searches demand for the given consumerCode and tenantIDd
     * @param tenantId The tenantId of the tradeLicense
     * @param consumerCodes The set of consumerCode of the demands
     * @param requestInfo The RequestInfo of the incoming request
     * @return Lis to demands for the given consumerCode
     */
    public List<Demand> searchDemand(String tenantId,String consumerCodes,RequestInfo requestInfo, String businessService){
        String uri = getDemandSearchURL();
        uri = uri.replace("{1}",tenantId);
        uri = uri.replace("{2}",businessService);
        uri = uri.replace("{3}", consumerCodes ); 
        Optional<Object> result  = serviceRequestRepository.fetchResult( uri ,new RequestInfoWrapper(requestInfo));
        DemandResponse response=null;
        if(result.isPresent()) {
        	try {
                response = mapper.convertValue(result.get(),DemandResponse.class);
           }
           catch (IllegalArgumentException e){
               throw new CustomException("PARSING ERROR","Failed to parse response from Demand Search");
           }
        }
        
        

        if(CollectionUtils.isEmpty(response.getDemands()))
            return null;

        else return response.getDemands();

    }
 













}
