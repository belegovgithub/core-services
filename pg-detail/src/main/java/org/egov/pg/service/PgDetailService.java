
package org.egov.pg.service;
import java.util.ArrayList;
import java.util.List;


import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pg.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.utils.PgDetailUtils;
import org.egov.pg.utils.ResponseInfoFactory;
import org.egov.pg.web.contract.PgDetail;
import org.egov.pg.web.contract.PgDetailRequest;
import org.egov.pg.web.contract.PgDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j; 
@Service
@Slf4j
public class PgDetailService {
	
	@Autowired
	private PgDetailUtils util;
	
	@Autowired
	private RestTemplate restTemplate;
	
	 
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	private PgDetailRepository repository;
	
	@Autowired
	private EncryptionDecryptionUtil encryptionDecryptionUtil;
//	@Autowired
//	private BillingslabQueryBuilder queryBuilder;
//	
//	@Autowired
//	private BillingSlabConfigs billingSlabConfigs;
	
	/**
	 * Service layer for creating billing slabs
	 * @param billingSlabReq
	 * @return
	 */
	public PgDetailResponse createPgDetails(PgDetailRequest pgDetailRequest) {
		PgDetail pg =encryptionDecryptionUtil.encryptObject(pgDetailRequest.getPgDetail().get(0) , "PgDetail",PgDetail.class);
		System.out.println("Encyription "+pg);
		List<PgDetail> pgList = new ArrayList<PgDetail>();
		pgList.add(pg);
		User userInfo = pgDetailRequest.getRequestInfo().getUserInfo();
		List<PgDetail> pgDetailListResponse = repository.createPgDetails(userInfo,pgList);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(pgDetailRequest.getRequestInfo(), true);
		return new PgDetailResponse(responseInfo, pgDetailListResponse);
		
	}
	
   public PgDetailResponse getPgDetails(PgDetailRequest pgDetailRequest) {
	   List<PgDetail> pgDetailListResponse = repository.getPgDetails(pgDetailRequest.getPgDetail());
	   ArrayList<PgDetail> decyrptList = new ArrayList<PgDetail>();
	   if(pgDetailListResponse!=null && pgDetailListResponse.size()>0)
	   for (PgDetail pgDetail : pgDetailListResponse) {
		   decyrptList.add(encryptionDecryptionUtil.decryptObject(pgDetail, "PgDetail", PgDetail.class, pgDetailRequest.getRequestInfo()));
	   }
	   ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(pgDetailRequest.getRequestInfo(), true);
	   return new PgDetailResponse(responseInfo, decyrptList);
   }
}
