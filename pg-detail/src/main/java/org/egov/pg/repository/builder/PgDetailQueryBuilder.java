package org.egov.pg.repository.builder;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PgDetailQueryBuilder {
	
	public static final String SELECT_NEXT_SEQUENCE_PGDETAIL = "select nextval('eg_pgdetail_id_seq')";
	
	public static  String getInsertUserQuery() {
		
		return "INSERT into eg_pgdetail(id, tenantid, merchantid, merchantSecretKey, merchantUserName, merchantPassword, merchantServiceId ,  createddate,lastmodifieddate,createdby,lastmodifiedby) values(:id,:tenantid, :merchantid, :merchantSecretKey, :merchantUserName, :merchantPassword, :merchantServiceId, :createddate,:lastmodifieddate,:createdby,:lastmodifiedby)";
	   
	}
	
	public static String updateUserQuery() {
		return "UPDATE eg_pgdetail set merchantid=:merchantid,merchantSecretKey=:merchantSecretKey, merchantUserName=:merchantUserName, merchantPassword=:merchantPassword, merchantServiceId=:merchantServiceId,lastmodifieddate=:lastmodifieddate,lastmodifiedby=:lastmodifiedby where tenantid =:tenantid";
	}
	public static String insertDataToAuditTable() {
		return "INSERT INTO eg_pgdetail_audit SELECT * FROM eg_pgdetail WHERE id =:id";
		
	}
	
	public static final String GET_PGDETAIL_BY_TENANTID = "SELECT * FROM eg_pgdetail where tenantid =:tenantId";
	
	
}
