package org.egov.pg.service.gateways.nic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.PgDetail;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.service.Gateway;
import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

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
    private final String GATEWAY_TRANSACTION_STATUS_URL_WITHIP; 
    private final String GATEWAY_URL;
    private final String CITIZEN_URL;
    private static final String SEPERATOR ="|";
    private String TX_DATE_FORMAT;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    private  final RequestInfo requestInfo;
    private PgDetailRepository pgDetailRepository;
    
    /**
     * Initialize by populating all required config parameters
     *
     * @param restTemplate rest template instance to be used to make REST calls
     * @param environment containing all required config parameters
     */
    @Autowired
    public NICGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper,PgDetailRepository pgDetailRepository) {
        this.restTemplate = restTemplate;
        ACTIVE = Boolean.valueOf(environment.getRequiredProperty("nic.active"));
        MESSAGE_TYPE = environment.getRequiredProperty("nic.messagetype");
        CURRENCY_CODE = environment.getRequiredProperty("nic.currency");
        REDIRECT_URL = environment.getRequiredProperty("nic.redirect.url");
        ORIGINAL_RETURN_URL_KEY = environment.getRequiredProperty("nic.original.return.url.key");
        GATEWAY_TRANSACTION_STATUS_URL = environment.getRequiredProperty("nic.gateway.status.url");
        GATEWAY_TRANSACTION_STATUS_URL_WITHIP= environment.getRequiredProperty("nic.gateway.status.url.withip");
        CITIZEN_URL = environment.getRequiredProperty("egov.default.citizen.url");
        GATEWAY_URL = environment.getRequiredProperty("nic.gateway.url");
        TX_DATE_FORMAT =environment.getRequiredProperty("nic.dateformat");
        User userInfo = User.builder()
                .uuid("PG_DETAIL_GET")
                .type("SYSTEM")
                .roles(Collections.emptyList()).id(0L).build();

        requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", userInfo);
        this.pgDetailRepository=pgDetailRepository;
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
    	return null;
    }
    
    @Override
    public String generateRedirectFormData(Transaction transaction) {
    	PgDetail pgDetail = pgDetailRepository.getPgDetailByTenantId(requestInfo, transaction.getTenantId());
    	
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
         queryMap.put(TRANSACTION_AMOUNT_KEY, String.valueOf( transaction.getTxnAmount()));
         queryMap.put(CURRENCY_CODE_KEY,CURRENCY_CODE);
         SimpleDateFormat format = new SimpleDateFormat(TX_DATE_FORMAT);
     	 queryMap.put(REQUEST_DATE_TIME_KEY, format.format(new Date()));
     	 String returnUrl = transaction.getCallbackUrl().replace(CITIZEN_URL, "");
     	 
     	 
     	 
     	 
         queryMap.put(SUCCESS_URL_KEY, getReturnUrl(returnUrl, REDIRECT_URL));
         queryMap.put(FAIL_URL_KEY, getReturnUrl(returnUrl, REDIRECT_URL));
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
     	} catch (Exception e) {
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
    	PgDetail pgDetail = pgDetailRepository.getPgDetailByTenantId(requestInfo, currentStatus.getTenantId());
    	log.info("tx input ", currentStatus);
    	try {
    		TrustStrategy acceptTrustStrategy = (cert, authType) -> true;
        	SSLContext context = SSLContexts.custom().loadTrustMaterial(null, acceptTrustStrategy).build();
        	BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        	credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(pgDetail.getMerchantUserName(), pgDetail.getMerchantPassword()));
        	 
        	CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context).
        			setDefaultCredentialsProvider(credentialsProvider).build();
        	HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        	RestTemplate template = new RestTemplate(factory);//.requestFactory(factory).build();
        	
        	MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        	String requestmsg =SEPERATOR+ pgDetail.getMerchantId() +SEPERATOR+currentStatus.getTxnId();
            params.add("requestMsg", requestmsg);
        	
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        	HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
 
        	ResponseEntity<String> response = template.postForEntity(GATEWAY_TRANSACTION_STATUS_URL,entity, String.class);
        	return transformRawResponse(response.getBody(), currentStatus, pgDetail.getMerchantSecretKey());
    	} catch (HttpStatusCodeException ex) {
    		log.info("Error code "+ex.getStatusCode());
    		log.info("Error getResponseBodyAsString code "+ex.getResponseBodyAsString());
    		try {
				NICStatusResponse errorResponse = new ObjectMapper().readValue(ex.getResponseBodyAsString(),NICStatusResponse.class);
				//Error 404 --> No Data Found for given Request and 408 --> Session Time Out Error if not transaction has been initiated for 15 min 
				if(errorResponse.getErrorCode().equals("404")||errorResponse.getErrorCode().equals("408")) {
					Transaction txStatus = Transaction.builder().txnId(currentStatus.getTxnId())
	                        .txnStatus(Transaction.TxnStatusEnum.FAILURE)
	                        .txnStatusMsg(PgConstants.TXN_FAILURE_GATEWAY)
	                        .gatewayStatusCode(errorResponse.getErrorCode()).gatewayStatusMsg(errorResponse.getErrorMessage())
	                        .responseJson(ex.getResponseBodyAsString()).build();
					return txStatus;
				}
			} catch (Exception e) {
				log.error("Error in response transform",e);
			}

    		log.error("Unable to fetch status from NIC gateway ", ex);
            throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from NIC gateway");
        } catch (RestClientException e) {
            log.error("Unable to fetch status from NIC gateway ", e);
            throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from NIC gateway");
        } catch (Exception e) {
            log.error("NIC Checksum validation failed ", e);
            throw new CustomException("CHECKSUM_GEN_FAILED","Checksum generation failed, gateway redirect URI cannot be generated");
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

     

    /**
     * Transform the Response string into NICStatusResponse object and return the transaction detail 
     * @param resp
     * @param currentStatus
     * @param secretKey
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private Transaction transformRawResponse(String resp, Transaction currentStatus, String secretKey)
            throws JsonParseException, JsonMappingException, IOException {
    	log.info("Response Data "+resp);
        if (resp!=null) {
        	
        	//Validate the response against the checksum
        	NICUtils.validateTransaction(resp, secretKey);
        	
        	String[] splitArray = resp.split("[|]");
        	Transaction txStatus=null;
        	NICStatusResponse statusResponse = new NICStatusResponse(splitArray[0]);
        	int index =0;
        	switch (statusResponse.getTxFlag()) {
    		case "S":
    			/*For Success : 
    			SuccessFlag|MessageType|SurePayMerchantId|ServiceId|OrderId|CustomerId|TransactionAmount|
    			CurrencyCode|PaymentMode|ResponseDateTime|SurePayTxnId|
    			BankTransactionNo|TransactionStatus|AdditionalInfo1|AdditionalInfo2|AdditionalInfo3|
    			AdditionalInfo4|AdditionalInfo5|ErrorCode|ErrorDescription|CheckSum*/
    			/*
    			 * Sample Response :
    			 * S|0100|UATSCBSG0000000207|SecuChhawani|PB_PG_2020_07_20_000153_16|
    			 * 9eb6f880-c22f-4c1e-8f99-106bb3e0e60a|600.00|INR|UPI|20-07-2020|13557|pay_FGkHC8M8edSAmW|A|111111|111111|111111|111111|111111||| 
    			 */
    			
    			statusResponse.setMessageType(splitArray[++index]);
    			statusResponse.setSurePayMerchantId(splitArray[++index]);
    			statusResponse.setServiceId(splitArray[++index]);
    			statusResponse.setOrderId(splitArray[++index]);
    			statusResponse.setCustomerId(splitArray[++index]);
    			statusResponse.setTransactionAmount(splitArray[++index]);
    			statusResponse.setCurrencyCode(splitArray[++index]);
    			statusResponse.setPaymentMode(splitArray[++index]);
    			statusResponse.setResponseDateTime(splitArray[++index]);
    			statusResponse.setSurePayTxnId(splitArray[++index]);
    			statusResponse.setBankTransactionNo(splitArray[++index]);
    			statusResponse.setTransactionStatus(splitArray[++index]);
    			statusResponse.setAdditionalInfo1(splitArray[++index]);
    			statusResponse.setAdditionalInfo2(splitArray[++index]);
    			statusResponse.setAdditionalInfo3(splitArray[++index]);
    			statusResponse.setAdditionalInfo4(splitArray[++index]);
    			statusResponse.setAdditionalInfo5(splitArray[++index]);
    			statusResponse.setErrorCode(splitArray[++index]);
    			statusResponse.setErrorDescription(splitArray[++index]);
    			statusResponse.setCheckSum(splitArray[++index]);
    			//Build tx Response object
    			txStatus = Transaction.builder().txnId(currentStatus.getTxnId())
                        .txnAmount(Utils.formatAmtAsRupee(statusResponse.getTransactionAmount()))
                        .txnStatus(Transaction.TxnStatusEnum.SUCCESS)
                        .txnStatusMsg(PgConstants.TXN_SUCCESS)
                        .gatewayTxnId(statusResponse.getSurePayTxnId())
                        .bankTransactionNo(statusResponse.getBankTransactionNo())
                        .gatewayPaymentMode(statusResponse.getPaymentMode())
                        .gatewayStatusCode(statusResponse.getTransactionStatus()).gatewayStatusMsg(statusResponse.getTransactionStatus())
                        .responseJson(resp).build();
    			
    			break;
    		case "F":
    			/*
    			 * FailureFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|BankTransactionNo|
    			 ErrorCode|ErrorMessage|ErrorDescription|ResponseDateTime|CheckSum
    			 
    			 F|UATSCBSG0000000207|PB_PG_2020_07_22_000183_35|SecuChhawani|Wallet|
    			 pay_FHWjr1cdBNUt7y|400|PAYMENT_DECLINED_A|Payment failed|2020-07-22 17:06:06.366|1326393779
    			 */
    			statusResponse.setSurePayMerchantId(splitArray[++index]);
    			statusResponse.setOrderId(splitArray[++index]);
    			statusResponse.setServiceId(splitArray[++index]);
    			statusResponse.setPaymentMode(splitArray[++index]);
    			statusResponse.setBankTransactionNo(splitArray[++index]);
    			statusResponse.setErrorCode(splitArray[++index]);
    			statusResponse.setErrorMessage(splitArray[++index]);
    			statusResponse.setErrorDescription(splitArray[++index]);
    			statusResponse.setResponseDateTime(splitArray[++index]);
    			statusResponse.setCheckSum(splitArray[++index]);
    			String txStatusMsg =PgConstants.TXN_FAILURE_GATEWAY;
    			if(statusResponse.getErrorMessage().equalsIgnoreCase("PAYMENT_DECLINED_A")) {
    				txStatusMsg="Transaction Failed At Aggregator";
    			}else if(statusResponse.getErrorMessage().equalsIgnoreCase("PAYMENT_DECLINED_M")) {
    				txStatusMsg="Transaction Failed At Merchant ";
    			}else if(statusResponse.getErrorMessage().equalsIgnoreCase("PAYMENT_DECLINED_S")) {
    				txStatusMsg="Transaction Failed At Surepay";
    			}
    			
    			//Build tx Response object
    			txStatus = Transaction.builder().txnId(currentStatus.getTxnId())
                        .txnStatus(Transaction.TxnStatusEnum.FAILURE)
                        .txnStatusMsg(txStatusMsg)
                        .gatewayTxnId(statusResponse.getSurePayTxnId())
                        .gatewayPaymentMode(statusResponse.getPaymentMode())
                        .bankTransactionNo(statusResponse.getBankTransactionNo())
                        .gatewayStatusCode(statusResponse.getErrorCode()).gatewayStatusMsg(statusResponse.getErrorMessage())
                        .responseJson(resp).build();
    			break;
    			
    		case "D":
    			index =0;
    			/*For Failure : 
    			 FailureFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|BankTransactionNo|
    			 ErrorCode|ErrorMessage|ErrorDescription|ResponseDateTime|CheckSum
    			 
    			 D|UATCBLSG0000000205|PB_PG_2020_07_22_000167_61|
    			 LuckChhawani||PAYMENT_DECLINED_M|2020-07-22 09:55:56.236|1250432021
    			 
    			 */
    			statusResponse.setSurePayMerchantId(splitArray[++index]);
    			statusResponse.setOrderId(splitArray[++index]);
    			statusResponse.setServiceId(splitArray[++index]);
    			statusResponse.setPaymentMode(splitArray[++index]);
    			//statusResponse.setBankTransactionNo(splitArray[++index]);
    			//statusResponse.setErrorCode(splitArray[++index]);
    			statusResponse.setErrorMessage(splitArray[++index]);
    			//statusResponse.setErrorDescription(splitArray[++index]);
    			statusResponse.setResponseDateTime(splitArray[++index]);
    			statusResponse.setCheckSum(splitArray[++index]);
    			//Build tx Response object
    			String txStatusMsgDecline =PgConstants.TXN_FAILURE_GATEWAY;
    			if(statusResponse.getErrorMessage().equalsIgnoreCase("PAYMENT_DECLINED_A")) {
    				txStatusMsgDecline="Transaction Failed At Aggregator";
    			}else if(statusResponse.getErrorMessage().equalsIgnoreCase("PAYMENT_DECLINED_M")) {
    				txStatusMsgDecline="Transaction Failed At Merchant ";
    			}else if(statusResponse.getErrorMessage().equalsIgnoreCase("PAYMENT_DECLINED_S")) {
    				txStatusMsgDecline="Transaction Failed At Surepay";
    			}
    			txStatus = Transaction.builder().txnId(currentStatus.getTxnId())
                        .txnStatus(Transaction.TxnStatusEnum.FAILURE)
                        .txnStatusMsg(txStatusMsgDecline)
                        .gatewayTxnId(statusResponse.getSurePayTxnId())
                        .gatewayPaymentMode(statusResponse.getPaymentMode())
                        .bankTransactionNo(statusResponse.getBankTransactionNo())
                        .gatewayStatusCode(statusResponse.getTxFlag()).gatewayStatusMsg(statusResponse.getErrorMessage())
                        .responseJson(resp).build();
    			break;
    		case "I":
    			/* For Initiated : 
    			 InitiatedFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|ErrorDescription|
    			 ResponseDateTime|CheckSum
    			 
    			 I|UATSCBSG0000000207|PB_PG_2020_07_22_000168_45|SecuChhawani||
    			 ORDER_INITIATED|2020-07-22 10:27:28.312|481313839
    			 */
    			statusResponse.setSurePayMerchantId(splitArray[++index]);
    			statusResponse.setOrderId(splitArray[++index]);
    			statusResponse.setServiceId(splitArray[++index]);
    			statusResponse.setPaymentMode(splitArray[++index]);
    			statusResponse.setErrorDescription(splitArray[++index]);
    			statusResponse.setResponseDateTime(splitArray[++index]);
    			statusResponse.setCheckSum(splitArray[++index]);
    			//Build tx Response object
    			txStatus = Transaction.builder().txnId(currentStatus.getTxnId())
                        .txnStatus(Transaction.TxnStatusEnum.PENDING)
                        .gatewayPaymentMode(statusResponse.getPaymentMode())
                        .gatewayStatusCode(statusResponse.getTxFlag())
                        .gatewayStatusMsg(statusResponse.getErrorDescription())
                        .responseJson(resp).build();
    			break;
    		  default : 
    			  throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch Status of transaction");
    		}
        	log.info("Encoded value "+resp);
        	log.info("NICStatusResponse --> "+statusResponse);
        	log.info("Transaction --> "+txStatus);
        	return txStatus;
        } else {
            log.error("Received error response from status call : " + resp);
            throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from nic gateway");
        }
    }
    
     
     

}
