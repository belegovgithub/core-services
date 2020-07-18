package org.egov.pg.service.gateways.nic;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.egov.pg.models.PgDetail;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.service.Gateway;
import org.egov.pg.service.gateways.ccavenue.CCAvenueStatusResponse;
import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.omg.IOP.ServiceIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static org.egov.pg.constants.TransactionAdditionalFields.BANK_ACCOUNT_NUMBER;

/**
 * NIC Gateway implementation
 */
@Component
@Slf4j
public class NICGateway implements Gateway {

    private static final String GATEWAY_NAME = "NIC";
    private final String MESSAGE_TYPE;
 
 
 
 
    private final String CURRENCY_CODE;

    private final RestTemplate restTemplate;
 
    private final boolean ACTIVE;
    
    private final String REDIRECT_URL;
    private final String ORIGINAL_RETURN_URL_KEY;
 
    private final String MESSAGE_TYPE_KEY = "messageType";
    private final String MERCHANT_ID_KEY = "merchantId";
    
    private final String SERVICE_ID_KEY = "serviceId";
    private final String ORDER_ID_KEY = "orderId";
    private final String CUSTOMER_ID_KEY = "customerId";
    private final String TRANSACTION_AMOUNT_KEY = "transactionAmount";
    private final String CURRENCY_CODE_KEY = "currencyCode";
    private final String REQUEST_DATE_TIME_KEY = "requestDateTime";
    private final String SUCCESS_URL_KEY = "successUrl";
    private final String FAIL_URL_KEY = "failUrl";
    private final String ADDITIONAL_FIELD1_KEY = "additionalField1";
    private final String ADDITIONAL_FIELD2_KEY = "additionalField2";
    private final String ADDITIONAL_FIELD3_KEY = "additionalField3";
    private final String ADDITIONAL_FIELD4_KEY = "additionalField4";
    private final String ADDITIONAL_FIELD5_KEY = "additionalField5";
    private final String ADDITIONAL_FIELD_VALUE = "111111";
    private final String GATEWAY_TRANSACTION_STATUS_URL;
    private final String GATEWAY_TRANSACTION_STATUS_URL1;
    private final String GATEWAY_TRANSACTION_STATUS_URL2;
    private final String GATEWAY_URL;
    private static final String SEPERATOR ="|";
    private String TX_DATE_FORMAT;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    
    
    /**
     * Initialize by populating all required config parameters
     *
     * @param restTemplate rest template instance to be used to make REST calls
     * @param environment containing all required config parameters
     */
    @Autowired
    public NICGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        
        ACTIVE = Boolean.valueOf(environment.getRequiredProperty("nic.active"));
        MESSAGE_TYPE = environment.getRequiredProperty("nic.messageType");
        
        CURRENCY_CODE = environment.getRequiredProperty("nic.currency");
        
        
        REDIRECT_URL = environment.getRequiredProperty("nic.redirect.url");
        ORIGINAL_RETURN_URL_KEY = environment.getRequiredProperty("nic.original.return.url.key");
        GATEWAY_TRANSACTION_STATUS_URL = environment.getRequiredProperty("nic.gateway.status.url");
        GATEWAY_TRANSACTION_STATUS_URL1 = environment.getRequiredProperty("nic.gateway.status.url1");
        GATEWAY_TRANSACTION_STATUS_URL2 = environment.getRequiredProperty("nic.gateway.status.url2");
        
        GATEWAY_URL = environment.getRequiredProperty("nic.gateway.url");
        TX_DATE_FORMAT =environment.getRequiredProperty("nic.dateformat");
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
    	return null;
    }
    
    @Override
    public String generateRedirectURI(Transaction transaction, PgDetail pgDetail) {
    	
    	/*
		 * 
		 messageType|merchantId|serviceId|orderId|customerId|transactionAmount|currencyCode|r
		equestDateTime|successUrl|failUrl|additionalField1| additionalField2| additionalField3|
		additionalField4| additionalField5
		 */
    	String urlData =null;
    	HashMap<String, String> queryMap = new HashMap<>();
         queryMap.put(MESSAGE_TYPE_KEY, MESSAGE_TYPE);
         queryMap.put(MERCHANT_ID_KEY, pgDetail.getMerchantId());
         queryMap.put(SERVICE_ID_KEY, pgDetail.getMerchantServiceId());
         queryMap.put(ORDER_ID_KEY, transaction.getTxnId());
         queryMap.put(CUSTOMER_ID_KEY, transaction.getUser().getUuid());
         queryMap.put(TRANSACTION_AMOUNT_KEY, String.valueOf(Utils.formatAmtAsPaise(transaction.getTxnAmount())));
         queryMap.put(CURRENCY_CODE_KEY,CURRENCY_CODE);
         SimpleDateFormat format = new SimpleDateFormat(TX_DATE_FORMAT);
     	 queryMap.put(REQUEST_DATE_TIME_KEY, format.format(new Date()));
         queryMap.put(SUCCESS_URL_KEY, getReturnUrl(transaction.getCallbackUrl(), REDIRECT_URL));
         queryMap.put(FAIL_URL_KEY, getReturnUrl(transaction.getCallbackUrl(), REDIRECT_URL));
         queryMap.put(ADDITIONAL_FIELD1_KEY, ADDITIONAL_FIELD_VALUE); //Not in use 
         queryMap.put(ADDITIONAL_FIELD2_KEY, ADDITIONAL_FIELD_VALUE); //Not in use 
         queryMap.put(ADDITIONAL_FIELD3_KEY, ADDITIONAL_FIELD_VALUE); //Not in use 
         queryMap.put(ADDITIONAL_FIELD4_KEY, ADDITIONAL_FIELD_VALUE); //Not in use 
         queryMap.put(ADDITIONAL_FIELD5_KEY, ADDITIONAL_FIELD_VALUE); //Not in use 
         
         
         
         //Generate Checksum for params  
         ArrayList<String> fields = new ArrayList<String>();
     	fields.add(queryMap.get(MESSAGE_TYPE_KEY));
     	fields.add(queryMap.get(MERCHANT_ID_KEY));
     	fields.add(queryMap.get(SERVICE_ID_KEY));
     	fields.add(queryMap.get(ORDER_ID_KEY));
     	fields.add(queryMap.get(CUSTOMER_ID_KEY));
     	fields.add(queryMap.get(TRANSACTION_AMOUNT_KEY));
     	fields.add(queryMap.get(CURRENCY_CODE_KEY));
     	fields.add(queryMap.get(REQUEST_DATE_TIME_KEY));
     	fields.add(queryMap.get(SUCCESS_URL_KEY));
     	fields.add(queryMap.get(FAIL_URL_KEY));
     	fields.add(queryMap.get(ADDITIONAL_FIELD1_KEY));
     	fields.add(queryMap.get(ADDITIONAL_FIELD2_KEY));
     	fields.add(queryMap.get(ADDITIONAL_FIELD3_KEY));
     	fields.add(queryMap.get(ADDITIONAL_FIELD4_KEY));
     	fields.add(queryMap.get(ADDITIONAL_FIELD5_KEY));
     	
        String message = String.join("|", fields);
     	queryMap.put("checksum", NICUtils.generateCRC32Checksum(message, pgDetail.getMerchantSecretKey()));
     	queryMap.put("txURL",GATEWAY_URL);
     	ObjectMapper mapper = new ObjectMapper();
     	try {
     		urlData= mapper.writeValueAsString(queryMap);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			log.error("NIC URL generation failed", e);
            throw new CustomException("URL_GEN_FAILED",
                    "NIC URL generation failed, gateway redirect URI cannot be generated");
		}
    	return urlData;
    }
    
     

    private String getReturnUrl(String callbackUrl, String baseurl) {
        return UriComponentsBuilder.fromHttpUrl(baseurl).queryParam(ORIGINAL_RETURN_URL_KEY, callbackUrl).build()
                .encode().toUriString();
    }
    
    class RequestMsg{
    	private String requestMsg;
    	public RequestMsg() {
    		
    	}
    	public RequestMsg(String msg) {
    		this.requestMsg= msg;
    	}
		public String getRequestMsg() {
			return requestMsg;
		}
		public void setRequestMsg(String requestMsg) {
			this.requestMsg = requestMsg;
		}
		@Override
		public String toString() {
			return "RequestMsg [requestMsg=" + requestMsg + "]";
		}
		
    	
    }
    
    
    class QueryApiRequest{
    	List<RequestMsg> queryApiRequest= new ArrayList<RequestMsg>();

		public List<RequestMsg> getQueryApiRequest() {
			return queryApiRequest;
		}

		public void setQueryApiRequest(List<RequestMsg> queryApiRequest) {
			this.queryApiRequest = queryApiRequest;
		}

		@Override
		public String toString() {
			return "QueryApiRequest [queryApiRequest=" + queryApiRequest + "]";
		}
		
    	
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> param) {
    	Transaction transaction=null;
    	boolean flag =false;
         
        try {
        	log.debug("Approach 3: ");
        	SSLContext context = SSLContext.getInstance("TLSv1.2");
        	context.init(null, null, null);

        	CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context)
        	    .build();
        	HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        	RestTemplate template =restTemplateBuilder.requestFactory(factory).basicAuthorization(param.get("merchantUserName"), param.get("merchantPassword")).build();
        	String requestmsg =SEPERATOR+ param.get("merchantId") +SEPERATOR+currentStatus.getTxnId();
            
        	log.debug("Status URL : "+GATEWAY_TRANSACTION_STATUS_URL2);
        	QueryApiRequest queryApiRequest = new QueryApiRequest();
        	queryApiRequest.getQueryApiRequest().add(new RequestMsg(requestmsg));
        	log.debug("queryApiRequest " +queryApiRequest);
            ResponseEntity response = template.postForObject(GATEWAY_TRANSACTION_STATUS_URL2,queryApiRequest, ResponseEntity.class);
            log.debug("Status URL Response Entity 333"+response);
        } catch (RestClientException e) {
            log.error("Unable to fetch status from ccavenue gateway", e);
            flag =true;
            //throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from ccavenue gateway");
        } catch (Exception e) {
            log.error("ccavenue Checksum generation failed", e);
            flag =true;
            //throw new CustomException("CHECKSUM_GEN_FAILED","Checksum generation failed, gateway redirect URI cannot be generated");
        }
        if (flag) {
        	throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from ccavenue gateway");
        }
        return transaction;
    }

    @Override
    public boolean isActive() {
        return ACTIVE;
    }

    @Override
    public String gatewayName() {
        return GATEWAY_NAME;
    }

    @Override
    public String transactionIdKeyInResponse() {
        return "vpc_MerchTxnRef";
    }

     

    private Transaction transformRawResponse(String resp, Transaction currentStatus, String secretKey)
            throws JsonParseException, JsonMappingException, IOException {
    	log.info("NICGateway.transformRawResponse()"+resp);
        Transaction.TxnStatusEnum status = Transaction.TxnStatusEnum.PENDING;
        NICStatusResponse statusResponse;
        Map<String, String> respMap = new HashMap<String, String>();
        Arrays.asList(resp.split("&")).forEach(
                param -> respMap.put(param.split("=")[0], param.split("=").length > 1 ? param.split("=")[1] : ""));
        log.info("Split Message "+ respMap);
        if (respMap.get("msg")!=null) {
        	log.info(" respMap.get(\"msg\")+> "+respMap.get("msg"));
        	//Validate the response against the checksum
        	
        	NICUtils.validateTransaction(respMap.get("msg"), secretKey);
        	
        	statusResponse = decodeResponseMsg(respMap.get("msg"));
        	log.info(" statusResponse => "+statusResponse);
        	if (statusResponse.getTxFlag().equalsIgnoreCase("S"))
                    status = Transaction.TxnStatusEnum.SUCCESS;
                else if (statusResponse.getTxFlag().equalsIgnoreCase("F")|| statusResponse.getTxFlag().equalsIgnoreCase("D"))
                    status = Transaction.TxnStatusEnum.FAILURE;
        	
        	
        	return Transaction.builder().txnId(currentStatus.getTxnId())
                    .txnAmount(Utils.formatAmtAsRupee(statusResponse.getTransactionAmount()))
                    .txnStatus(status).gatewayTxnId(statusResponse.getSurePayTxnId())
                    .gatewayPaymentMode(statusResponse.getPaymentMode())
                    .gatewayStatusCode(statusResponse.getTxFlag())
                    .responseJson(respMap.get("msg")).build();
        } else {
            log.error("Received error response from status call : " + respMap.get("enc_response"));
            throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from nic gateway");
        }
    }
    
    private NICStatusResponse decodeResponseMsg(String respMsg) {
    	String[] splitArray = respMsg.split("[|]");
    	NICStatusResponse txResp = new NICStatusResponse(splitArray[0]);
    	switch (txResp.getTxFlag()) {
		case "S":
			/*For Success : 
			SuccessFlag|MessageType|SurePayMerchantId|ServiceId|OrderId|CustomerId|TransactionAmount|
			CurrencyCode|PaymentMode|ResponseDateTime|SurePayTxnId|
			BankTransactionNo|TransactionStatus|AdditionalInfo1|AdditionalInfo2|AdditionalInfo3|
			AdditionalInfo4|AdditionalInfo5|ErrorCode|ErrorDescription|CheckSum*/
			
			txResp.setMessageType(splitArray[1]);
			txResp.setSurePayMerchantId(splitArray[2]);
			txResp.setServiceId(splitArray[3]);
			txResp.setOrderId(splitArray[4]);
			txResp.setCustomerId(splitArray[5]);
			txResp.setTransactionAmount(splitArray[6]);
			txResp.setCurrencyCode(splitArray[7]);
			txResp.setPaymentMode(splitArray[8]);
			txResp.setResponseDateTime(splitArray[9]);
			txResp.setSurePayTxnId(splitArray[10]);
			txResp.setBankTransactionNo(splitArray[11]);
			txResp.setTransactionStatus(splitArray[12]);
			txResp.setAdditionalInfo1(splitArray[13]);
			txResp.setAdditionalInfo2(splitArray[14]);
			txResp.setAdditionalInfo3(splitArray[15]);
			txResp.setAdditionalInfo4(splitArray[16]);
			txResp.setAdditionalInfo5(splitArray[17]);
			txResp.setErrorCode(splitArray[18]);
			txResp.setErrorDescription(splitArray[19]);
			txResp.setCheckSum(splitArray[20]);
			
			break;
		case "F":
		case "D":
			
			/*For Failure : 
			 FailureFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|BankTransactionNo|
			 ErrorCode|ErrorMessage|ErrorDescription|ResponseDateTime|CheckSum
			 */
			txResp.setSurePayMerchantId(splitArray[1]);
			txResp.setOrderId(splitArray[2]);
			txResp.setServiceId(splitArray[3]);
			txResp.setPaymentMode(splitArray[4]);
			txResp.setBankTransactionNo(splitArray[5]);
			txResp.setErrorCode(splitArray[6]);
			txResp.setErrorMessage(splitArray[7]);
			txResp.setErrorDescription(splitArray[8]);
			txResp.setResponseDateTime(splitArray[9]);
			txResp.setCheckSum(splitArray[10]);
			
			break;
		case "I":
			/* For Initiated : 
			 InitiatedFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|ErrorDescription|
			 ResponseDateTime|CheckSum
			 */
			txResp.setSurePayMerchantId(splitArray[1]);
			txResp.setOrderId(splitArray[2]);
			txResp.setServiceId(splitArray[3]);
			txResp.setPaymentMode(splitArray[4]);
			txResp.setErrorDescription(splitArray[5]);
			txResp.setResponseDateTime(splitArray[6]);
			txResp.setCheckSum(splitArray[7]);
			break;
		}
    	return txResp;
    }
     

}