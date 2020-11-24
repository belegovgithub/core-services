package org.egov.web.notification.mail.consumer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.egov.web.notification.mail.config.ApplicationConfiguration;
import org.egov.web.notification.mail.consumer.contract.Email;
import org.egov.web.notification.mail.repository.UserRepository;
import org.egov.web.notification.mail.service.EmailService;
import org.egov.web.notification.mail.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SmsNotificationListener {

	private UserRepository userRepository;
	private ObjectMapper objectMapper;
	
	private ApplicationConfiguration config;

	private EmailService emailService;

	@Value("${email.subject}")
	private String subject;

	@Autowired
	public SmsNotificationListener(UserRepository userRepository, ApplicationConfiguration config,
			EmailService emailService,ObjectMapper mapper) {
		this.userRepository = userRepository;
		this.config = config;
		this.emailService = emailService;
		this.objectMapper =mapper;
	}

	@KafkaListener(topics = "${kafka.topics.notification.sms.topic.name}")
	public void process(final HashMap<String, Object> record) {
		System.out.println("record" + record);
		try {
			String rcvData = objectMapper.writeValueAsString(record);
			System.out.println(rcvData);

			List<String> emails = new ArrayList<String>();
			if (record.containsKey("users")) {
				ArrayList<String> users = null;
				if (record.get("users").getClass().equals(ArrayList.class)) {
					users = (ArrayList<String>) record.get("users");
					for (String uuid : users) {
						List<String> em = userRepository.getUserDetails(config.getStateTenantId(), null, uuid);
						if (em != null) {
							emails.addAll(em);
						}
					}
				}

			} else {
				emails = userRepository.getUserDetails(config.getStateTenantId(),
						(String) record.get(Constants.SMS_REQ_MOBILE_NO_KEY_NAME), null);
			}
			if (!CollectionUtils.isEmpty(emails))
				emailService.sendEmail(
						getEmailReq(getValideEmails(emails), (String) record.get(Constants.SMS_REQ_MSG_KEY_NAME)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Email getEmailReq(Set<String> emails, String msg) {
		return Email.builder().emailTo(emails).body(msg).subject(subject).build();
	}

	private static Set<String> getValideEmails(List<String> emails) {
		Set<String> validUniqueEmails = new HashSet<>();
		for (String email : emails) {
			if (isValid(email))
				validUniqueEmails.add(email);
		}
		
		return validUniqueEmails;
	}

	private static boolean isValid(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";

		Pattern pat = Pattern.compile(emailRegex);
		if (email == null)
			return false;
		return pat.matcher(email).matches();
	}

}
