package org.egov.pg.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Getter
@ToString
@Configuration
@PropertySource("classpath:application.properties")
public class AppProperties {

    private final Integer earlyReconcileJobRunInterval;
    
    private final Integer earlyTxCancelReconcileJobRunInterval;

    private final String saveTxnTopic;

    private final String updateTxnTopic;

    private final String saveTxnDumpTopic;

    private final String updateTxnDumpTopic;
    
    private final String idGenHost;

    private final String idGenPath;

    private final String idGenName;

    private final String idGenFormat;

    private final String collectionServiceHost;

    private final String collectionServiceCreatePath;

    private final String collectionServiceValidatePath;
    
    private final String paymentCreatePath;

    private final String paymentValidatePath;

    private final String bankAccountHost;

    private final String bankAccountPath;
    
    private final String pgDetailHost;

	private final String pgDetailPath;
	
    private String userHost;

    private String userSearchEndpoint;
    
    private String tenantId;

	private String dailyReconcileUserName;

	private String earlyReconcileUserName;
	
    private String demandHost;
    
    private String demandSearchEndpoint;

    @Autowired
    public AppProperties(Environment environment){
        this.earlyReconcileJobRunInterval = Integer.valueOf(environment.getRequiredProperty("pg.earlyReconcileJobRunInterval.mins"));
        this.earlyTxCancelReconcileJobRunInterval = Integer.valueOf(environment.getRequiredProperty("pg.txCancelJobRunInterval.mins"));
        this.saveTxnTopic = environment.getRequiredProperty("persister.save.pg.txns");
        this.updateTxnTopic = environment.getRequiredProperty("persister.update.pg.txns");
        this.saveTxnDumpTopic = environment.getRequiredProperty("persister.save.pg.txnsDump");
        this.updateTxnDumpTopic = environment.getRequiredProperty("persister.update.pg.txnsDump");
        this.idGenHost = environment.getRequiredProperty("egov.idgen.host");
        this.idGenPath = environment.getRequiredProperty("egov.idgen.path");
        this.idGenName = environment.getRequiredProperty("egov.idgen.ack.name");
        this.idGenFormat = environment.getRequiredProperty("egov.idgen.ack.format");
        this.collectionServiceHost = environment.getRequiredProperty("egov.collectionservice.host");
        this.collectionServiceCreatePath = environment.getRequiredProperty("egov.collectionservice.create.path");
        this.collectionServiceValidatePath = environment.getRequiredProperty("egov.collectionservice.validate.path");
        this.bankAccountHost = environment.getRequiredProperty("egov.bankaccountservice.host");
        this.bankAccountPath = environment.getRequiredProperty("egov.bankaccountservice.path");
        this.paymentCreatePath = environment.getRequiredProperty("egov.collectionservice.payment.create.path");
        this.paymentValidatePath = environment.getRequiredProperty("egov.collectionservice.payment.validate.path");
        this.pgDetailHost = environment.getRequiredProperty("egov.pgdetail.host");
        this.pgDetailPath = environment.getRequiredProperty("egov.pgdetail.path");
        this.userHost = environment.getRequiredProperty("egov.user.host");
        this.userSearchEndpoint = environment.getRequiredProperty("egov.user.search.endpoint");
        this.tenantId=environment.getRequiredProperty("egov.tenantid");
        this.dailyReconcileUserName =environment.getRequiredProperty("pg.dailyreconcileusername");
        this.earlyReconcileUserName =environment.getRequiredProperty("pg.earlyreconcileusername");
        this.demandSearchEndpoint =environment.getRequiredProperty("egov.demand.search.endpoint");
        this.demandHost =environment.getRequiredProperty("egov.demand.host");
    }

}
