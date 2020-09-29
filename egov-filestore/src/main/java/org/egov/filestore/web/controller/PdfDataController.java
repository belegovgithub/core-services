package org.egov.filestore.web.controller;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.service.PdfDataService;
import org.egov.filestore.web.contract.RequestInfoWrapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/v1/pdfData")
public class PdfDataController {
	
	//PDF services for mCollect
	@Autowired
	PdfDataService pdfService;
	
	ObjectMapper objMapper = new ObjectMapper();
	
	@RequestMapping(value = "/_create", method = RequestMethod.POST)
	public ResponseEntity<Object> getPDFData_mCollectChallan(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			 @RequestParam("tenantId") String tenantId,@RequestParam("challanNo") String challanNo,@RequestParam("key")String key) throws JsonProcessingException {
		
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		String result =  null;
		switch (key) {
		case "mcollect-challan":
			result = pdfService.fetchChallanDetails(requestInfo,tenantId,challanNo,key);
			break;

		default:
			break;
		}
		
		return new ResponseEntity<Object>(result, HttpStatus.OK);
		

			 
		}
		

}
