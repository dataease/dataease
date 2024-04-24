ALTER TABLE `xpack_share`
    ADD COLUMN `auto_pwd` tinyint(1) NOT NULL DEFAULT 1 COMMENT '自动生成密码' AFTER `type`;

ALTER TABLE `data_visualization_info`
    ADD COLUMN `version` int NULL DEFAULT 3 COMMENT '可视化资源版本';
update data_visualization_info
set version = 2;

ALTER TABLE `visualization_template`
    ADD COLUMN `version` int NULL DEFAULT 3 COMMENT '使用资源的版本';
update visualization_template
set version = 2;

UPDATE `area`
set `id`   = '156330113',
    `name` = '临平区'
where `id` = '156330103';
UPDATE `area`
set `id`   = '156330114',
    `name` = '钱塘区'
where `id` = '156330104';

update
    core_chart_view as a,
    core_chart_view as b
set
    a.x_axis = b.x_axis_ext,
    a.x_axis_ext = b.x_axis
where a.id = b.id and a.type = 'table-pivot';
