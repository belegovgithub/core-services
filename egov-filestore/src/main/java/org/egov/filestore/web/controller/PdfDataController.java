package org.egov.filestore.web.controller;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.service.PdfDataService;
import org.egov.filestore.web.contract.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/v1/pdfData")
public class PdfDataController {
	
	//PDF services for mCollect
	@Autowired
	PdfDataService pdfService;
	
	@RequestMapping(value = "/mCollectChallan", method = RequestMethod.POST)
	public String preparemCollectChallan(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			 @RequestParam("tenantId") String tenantId,@RequestParam("challanNo") String challanNo) {
		
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		pdfService.fetchChallanDetails(requestInfo,tenantId,challanNo);
			 return "success";
		}
		

}
