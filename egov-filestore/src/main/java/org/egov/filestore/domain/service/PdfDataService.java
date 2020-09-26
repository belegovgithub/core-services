package org.egov.filestore.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.persistence.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfDataService {
	
	
	private ServiceRequestRepository serviceRequestRepository;
    private ObjectMapper mapper;	
    private FileStoreConfig config;
    
    
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
		// Object result = serviceRequestRepository.fetchResult(uri,request);
		System.out.println("uri"+uri);
	}

}
