package org.egov.pg.web.contract.factory;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;

public class ResponseInfoFactory {
	public ResponseInfo createResponseInfoFromRequestInfo(final RequestInfo requestInfo, final Boolean success) {
		
		final String apiId = requestInfo != null ? requestInfo.getApiId() : "";
		final String ver = requestInfo != null ? requestInfo.getVer() : "";
		Long ts = null;
		if (requestInfo != null)
			ts = requestInfo.getTs();
		final String resMsgId = "uief87324"; // FIXME : Hard-coded
		final String msgId = requestInfo != null ? requestInfo.getMsgId() : "";


		return ResponseInfo.builder().apiId(apiId).ver(ver).ts(ts).resMsgId(resMsgId).msgId(msgId).resMsgId(resMsgId)
				.build();
	}
}
