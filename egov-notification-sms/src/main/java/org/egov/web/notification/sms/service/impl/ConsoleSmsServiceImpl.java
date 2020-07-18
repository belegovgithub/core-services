package org.egov.web.notification.sms.service.impl;

import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.SMSService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;


@Service
@ConditionalOnProperty(value = "sms.gateway.to.use", havingValue ="CONSOLE_SMS", matchIfMissing = true)
public class ConsoleSmsServiceImpl implements SMSService{
	
	@Override
	public void sendSMS(Sms sms) {
		System.out.println(String.format("Sending SMS to %s : %s ",sms.getMobileNumber() , sms.getMessage()));
	}
	
}
