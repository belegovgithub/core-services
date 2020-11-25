CREATE TABLE eg_pgdetail_audit
(
    id serial NOT NULL,
    tenantid character varying(256),
    merchantid character varying(256),
    merchantSecretKey character varying(256),
    merchantUserName character varying(256),
    merchantPassword character varying(256),
    merchantServiceId character varying(256),
    createddate timestamp,
    lastmodifieddate timestamp,
    createdby character(36),
    lastmodifiedby character(36) 
);