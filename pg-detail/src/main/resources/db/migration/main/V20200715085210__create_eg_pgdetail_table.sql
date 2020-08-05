CREATE TABLE eg_pgdetail
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

ALTER TABLE eg_pgdetail ADD CONSTRAINT eg_pgdetail_tenantid_key UNIQUE (tenantid); 
ALTER TABLE eg_pgdetail ADD CONSTRAINT eg_pgdetail_pkey PRIMARY KEY (id);