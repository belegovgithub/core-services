package org.egov.pg.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.pg.web.contract.PgDetail;
import org.springframework.jdbc.core.RowMapper;

public class PgDetailRowMapper implements RowMapper<PgDetail> {
	@Override
	public PgDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
		final PgDetail pgDetail = PgDetail.builder().id(rs.getLong("id")).tenantId(rs.getString("tenantid")).merchantId(rs.getString("merchantId")).merchantSecretKey(rs.getString("merchantSecretKey")).merchantUserName(rs.getString("merchantUserName")).merchantPassword(rs.getString("merchantPassword"))
				.merchantServiceId(rs.getString("merchantServiceId"))
				.createdBy(rs.getString("createdBy"))
				.createdDate(rs.getDate("createdDate")).lastModifiedBy(rs.getString("lastModifiedBy"))
				.lastModifiedDate(rs.getDate("lastModifiedDate")).build();
		return pgDetail;
	}
}
