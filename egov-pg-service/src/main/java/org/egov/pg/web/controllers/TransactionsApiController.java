package org.egov.pg.web.controllers;


import lombok.extern.slf4j.Slf4j;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.config.AppProperties;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.service.GatewayService;
import org.egov.pg.service.TransactionService;
import org.egov.pg.utils.ResponseInfoFactory;
import org.egov.pg.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Endpoints to deal with all payment related operations
 */

@Slf4j
@Controller
public class TransactionsApiController {

    private final TransactionService transactionService;
    private final GatewayService gatewayService;


    @Autowired
    public TransactionsApiController(TransactionService transactionService, GatewayService
            gatewayService) {
        this.transactionService = transactionService;
        this.gatewayService = gatewayService;
    }


    /**
     * Initiates a new payment transaction, on successful validation, a redirect is issued to the payment gateway.
     *
     * @param transactionRequest Request containing all information necessary for initiating payment
     * @return Transaction that has been created
     */
    @RequestMapping(value = "/transaction/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<TransactionCreateResponse> transactionsV1CreatePost(@Valid @RequestBody TransactionRequest transactionRequest) {

        Transaction transaction = transactionService.initiateTransaction(transactionRequest);
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(transactionRequest
                .getRequestInfo(), true);
        TransactionCreateResponse response = new TransactionCreateResponse(responseInfo, transaction);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Returns the current status of a transaction in our systems;
     * This does not guarantee live payment gateway status.
     *
     * @param requestInfoWrapper  Request Info
     * @param transactionCriteria Search Conditions that should be matched
     * @return List of transactions matching the search criteria
     */
    @RequestMapping(value = "/transaction/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<TransactionResponse> transactionsV1SearchPost(@Valid @RequestBody RequestInfoWrapper
                                                                                requestInfoWrapper, @Valid
                                                                        @ModelAttribute TransactionCriteria transactionCriteria) {
        transactionCriteria.setOffset(0);
        transactionCriteria.setLimit(5);
        List<Transaction> transactions = transactionService.getTransactions(transactionCriteria);
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper
                .getRequestInfo(), true);
        TransactionResponse response = new TransactionResponse(responseInfo, transactions);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    /**
     * Updates the status of the transaction from the gateway
     *
     * @param params             Parameters posted by the gateway
     * @return The current transaction status of the transaction
     */
    @RequestMapping(value = "/transaction/v1/_update", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<TransactionResponse> transactionsV1UpdatePost(@RequestBody RequestInfoWrapper
                                                                                requestInfoWrapper, @RequestParam
                                                                                Map<String,
                                                                                        String> params) {
        List<Transaction> transactions = transactionService.updateTransaction(requestInfoWrapper.getRequestInfo(), params);
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper
                .getRequestInfo(), true);
        TransactionResponse response = new TransactionResponse(responseInfo, transactions);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Active payment gateways that can be used for payments
     *
     * @return list of active gateways that can be used for payments
     */
    @RequestMapping(value = "/gateway/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<Set<String>> transactionsV1AvailableGatewaysPost() {

        Set<String> gateways = gatewayService.getActiveGateways();
        log.debug("Available gateways : " + gateways);
        return new ResponseEntity<>(gateways, HttpStatus.OK);
    }
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AppProperties appProperties;
    
    @RequestMapping(value = "/gateway/v1/_schedular", method = RequestMethod.POST)
    public ResponseEntity<Set<String>> transactionsV1Gateway() {

    	 List<Transaction> pendingTxns = transactionRepository.fetchTransactionsByTimeRange(TransactionCriteria.builder()
                 .txnStatus(Transaction.TxnStatusEnum.PENDING).build(), 0L,
         System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(appProperties.getEarlyReconcileJobRunInterval
                 () * 2));
    	 
    	 User userInfo = User.builder()
                 .uuid("DAILY_RECONC_JOB")
                 .type("SYSTEM")
                 .roles(Collections.emptyList()).id(0L).build();

    	 RequestInfo requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", userInfo);

		 log.info("Attempting to reconcile {} pending transactions", pendingTxns.size());
		 int count =0;
		 for (Transaction txn : pendingTxns) {
		 	try {
		 		log.info(transactionService.updateTransaction(requestInfo, Collections.singletonMap(PgConstants.PG_TXN_IN_LABEL, txn
		                 .getTxnId
		                 ())).toString());	
		 	}catch (Exception e) {
					log.error("Error in daily reconcile job",e);
					count ++;
				}
		 	if(count >5) {
		 		break;
		 	}
		     
		 }
    	
        Set<String> gateways =new HashSet<String>();
        log.debug("Available gateways : " + gateways);
        return new ResponseEntity<>(gateways, HttpStatus.OK);
    }

}
