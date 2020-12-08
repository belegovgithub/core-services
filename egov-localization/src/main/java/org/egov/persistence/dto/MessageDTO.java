package org.egov.persistence.dto;

import java.io.Serializable;

import org.egov.domain.model.Message;
import org.egov.domain.model.MessageIdentity;
import org.egov.domain.model.Tenant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO implements Serializable {
	private String code;
	private String locale;
	private String module;
	private String tenant;
	private String message;
	
	@JsonInclude(Include.NON_NULL)
	private String templateId;

	public MessageDTO(Message domainMessage) {
		code = domainMessage.getCode();
		module = domainMessage.getModule();
		locale = domainMessage.getLocale();
		tenant = domainMessage.getTenant();
		message = domainMessage.getMessage();
		templateId = domainMessage.getTemplateId();
	}

	@JsonIgnore
	public Message toDomainMessage() {
		final Tenant tenant = new Tenant(this.tenant);
		final MessageIdentity messageIdentity = MessageIdentity.builder().code(code).module(module).locale(locale)
				.tenant(tenant).templateId(templateId).build();
		return Message.builder().messageIdentity(messageIdentity).message(message).build();
	}
}
