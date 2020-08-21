package org.egov.domain.exception;

import org.egov.domain.model.Token;

import lombok.Getter;

public class TokenGenerationAttemptsOverException extends RuntimeException {

	
	private static final long serialVersionUID = -7682510712584181850L;
	
	@Getter
    private Token token;

    public TokenGenerationAttemptsOverException(Token token) {    	
        this.token = token;
    }

}
