alter table test_plan add project_id varchar(50) null comment '测试计划所属项目';
ALTER TABLE api_test_case MODIFY COLUMN name varchar(255) NOT NULL COMMENT 'Test case name';

DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE planId VARCHAR(64);
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT id
                                FROM test_plan
                                WHERE project_id IS NULL);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO planId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        select count(1) as s, project_id
        into @num, @projectId
        from test_plan_test_case
                 join test_case on case_id = test_case.id
        where plan_id = planId
        group by project_id
        order by s desc
        limit 1;

        IF @projectId = ''
        THEN
            select test_plan_project.project_id
            into @projectId
            from test_plan
                     join test_plan_project on test_plan.id = test_plan_project.test_plan_id
            where test_plan.id = planId
            limit 1;
        END IF;

        UPDATE test_plan SET test_plan.project_id = @projectId WHERE test_plan.id = planId;
        DELETE FROM test_plan_project
        WHERE test_plan_project.project_id = @projectId
          AND test_plan_project.test_plan_id = planId;

        SET @projectId = '';
        SET done = 0;

    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;