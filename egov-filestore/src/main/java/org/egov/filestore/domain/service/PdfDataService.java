package org.egov.filestore.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.persistence.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Service
@Slf4j
public class PdfDataService {
	
	
	private ServiceRequestRepository serviceRequestRepository;
    private ObjectMapper mapper;	
    private FileStoreConfig config;
    
    ObjectMapper objMapper = new ObjectMapper();
    
    @Autowired
	public PdfDataService(ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper, FileStoreConfig config) {
		super();
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
		this.config = config;
	}



	public void fetchChallanDetails(RequestInfo request,String tenantId,String challanNo) {
		
		 StringBuilder uri = new StringBuilder();
		 uri.append(config.getEChallanHost());
		 uri.append(config.getEChallanEndpoint());
		 uri.append("?tenantId="+tenantId+"&challanNo="+challanNo);
		 Object result = serviceRequestRepository.fetchResult(uri,request);
		 //String stringResult = result.toString();
		try {
			String jsonString = objMapper.writeValueAsString(result);
			  System.out.println("string="+jsonString);
			 // JSONParser parser = new JSONParser();
			  JSONObject myJSONObject = new JSONObject(jsonString);
			  System.out.println("myJSONObject="+myJSONObject);
			  
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		 
		
		 
		// System.out.println("Result got ..."+result.toString());
	}
	
	public void fetchBillDetails(RequestInfo request) {
		
	}

}
