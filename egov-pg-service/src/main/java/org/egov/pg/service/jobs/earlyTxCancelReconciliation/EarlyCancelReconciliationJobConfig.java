package org.egov.pg.service.jobs.earlyTxCancelReconciliation;

import org.egov.pg.config.AppProperties;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * Scheduled to run frequent interval, configurable
 */
@Configuration
public class EarlyCancelReconciliationJobConfig {

    @Autowired
    private AppProperties appProperties;

    @Bean
    JobDetailFactoryBean earlyCancelReconciliationJob() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(EarlyCancelReconciliationJob.class);
        jobDetailFactory.setGroup("status-update");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    @Autowired
    CronTriggerFactoryBean earlyCancelReconciliationTrigger(JobDetail earlyCancelReconciliationJob) {
        int runEvery = appProperties.getEarlyTxCancelReconcileJobRunInterval();
        Integer runEveryMinutes, runEveryHours;
        runEveryHours = runEvery / 60;
        runEveryMinutes = runEvery % 60;


        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(earlyCancelReconciliationJob);
//        cronTriggerFactoryBean.setCronExpression("0 0/" + appProperties.getReconciliationTimeout().toString() + " * * * ?");
        cronTriggerFactoryBean.setCronExpression("0 " + runEveryHours + "/" + runEveryMinutes + " * * * ?");
        cronTriggerFactoryBean.setGroup("status-update");
        return cronTriggerFactoryBean;
    }


}
