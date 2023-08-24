ALTER TABLE `dataset_table` ADD COLUMN `sync_status` VARCHAR(45) NULL AFTER `create_time`;
ALTER TABLE `dataset_table` ADD COLUMN `qrtz_instance` VARCHAR(1024) NULL AFTER `create_time`;


BEGIN;
INSERT INTO `sys_auth` VALUES ('05e5440f-b9c1-4998-bb7e-cabdb293183f', '25', 'menu', '2', 'role', 1622713311975, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('19569764-ad2e-4d7b-b575-508913495ccf', '8', 'menu', '2', 'role', 1622713307681, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('4541d582-b532-47b1-9e64-b4937c7b614f', '30', 'menu', '2', 'role', 1622713299688, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('45dacd42-e9a4-4ce5-a03c-61c8850cf5b1', '10', 'menu', '2', 'role', 1622713304841, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('8b1e1e02-b6c2-49dc-9118-10ab31f78b46', '34', 'menu', '2', 'role', 1623142236719, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('a11f5705-887d-403b-9a9d-d3a1317ed91f', '24', 'menu', '2', 'role', 1622713311583, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('c318d302-457d-43d8-bb01-96a7cbbf6cb7', '27', 'menu', '2', 'role', 1622713313044, NULL, 'admin', NULL);
INSERT INTO `sys_auth` VALUES ('c3349d14-1433-41e6-9ddc-ba947c3b523c', '26', 'menu', '2', 'role', 1622713312606, NULL, 'admin', NULL);
COMMIT;

BEGIN;
INSERT INTO `sys_auth_detail` VALUES ('980c14e1-c836-11eb-bbcc-00163e0c8d3f', '8b1e1e02-b6c2-49dc-9118-10ab31f78b46', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1623142236000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('980c1674-c836-11eb-bbcc-00163e0c8d3f', '8b1e1e02-b6c2-49dc-9118-10ab31f78b46', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1623142236000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('e59001d3-c44f-11eb-bbcc-00163e0c8d3f', '4541d582-b532-47b1-9e64-b4937c7b614f', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713299000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('e59003a0-c44f-11eb-bbcc-00163e0c8d3f', '4541d582-b532-47b1-9e64-b4937c7b614f', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713299000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('e8a4bdbf-c44f-11eb-bbcc-00163e0c8d3f', '45dacd42-e9a4-4ce5-a03c-61c8850cf5b1', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713304000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('e8a4bf70-c44f-11eb-bbcc-00163e0c8d3f', '45dacd42-e9a4-4ce5-a03c-61c8850cf5b1', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713304000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ea536049-c44f-11eb-bbcc-00163e0c8d3f', '19569764-ad2e-4d7b-b575-508913495ccf', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713307000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ea5361d6-c44f-11eb-bbcc-00163e0c8d3f', '19569764-ad2e-4d7b-b575-508913495ccf', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713307000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('eca67ad0-c44f-11eb-bbcc-00163e0c8d3f', 'a11f5705-887d-403b-9a9d-d3a1317ed91f', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713311000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('eca67c74-c44f-11eb-bbcc-00163e0c8d3f', 'a11f5705-887d-403b-9a9d-d3a1317ed91f', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713311000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ece430b9-c44f-11eb-bbcc-00163e0c8d3f', '05e5440f-b9c1-4998-bb7e-cabdb293183f', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713312000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ece4330e-c44f-11eb-bbcc-00163e0c8d3f', '05e5440f-b9c1-4998-bb7e-cabdb293183f', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713312000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ed4328d0-c44f-11eb-bbcc-00163e0c8d3f', 'c3349d14-1433-41e6-9ddc-ba947c3b523c', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713312000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ed432abc-c44f-11eb-bbcc-00163e0c8d3f', 'c3349d14-1433-41e6-9ddc-ba947c3b523c', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713312000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ed8722a7-c44f-11eb-bbcc-00163e0c8d3f', 'c318d302-457d-43d8-bb01-96a7cbbf6cb7', 'i18n_auth_grant', 15, 0, 'grant', '基础权限-授权', 'admin', 1622713313000, NULL);
INSERT INTO `sys_auth_detail` VALUES ('ed872435-c44f-11eb-bbcc-00163e0c8d3f', 'c318d302-457d-43d8-bb01-96a7cbbf6cb7', 'i18n_auth_use', 1, 1, 'use', '基础权限-使用', 'admin', 1622713313000, NULL);
COMMIT;