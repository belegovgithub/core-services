package org.egov.pg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;


@Configuration
@Data
public class PgDetailConfigs {

	@Value("${kafka.topics.save.service}")
	public String persisterSaveTopic;
	
	@Value("${kafka.topics.update.service}")
	public String persisterUpdateTopic;
	
    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;
}
