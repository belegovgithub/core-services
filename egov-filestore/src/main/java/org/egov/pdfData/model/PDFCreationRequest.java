package org.egov.pdfData.model;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PDFCreationRequest {
	
	@JsonProperty("RequestInfo")
	 @Valid
    private RequestInfo requestInfo = null;
	
	 @JsonProperty("Challan")
     @Valid
     private Object pdfData = null;

}
