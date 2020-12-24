package org.egov.web.notification.mail.service;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.egov.web.notification.mail.config.EmailProperties;
import org.egov.web.notification.mail.consumer.contract.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sun.mail.smtp.SMTPTransport;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(value = "mail.enabled", havingValue = "true")
@Slf4j
public class ExternalEmailService implements EmailService {
	
	@Autowired
    private EmailProperties emailProperties;

	public static final String EXCEPTION_MESSAGE = "Exception creating HTML email";
	//private JavaMailSenderImpl mailSender;

    public ExternalEmailService(JavaMailSenderImpl mailSender) {
        //this.mailSender = mailSender;
    }
    
    @Override
	public void sendEmail(Email email) {
		/*
		 * if(email.isHTML()) { sendHTMLEmail(email); } else { sendTextEmail(email); }
		 */
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", emailProperties.getMailHost());
		properties.setProperty("mail.smtp.port", emailProperties.getMailPort().toString());
		properties.setProperty("mail.protocol", emailProperties.getMailProtocol());
		properties.setProperty("mail.smtps.starttls.enable", emailProperties.getMailStartTlsEnabled());
		properties.setProperty("mail.smtp.auth",emailProperties.getMailSmtpsAuth());
		properties.setProperty("mail.user", emailProperties.getMailSenderUsername());
		properties.setProperty("mail.password", emailProperties.getMailSenderPassword());
		properties.setProperty("mail.smtps.debug", emailProperties.getMailSmtpsDebug());
		Session session = Session.getDefaultInstance(properties);
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailProperties.getMailSenderUsername()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", email.getEmailTo())));
			message.setSubject(email.getSubject());
			if(email.isHTML()) 
				message.setContent(email.getBody(),"text/html");
			else
				message.setText(email.getBody());
			Transport tp = session.getTransport(emailProperties.getMailProtocol());
			tp.connect(emailProperties.getMailHost(),emailProperties.getMailPort(),
				emailProperties.getMailSenderUsername(),emailProperties.getMailSenderPassword());
			if(emailProperties.isMailEnableFlag()) {
			tp.sendMessage(message, message.getAllRecipients());
			if(emailProperties.isMailResponseDebug()) {
				log.info("Mail : "+email.getBody());
				for(Address a :message.getRecipients(Message.RecipientType.TO)){
					log.info("To :"+a.toString());
				}
				log.info("LastServerResponse from server : " + ((SMTPTransport) tp).getLastServerResponse());
				log.info("LastReturnCode from server : " + ((SMTPTransport) tp).getLastReturnCode());
			}
			}
			else {
				log.info("Mail disabled: "+email.getBody());
				for(Address a :message.getRecipients(Message.RecipientType.TO)){
					log.info("To :"+a.toString());
				}
			}
			tp.close();
		} catch (Exception e) {
			log.error("Error sending in mail.", e);
			throw new RuntimeException(e);
		}

	}

	/*private void sendTextEmail(Email email) {
		try {
			final SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(email.getEmailTo().toArray(new String[0]));
			mailMessage.setSubject(email.getSubject());
			mailMessage.setText(email.getBody());
			log.info("Text Mail sending to : "+email.getEmailTo().size());
			mailMessage.setFrom(mailSender.getUsername());
			mailSender.send(mailMessage);
			log.info("Mail sent");
		}
		catch(Exception e) {
			log.error("Error sending in mail."+e);
		}
	}

	private void sendHTMLEmail(Email email) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper;
		try {
			helper = new MimeMessageHelper(message, true);
			helper.setTo(email.getEmailTo().toArray(new String[0]));
			helper.setSubject(email.getSubject());
			helper.setText(email.getBody(), true);
		} catch (MessagingException e) {
			log.error(EXCEPTION_MESSAGE, e);
			throw new RuntimeException(e);
		}
		mailSender.send(message);
	}*/
}
