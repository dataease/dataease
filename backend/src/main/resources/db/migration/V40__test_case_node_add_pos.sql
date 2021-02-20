alter table test_case_node add pos double null;

DROP PROCEDURE IF EXISTS pos_cursor;
DELIMITER //
CREATE PROCEDURE pos_cursor()
BEGIN
    DECLARE projectId VARCHAR(64);
    DECLARE nodeId VARCHAR(64);
    DECLARE pos DOUBLE;
    DECLARE level INT;
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT DISTINCT project_id
                                FROM test_case_node
                                WHERE pos IS NULL);
    DECLARE cursor2 CURSOR FOR (select id
                                from test_case_node
                                where project_id = projectId
                                  and test_case_node.level = level
                                order by create_time);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO projectId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        SET level = 1;
        select max(test_case_node.level) into @max_level from test_case_node where project_id = projectId;
        while level <= @max_level
            do
                set pos = 65536;
                OPEN cursor2;
                inner_loop:
                LOOP
                    FETCH cursor2 INTO nodeId;
                    IF done
                    THEN
                        LEAVE inner_loop;
                    END IF;
                    UPDATE test_case_node
                    SET test_case_node.pos = pos
                    WHERE id = nodeId;
                    SET pos = pos + 65536;
                END LOOP;
                SET done = 0;
                CLOSE cursor2;
                set level = level + 1;
            end while;

    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL pos_cursor();
DROP PROCEDURE IF EXISTS pos_cursor;