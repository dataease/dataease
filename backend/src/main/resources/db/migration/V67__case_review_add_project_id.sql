alter table test_case_review add project_id varchar(50) null comment '用例评审所属项目';

DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE reviewId VARCHAR(64);
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT id
                                FROM test_case_review
                                WHERE project_id IS NULL);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO reviewId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        select count(1) as s, project_id
        into @num, @projectId
        from test_case_review_test_case
                 join test_case on case_id = test_case.id
        where review_id = reviewId
        group by project_id
        order by s desc
        limit 1;

        IF @projectId = ''
        THEN
            select test_case_review_project.project_id
            into @projectId
            from test_case_review
                     join test_case_review_project on test_case_review.id = test_case_review_project.review_id
            where test_case_review.id = reviewId
            limit 1;
        END IF;

        UPDATE test_case_review SET test_case_review.project_id = @projectId WHERE test_case_review.id = reviewId;
        DELETE FROM test_case_review_project
        WHERE test_case_review_project.project_id = @projectId
          AND test_case_review_project.review_id = reviewId;

        SET @projectId = '';
        SET done = 0;

    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;