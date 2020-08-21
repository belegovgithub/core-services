package org.egov.web.error;

import java.util.ArrayList;
import java.util.List;
import org.egov.web.contract.Error;
import org.egov.web.contract.ErrorField;
import org.egov.web.contract.ErrorResponse;
import org.springframework.http.HttpStatus;

public class TokenGenerationAttemptsOverErrorAdapter implements ErrorAdapter<Void> {

    private static final String GENERATE_OTP_ATTEMPTS_FAILURE_EXCEPTION = "Exceeded 3 attempts to generate OTP try after 1 hr";
    private static final String GENERATE_OTP_ATTEMPTS_FAILURE_CODE = "OTP.GENERATE_FAILED";
    private static final String EMPTY = "";

    @Override
	public ErrorResponse adapt(Void model) {
    	final Error error = getError();
		return new ErrorResponse(null, error);
	}

    private Error getError() {
		List<ErrorField> errorFields = getErrorFields();
		return Error.builder()
				.code(HttpStatus.BAD_REQUEST.value())
				.message(GENERATE_OTP_ATTEMPTS_FAILURE_EXCEPTION)
				.fields(errorFields)
				.build();
	}
    private List<ErrorField> getErrorFields() {
		List<ErrorField> errorFields = new ArrayList<>();
		final ErrorField latitudeErrorField = ErrorField.builder()
				.code(GENERATE_OTP_ATTEMPTS_FAILURE_CODE)
				.message(GENERATE_OTP_ATTEMPTS_FAILURE_EXCEPTION)
				.field(EMPTY)
				.build();
		errorFields.add(latitudeErrorField);
		return errorFields;
	}


}
