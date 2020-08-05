package org.egov.pg.web.contract;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
public class PgDetailRequest {
    
    
    @JsonProperty("RequestInfo")
	@NotNull
	@Valid
	private RequestInfo requestInfo = null;

	@JsonProperty("pgDetail")
	@Valid
	private List<PgDetail> pgDetail = null;
    
}


