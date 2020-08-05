package org.egov.pg.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.pg.repository.ServiceRequestRepository;
import org.egov.pg.web.contract.PgDetailRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.JsonPath;

@Component
public class PgDetailUtils {
	
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint; 

    private ServiceRequestRepository serviceRequestRepository;

     

    @Autowired
    public PgDetailUtils( ServiceRequestRepository serviceRequestRepository ) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

	
	/**
	 * A common method that builds MDMS request for searching master data.
	 * 
	 * @param uri
	 * @param tenantId
	 * @param module
	 * @param master
	 * @param filter
	 * @param requestInfo
	 * @return
	 */
    private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo,String tenantId){
        ModuleDetail financialYearRequest = getFinancialYearRequest();
        

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(financialYearRequest);
        
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }
    
    /**
     * Creates request to search financialYear in mdms
     * @return MDMS request for financialYear
     */
    private ModuleDetail getFinancialYearRequest() {

        // master details for TL module
        List<MasterDetail> tlMasterDetails = new ArrayList<>();

        // filter to only get code field from master data

        final String filterCodeForUom = "$.[?(@.type=='CITY')]";

        tlMasterDetails.add(MasterDetail.builder().name("tenants").filter(filterCodeForUom).build());

        ModuleDetail tlModuleDtls = ModuleDetail.builder().masterDetails(tlMasterDetails)
                .moduleName("tenant").build();


  /*      MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Collections.singletonList(tlModuleDtls)).tenantId(tenantId)
                .build();*/

        return tlModuleDtls;
    }
	 
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(mdmsHost).append(mdmsEndpoint);
    }
    public Object mDMSCall(PgDetailRequest tradeLicenseRequest){
        RequestInfo requestInfo = tradeLicenseRequest.getRequestInfo();
        String tenantId = tradeLicenseRequest.getPgDetail().get(0).getTenantId();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo,tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl() , mdmsCriteriaReq);
        
        String jsonPath = "$.MdmsRes.tenant.tenants[*].code";
        List<String> jsonOutput =  JsonPath.read(result, jsonPath);
        System.out.println("jsonOutput"+jsonOutput);
        return result;
    }
}
