package org.egov.filestore.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.persistence.repository.ServiceRequestRepository;
import org.egov.pdfData.model.PDFCreationRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONArray;
import org.json.JSONException;
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



	public String fetchChallanDetails(RequestInfo request,String tenantId,String challanNo,String key) {
		
		 StringBuilder uri = new StringBuilder();
		 uri.append(config.getEChallanHost());
		 uri.append(config.getEChallanEndpoint());
		 uri.append("?tenantId="+tenantId+"&challanNo="+challanNo);
		 Object result = serviceRequestRepository.fetchResult(uri,request);
		try {
			  String jsonString_challan = objMapper.writeValueAsString(result);			  
			  JSONObject responseJsonObj_challan = new JSONObject(jsonString_challan);
			  JSONArray jsonArray_challan = (JSONArray) responseJsonObj_challan.getJSONArray("challans");
			  JSONObject Challan = (JSONObject) jsonArray_challan.get(0);
			  JSONObject ChallanCitizen = (JSONObject) Challan.get("citizen");			 
			  Map<String,String>fetchBillInput = new HashMap<String, String>();			  		 
			  fetchBillInput.put("tenantId", Challan.get("tenantId").toString());
			  fetchBillInput.put("consumerCode",Challan.get("challanNo").toString());
			  fetchBillInput.put("businessService",Challan.get("businessService").toString());
			  Object billDetails = fetchBillDetails(request,fetchBillInput);
			  String jsonString_bill = objMapper.writeValueAsString(billDetails);
			  JSONObject responseJsonObj_bill = new JSONObject(jsonString_bill);
			  JSONArray jsonArray_bill = (JSONArray) responseJsonObj_bill.getJSONArray("Bill");
			  JSONObject Bill = (JSONObject) jsonArray_bill.get(0);		 
			  JSONArray jsonArray_billDetails = (JSONArray) Bill.getJSONArray("billDetails");
			  JSONObject billDetailsObj = (JSONObject) jsonArray_billDetails.get(0);
			  JSONArray jsonArray_billAccountDetails = (JSONArray) billDetailsObj.getJSONArray("billAccountDetails");
			  List<JSONObject> list = new ArrayList<JSONObject>();
		      for(int i = 0; i < jsonArray_billAccountDetails.length(); i++) {
		         list.add(jsonArray_billAccountDetails.getJSONObject(i));
		      }
		      String key_name = "amount";
		      List<JSONObject> sortedArray = sortBillDetails(list,key_name);
			  Challan.put("totalAmount", Bill.get("totalAmount"));
			  Challan.put("billNo",Bill.get("billNumber"));
			  Challan.put("billDate",Bill.get("billDate"));
			  Challan.put("amount", sortedArray);
			  Challan.put("mobileNumber",ChallanCitizen.get("mobileNumber"));
			  Challan.put("serviceType",Challan.get("businessService"));
					 
			  String pdfResult = generatePDF(request,Challan,key);
			  return pdfResult;
			  
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "error";
		}		 
				 
	}
	public String generatePDF(RequestInfo request,JSONObject pdfData,String key) {
		try {
		log.info("Came to generate pdf"+pdfData.get("tenantId"));
        if(pdfData==null)
            throw new CustomException("INVALID REQUEST","The request for calculation cannot be empty or null");
        StringBuilder uri = new StringBuilder();
        uri.append(config.getPdfServiceHost());
        uri.append(config.getPdfServiceEndPoint());
        //Get tenant id as only "pb"
        String tenantId = pdfData.get("tenantId").toString().split("\\.")[0];
        uri.append("?key="+key+"&tenantId="+tenantId);
        PDFCreationRequest pdfCreationRequest = PDFCreationRequest.builder().pdfData(pdfData).requestInfo(request).build();
        Object result = serviceRequestRepository.fetchResult(uri,pdfCreationRequest);
        String pdfString_challan = objMapper.writeValueAsString(result);			  
        log.info("result="+pdfString_challan);
        return pdfString_challan;
		}
		catch (Exception e) {
			log.info("Some error in creating pdf obj");
			return "error";
		}
        
	}
	
	public Object fetchBillDetails(RequestInfo request,Map<String,String>fetchBillInput) {
		 StringBuilder uri = new StringBuilder();
		 uri.append(config.getBillingHost());
		 uri.append(config.getFetchBillEndpoint());
		 uri.append("?tenantId="+fetchBillInput.get("tenantId")+"&consumerCode="+fetchBillInput.get("consumerCode")+"&businessService="+fetchBillInput.get("businessService"));
		 Object result = serviceRequestRepository.fetchResult(uri,request);	
		 return result;
	}
	/**
	 * Sort Bill details in descending order
	 */
	public List<JSONObject> sortBillDetails(List<JSONObject> list,String key_name) {
	      Collections.sort(list, new Comparator<JSONObject>() {
	         // private static final String KEY_NAME = key_name;
	      	@Override
	          public int compare(JSONObject a, JSONObject b) {
	             Double str1 = 0.0;
	             Double str2 = 0.0;
	             try {
	                str1 = (Double) a.get(key_name);
	                str2 = (Double) b.get(key_name);
	             } catch(JSONException e) {
	                e.printStackTrace();
	             }
	             return -str1.compareTo(str2);
	           }			
	       });
	      return list;
	}
	
	
}
