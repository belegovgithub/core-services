package org.egov.web.adapter.error;

import java.util.Collections;

import org.egov.common.contract.response.Error;
import org.egov.common.contract.response.ErrorField;
import org.egov.common.contract.response.ErrorResponse;
import org.egov.domain.model.Token;
import org.springframework.http.HttpStatus;

public class TokenGenerationAttemptsOverAdapter implements ErrorAdapter<Token> {

    private static final String GENERATE_OTP_ATTEMPTS_FAILURE_EXCEPTION = "Exceeded 3 attempts to generate OTP try after 1 hr";
    private static final String GENERATE_OTP_ATTEMPTS_FAILURE_CODE = "OTP.GENERATE_FAILED";
    private static final String EMPTY = "";

    @Override
    public ErrorResponse adapt(Token model) {
    
        final Error error = Error.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(GENERATE_OTP_ATTEMPTS_FAILURE_EXCEPTION)
                .fields(Collections.singletonList(getError()))
                .build();
       
        return new ErrorResponse(null, error);
    }

    private ErrorField getError() {
        return ErrorField.builder()
                .code(GENERATE_OTP_ATTEMPTS_FAILURE_CODE)
                .message(GENERATE_OTP_ATTEMPTS_FAILURE_EXCEPTION)
                .field(EMPTY)
                .build();
    }

}
