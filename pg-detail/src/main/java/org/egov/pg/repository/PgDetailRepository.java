package org.egov.pg.repository;


import static org.egov.pg.repository.builder.PgDetailQueryBuilder.SELECT_NEXT_SEQUENCE_PGDETAIL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.User;
import org.egov.pg.repository.builder.PgDetailQueryBuilder;
import org.egov.pg.repository.rowmapper.PgDetailRowMapper;
import org.egov.pg.web.contract.PgDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
@Repository
@Slf4j
public class PgDetailRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
 
	
	@Autowired
	private PgDetailQueryBuilder pgDetailQueryBuilder;
	
 
	
	public Long getNextSequence() {
		return jdbcTemplate.queryForObject(SELECT_NEXT_SEQUENCE_PGDETAIL, Long.class);
	}

	
	
	public List<PgDetail> getPgDetails(List<PgDetail>pgDetailList) {
		final Map<String, Object> Map = new HashMap<String, Object>();
		Map.put("tenantId", pgDetailList.get(0).getTenantId());
		List<PgDetail> pgDetailListResponse = namedParameterJdbcTemplate.query(PgDetailQueryBuilder.GET_PGDETAIL_BY_TENANTID, Map,new PgDetailRowMapper());
		return pgDetailListResponse;
		
	
		
	}
	
	
	public void createPgDetails(List<PgDetail> pgDetails ) {
		PgDetail pgDetail = pgDetails.get(0);
		pgDetail.setCreatedDate(new Date());
		saveOrUpdate(pgDetail,pgDetailQueryBuilder.getInsertUserQuery());
		
	}
	
	public void saveOrUpdate(PgDetail pgDetail, String query) {
		if(pgDetail!=null) {
			Map<String,Object>pgDetilInputs = new HashMap<String, Object>();
			pgDetilInputs.put("id", pgDetail.getId());
			pgDetilInputs.put("tenantid", pgDetail.getTenantId());
			pgDetilInputs.put("merchantid", pgDetail.getMerchantId());
			pgDetilInputs.put("merchantSecretKey", pgDetail.getMerchantSecretKey());
			pgDetilInputs.put("merchantUserName", pgDetail.getMerchantUserName());
			pgDetilInputs.put("merchantPassword", pgDetail.getMerchantPassword());
			pgDetilInputs.put("merchantServiceId", pgDetail.getMerchantServiceId());
			pgDetilInputs.put("createddate", pgDetail.getCreatedDate());
			pgDetilInputs.put("lastmodifieddate", pgDetail.getLastModifiedDate());
			pgDetilInputs.put("createdby", pgDetail.getCreatedBy());
			pgDetilInputs.put("lastmodifiedby", pgDetail.getLastModifiedBy());
			int result = namedParameterJdbcTemplate.update(query, pgDetilInputs);
			insertDataToAuditTable(pgDetail);
	 }
	}
	
	public void update(List<PgDetail> pgDetails) {
		PgDetail pgDetail = pgDetails.get(0);
		saveOrUpdate(pgDetail, pgDetailQueryBuilder.updateUserQuery());
	}
	
	public int insertDataToAuditTable(PgDetail pgDetail) {
		final Map<String, Object> Map = new HashMap<String, Object>();
		Map.put("id", pgDetail.getId());
		int result = namedParameterJdbcTemplate.update(PgDetailQueryBuilder.insertDataToAuditTable(), Map);
		return result;
		
	}
	


}