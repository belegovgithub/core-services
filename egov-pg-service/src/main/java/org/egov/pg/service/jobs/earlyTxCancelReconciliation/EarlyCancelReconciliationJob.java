package org.egov.pg.service.jobs.earlyTxCancelReconciliation;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.config.AppProperties;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.service.TransactionService;
import org.egov.pg.service.UserService;
import org.egov.pg.web.models.TransactionCriteria;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Updates all transactions in pending state and created in the last 15 minutes
 */
@Component
@Slf4j
public class EarlyCancelReconciliationJob implements Job {

    private static final RequestInfo requestInfo;

    static {
    	requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", null);
    }

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserService userService;

    /**
     * Fetch live status for pending transactions
     * that were created for ex, between 40-80 minutes, configurable value
     *
     * @param jobExecutionContext execution context with optional job parameters
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
    	if(requestInfo.getUserInfo()==null) {
    		requestInfo.setUserInfo(userService.searchSystemUser(requestInfo, appProperties.getEarlyReconcileUserName() ));
    	}
        Integer startTime, endTime;

        startTime = appProperties.getEarlyTxCancelReconcileJobRunInterval() * 2;
        endTime = startTime - appProperties.getEarlyTxCancelReconcileJobRunInterval();
        
        List<Transaction> pendingTxns = transactionRepository.fetchTransactionsByTimeRange(TransactionCriteria.builder()
                        .txnStatus(Transaction.TxnStatusEnum.PENDING).build(),
                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(startTime),
                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(endTime));

        log.info("Attempting to reconcile {} pending transactions", pendingTxns.size());

        for (Transaction txn : pendingTxns) {
        	try {
        	 
            log.info(transactionService.updateTransaction(requestInfo, Collections.singletonMap(PgConstants.PG_TXN_IN_LABEL, txn
                    .getTxnId
                    ()),true).toString());
        	}catch (Exception e) {
				log.error("Error in early reconcile job ",e);
			}
        	
        }

    }

}