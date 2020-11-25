
package org.egov.pg.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pg.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.utils.PgDetailUtils;
import org.egov.pg.utils.ResponseInfoFactory;
import org.egov.pg.validator.PgValidator;
import org.egov.pg.web.contract.PgDetail;
import org.egov.pg.web.contract.PgDetailRequest;
import org.egov.pg.web.contract.PgDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
 
	@Autowired
	private PgValidator pgValidator;
 
	
	/**
	 * Service layer for creating billing slabs
	 * @param billingSlabReq
	 * @return
	 */
	public PgDetailResponse createPgDetails(PgDetailRequest pgDetailRequest) {
		PgDetail pg = validateAndEncryptPgDetails(pgDetailRequest);
		List<PgDetail> pgList = new ArrayList<PgDetail>();
		final Long newId = repository.getNextSequence();
		pg.setId(newId);
		pg.setCreatedDate(new Date());
		User userInfo = pgDetailRequest.getRequestInfo().getUserInfo();
		pg.setCreatedBy(userInfo.getUuid());
		pgList.add(pg);
		repository.createPgDetails(pgList);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(pgDetailRequest.getRequestInfo(), true);
		return new PgDetailResponse(responseInfo, pgList);
		
	}
	
	public PgDetail validateAndEncryptPgDetails(PgDetailRequest pgDetailRequest){
		pgValidator.validateCreate(pgDetailRequest);
		PgDetail pg =encryptionDecryptionUtil.encryptObject(pgDetailRequest.getPgDetail().get(0) , "PgDetail",PgDetail.class);
		return pg;
	}
	
	public PgDetailResponse updatePgDetails(PgDetailRequest pgDetailRequest) {
		List<PgDetail> pgList = new ArrayList<PgDetail>();
		PgDetailResponse existingPgDetails = getPgDetails(pgDetailRequest);
		if(!(existingPgDetails == null)) {
			PgDetail existingPgDetail = existingPgDetails.getPgDetail().get(0);
			existingPgDetail.setMerchantId(existingPgDetail.getMerchantId());
			existingPgDetail.setMerchantSecretKey(existingPgDetail.getMerchantSecretKey());
			existingPgDetail.setMerchantUserName(existingPgDetail.getMerchantUserName());
			existingPgDetail.setMerchantPassword(existingPgDetail.getMerchantPassword());
			existingPgDetail.setMerchantServiceId(existingPgDetail.getMerchantServiceId());
			existingPgDetail.setCreatedDate(existingPgDetail.getCreatedDate());
			existingPgDetail.setLastModifiedDate(new Date());
			existingPgDetail.setLastModifiedBy(pgDetailRequest.getRequestInfo().getUserInfo().getUuid());
			pgDetailRequest.getPgDetail().set(0, existingPgDetail);
			PgDetail pgDetail = validateAndEncryptPgDetails(pgDetailRequest);
			pgList.add(pgDetail);
			repository.update(pgList);
			ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(pgDetailRequest.getRequestInfo(), true);
			return new PgDetailResponse(responseInfo, pgList);
		}
		else {
			return null;
		}
		
	}
	
   public PgDetailResponse getPgDetails(PgDetailRequest pgDetailRequest) {
	   pgValidator.validateForSearch(pgDetailRequest);
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
