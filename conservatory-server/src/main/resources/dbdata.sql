INSERT INTO ENVIRONMENT (ID, NAME, CREATED_AT, COMMENT, STATUS) VALUES (1, 'Live', SYSDATE, 'Live environment', 1);
INSERT INTO ENVIRONMENT (ID, NAME, CREATED_AT, COMMENT, STATUS) VALUES (2, 'Staging', SYSDATE, 'Staging environment', 1);
INSERT INTO ENVIRONMENT (ID, NAME, CREATED_AT, COMMENT, STATUS) VALUES (3, 'Test', SYSDATE, 'Test environment', 1);

INSERT INTO APPLICATION (ID, NAME, CREATED_AT, COMMENT, STATUS) VALUES (1, 'Example Application', SYSDATE, 'Example Application is shipped with Conservatory', 1);

INSERT INTO CONFIG_RESOURCE (ID, NAME, CREATED_AT, COMMENT, ID_APPLICATION) VALUES (1, 'Example Properties', SYSDATE, 'Example Properties is shipped with Conservatory', 1);

INSERT INTO CONFIG_TARGET (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_RESOURCE, ID_ENVIRONMENT) VALUES (1, 'Example Properties on Test', SYSDATE, 'Shipped with Conservatory', 1, 1);
INSERT INTO CONFIG_TARGET (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_RESOURCE, ID_ENVIRONMENT) VALUES (2, 'Example Properties on Staging', SYSDATE, 'Sample Application', 1, 2);
INSERT INTO CONFIG_TARGET (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_RESOURCE, ID_ENVIRONMENT) VALUES (3, 'Example Properties on Live', SYSDATE, 'Sample Application', 1, 3);

INSERT INTO CONFIG_PROPERTY (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_TARGET, PROP_TYPE, PROP_VALUE) VALUES (1, 'string.property', SYSDATE, 'Example String property is shipped with Conservatory', 1, 'STRING','Some value');
INSERT INTO CONFIG_PROPERTY (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_TARGET, PROP_TYPE, PROP_VALUE) VALUES (2, 'integer.property', SYSDATE, 'Example Integer property is shipped with Conservatory', 1, 'INTEGER','123456789');
INSERT INTO CONFIG_PROPERTY (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_TARGET, PROP_TYPE, PROP_VALUE) VALUES (3, 'url.property', SYSDATE, 'Example URL property is shipped with Conservatory', 1, 'URL','http://www.disney.com:8080/whatever');
INSERT INTO CONFIG_PROPERTY (ID, NAME, CREATED_AT, COMMENT, ID_CONFIG_TARGET, PROP_TYPE, PROP_VALUE) VALUES (4, 'date.property', SYSDATE, 'Example Date property is shipped with Conservatory', 1, 'DATETIME','2013-10-30T16:35:55');

INSERT INTO TARGET_PROPERTY (ID_CONFIG_TARGET, ID_CONFIG_PROPERTY) VALUES (1, 1);
INSERT INTO TARGET_PROPERTY (ID_CONFIG_TARGET, ID_CONFIG_PROPERTY) VALUES (1, 2);
INSERT INTO TARGET_PROPERTY (ID_CONFIG_TARGET, ID_CONFIG_PROPERTY) VALUES (1, 3);
INSERT INTO TARGET_PROPERTY (ID_CONFIG_TARGET, ID_CONFIG_PROPERTY) VALUES (1, 4);

--CONFIG_DOWNLOAD