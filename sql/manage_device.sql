-- Alter manage_device table to make all fields nullable except id and device_sn

ALTER TABLE `dji_connect_manage_device`
  MODIFY COLUMN `device_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'model of the device. This parameter corresponds to the device name in the device dictionary table.',
  MODIFY COLUMN `user_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'The account used when the device was bound.',
  MODIFY COLUMN `nickname` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'custom name of the device',
  MODIFY COLUMN `workspace_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'The workspace to which the current device belongs.',
  MODIFY COLUMN `device_type` int DEFAULT -1 COMMENT 'This parameter corresponds to the device type in the device dictionary table.',
  MODIFY COLUMN `sub_type` int DEFAULT -1 COMMENT 'This parameter corresponds to the sub type in the device dictionary table.',
  MODIFY COLUMN `domain` int DEFAULT -1 COMMENT 'This parameter corresponds to the domain in the device dictionary table.',
  MODIFY COLUMN `firmware_version` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'firmware version of the device',
  MODIFY COLUMN `compatible_status` tinyint(1) DEFAULT 1 COMMENT '1: consistent; 0: inconsistent; Whether the firmware versions are consistent.',
  MODIFY COLUMN `version` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'version of the protocol. This field is currently not useful.',
  MODIFY COLUMN `device_index` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'Control of the drone, A control or B control.',
  MODIFY COLUMN `child_sn` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'The device controlled by the gateway.',
  MODIFY COLUMN `create_time` bigint DEFAULT NULL,
  MODIFY COLUMN `update_time` bigint DEFAULT NULL,
  MODIFY COLUMN `bound_time` bigint DEFAULT NULL COMMENT 'The time when the device is bound to the workspace.',
  MODIFY COLUMN `bound_status` tinyint(1) DEFAULT 0 COMMENT 'The status when the device is bound to the workspace. 1: bound; 0: not bound;',
  MODIFY COLUMN `login_time` bigint DEFAULT NULL COMMENT 'The time of the last device login.',
  MODIFY COLUMN `device_desc` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '',
  MODIFY COLUMN `url_normal` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'The icon displayed on the remote control.',
  MODIFY COLUMN `url_select` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'The icon displayed on the remote control when it is selected.';
