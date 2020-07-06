package org.egov.pg.service.gateways.nic;

import org.egov.pg.models.Transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NICStatusResponse {
	//Message Format
	/*For Success : 
	SuccessFlag|MessageType|SurePayMerchantId|ServiceId|OrderId|CustomerId|TransactionAmount|
	CurrencyCode|PaymentMode|ResponseDateTime|SurePayTxnId|
	BankTransactionNo|TransactionStatus|AdditionalInfo1|AdditionalInfo2|AdditionalInfo3|
	AdditionalInfo4|AdditionalInfo5|ErrorCode|ErrorDescription|CheckSum*/
	
	/*For Failure : 
	 FailureFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|BankTransactionNo|
	 ErrorCode|ErrorMessage|ErrorDescription|ResponseDateTime|CheckSum
	 */
	/* For Initiated : 
	 InitiatedFlag|SurePayMerchantId|OrderId|ServiceId|PaymentMode|ErrorDescription|
	 ResponseDateTime|CheckSum
	 */
	
	
	public NICStatusResponse(String status) {
		txStatus = Transaction.TxnStatusEnum.fromValue(status);
	}
	
	private Transaction.TxnStatusEnum txStatus;
	private String messageType;
	private String surePayMerchantId;
	private String serviceId;
	private String orderId;
	private String customerId;
	private String transactionAmount;
	
	private String currencyCode;
	private String paymentMode;
	private String responseDateTime;
	private String surePayTxnId;
	private String bankTransactionNo;
	private String transactionStatus;
	private String additionalInfo1;
	private String additionalInfo2;
	private String additionalInfo3;
	private String additionalInfo4;
	private String additionalInfo5;
	private String errorCode;
	private String errorMessage;
	private String errorDescription;
	private String checkSum;
	
 
	
}
