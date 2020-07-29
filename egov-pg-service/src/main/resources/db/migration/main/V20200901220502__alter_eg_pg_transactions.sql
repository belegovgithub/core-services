ALTER TABLE eg_pg_transactions DROP COLUMN "bankTransactionNo"; 
ALTER TABLE eg_pg_transactions ADD COLUMN "bank_ransaction_no" CHARACTER VARYING(250) NULL;