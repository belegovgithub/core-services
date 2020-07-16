package org.egov.pg.service.gateways.nic;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.Gateway;
import org.egov.pg.service.gateways.ccavenue.CCAvenueStatusResponse;
import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.omg.IOP.ServiceIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
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
 * AXIS Gateway implementation
 */
@Component
@Slf4j
public class NICGateway implements Gateway {

    private static final String GATEWAY_NAME = "NIC";
    private final String MESSAGE_TYPE;
    private final String MERCHANT_ID; 
 
    private final String SECURE_SECRET;
    private final String AMA_USER;
    private final String AMA_PWD;
 
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
    private final String ADDITIONAL_FIELD1_KEY = "additionalFeild1";
    private final String ADDITIONAL_FIELD2_KEY = "additionalFeild2";
    private final String ADDITIONAL_FIELD3_KEY = "additionalFeild3";
    private final String ADDITIONAL_FIELD4_KEY = "additionalFeild4";
    private final String ADDITIONAL_FIELD5_KEY = "additionalFeild5";
    private final String GATEWAY_TRANSACTION_STATUS_URL;
    private final String GATEWAY_URL;
    private static final String SEPERATOR ="|";
    
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
        MERCHANT_ID = environment.getRequiredProperty("nic.merchant.id");
        SECURE_SECRET = environment.getRequiredProperty("nic.merchant.secret.key");
        AMA_USER = environment.getRequiredProperty("nic.merchant.user");
        AMA_PWD = environment.getRequiredProperty("nic.merchant.pwd");
        
        REDIRECT_URL = environment.getRequiredProperty("nic.redirect.url");
        ORIGINAL_RETURN_URL_KEY = environment.getRequiredProperty("nic.original.return.url.key");
        GATEWAY_TRANSACTION_STATUS_URL = environment.getRequiredProperty("nic.gateway.status.url");
        GATEWAY_URL = environment.getRequiredProperty("nic.gateway.url");
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
    	return null;
    }
    
    @Override
    public String generateRedirectURIPost(Transaction transaction) {
		/*
		 * 
		 messageType|merchantId|serviceId|orderId|customerId|transactionAmount|currencyCode|r
		equestDateTime|successUrl|failUrl|additionalFeild1| additionalFeild2| additionalFeild3|
		additionalFeild4| additionalFeild5
		 */
    	String urlData =null;
    	HashMap<String, String> queryMap = new HashMap<>();
         queryMap.put(MESSAGE_TYPE_KEY, MESSAGE_TYPE);
         queryMap.put(MERCHANT_ID_KEY, MERCHANT_ID);
         queryMap.put(SERVICE_ID_KEY, "SecunderabadChhawani");
         queryMap.put(ORDER_ID_KEY, transaction.getTxnId());
         queryMap.put(CUSTOMER_ID_KEY, transaction.getUser().getUuid());
         queryMap.put(TRANSACTION_AMOUNT_KEY, String.valueOf(Utils.formatAmtAsPaise(transaction.getTxnAmount())));
         queryMap.put(CURRENCY_CODE_KEY,CURRENCY_CODE);
         SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");
     	 queryMap.put(REQUEST_DATE_TIME_KEY, format.format(new Date()));
         queryMap.put(SUCCESS_URL_KEY, getReturnUrl(transaction.getCallbackUrl(), REDIRECT_URL));
         queryMap.put(FAIL_URL_KEY, getReturnUrl(transaction.getCallbackUrl(), REDIRECT_URL));
         queryMap.put(ADDITIONAL_FIELD1_KEY, ""); //Not in use 
         queryMap.put(ADDITIONAL_FIELD2_KEY, ""); //Not in use 
         queryMap.put(ADDITIONAL_FIELD3_KEY, ""); //Not in use 
         queryMap.put(ADDITIONAL_FIELD4_KEY, ""); //Not in use 
         queryMap.put(ADDITIONAL_FIELD5_KEY, ""); //Not in use 
         
         
         System.out.println("queryMap  "+ queryMap);
         
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
     	queryMap.put("checksum", NICUtils.generateCRC32Checksum(message, SECURE_SECRET));
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
    

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> param) {

        try {
        	String requestmsg =SEPERATOR+ MERCHANT_ID +SEPERATOR+currentStatus.getTxnId();
            HashMap<String, String> params = new HashMap<>();
            params.put("username", AMA_USER);
            params.put("password", AMA_PWD);
            params.put("request_Msg", requestmsg); 
            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(GATEWAY_TRANSACTION_STATUS_URL)
                    .buildAndExpand(params).encode();
            log.debug("Status URL : "+uriComponents.toUri());
            ResponseEntity<String> response = restTemplate.postForEntity(uriComponents.toUri(),"", String.class);
            Transaction transaction = transformRawResponse(response.getBody(), currentStatus);
            log.info("Updated transaction : " + transaction.toString());
            return transaction;
        } catch (RestClientException e) {
            log.error("Unable to fetch status from ccavenue gateway", e);
            throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from ccavenue gateway");
        } catch (Exception e) {
            log.error("ccavenue Checksum generation failed", e);
            throw new CustomException("CHECKSUM_GEN_FAILED",
                    "Checksum generation failed, gateway redirect URI cannot be generated");
        }
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

     

    private Transaction transformRawResponse(String resp, Transaction currentStatus)
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
        	
        	NICUtils.validateTransaction(respMap.get("msg"), SECURE_SECRET);
        	
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
