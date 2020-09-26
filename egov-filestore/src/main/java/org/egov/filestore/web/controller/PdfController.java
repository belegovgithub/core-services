package org.egov.filestore.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/v1/pdfData")
public class PdfController {
	
	//PDF services for mCollect
	
		 @RequestMapping(value = "/mCollectChallan", method = RequestMethod.POST)
		public String preparemCollectChallan() {
			 System.out.println("came to filestore service");
			 //Challn detail
			 //Bill details
			 
			 return "success";
		 }
		

}
