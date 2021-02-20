alter table api_definition add num int null comment 'api definition ID';
alter table api_test_case add num int null comment 'api test case ID';
alter table api_scenario add num int null comment 'api scenario ID';

DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE projectId VARCHAR(64);
    DECLARE definitionId VARCHAR(64);
    DECLARE apiTestCaseId VARCHAR(64);
    DECLARE num1 INT;
    DECLARE num2 INT;
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT DISTINCT project_id
                                FROM api_definition
                                WHERE num IS NULL);
    DECLARE cursor2 CURSOR FOR (SELECT id
                                FROM api_definition
                                WHERE project_id = projectId
                                ORDER BY create_time);
    DECLARE cursor3 CURSOR FOR (SELECT id
                                FROM api_test_case
                                WHERE api_definition_id = definitionId
                                ORDER BY create_time);


    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO projectId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        SET num1 = 100001;
        OPEN cursor2;
        inner_loop:
        LOOP
            FETCH cursor2 INTO definitionId;
            IF done
            THEN
                LEAVE inner_loop;
            END IF;
            UPDATE api_definition
            SET num = num1
            WHERE id = definitionId;
            SET num2 = num1 * 1000 + 1;
            OPEN cursor3;
            inner_loop2:
            LOOP
                FETCH cursor3 INTO apiTestCaseId;
                IF done
                THEN
                    LEAVE inner_loop2;
                END IF;
                UPDATE api_test_case
                SET num = num2
                WHERE id = apiTestCaseId;
                SET num2 = num2 + 1;
            END LOOP;
            SET done = 0;
            CLOSE cursor3;
            SET num1 = num1 + 1;
        END LOOP;
        SET done = 0;
        CLOSE cursor2;
    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;


DROP PROCEDURE IF EXISTS test_cursor1;
DELIMITER //
CREATE PROCEDURE test_cursor1()
BEGIN
    DECLARE projectId VARCHAR(64);
    DECLARE scenarioId VARCHAR(64);
    DECLARE num INT;
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT DISTINCT project_id
                                FROM api_scenario
                                WHERE num IS NULL);
    DECLARE cursor2 CURSOR FOR (SELECT id
                                FROM api_scenario
                                WHERE project_id = projectId
                                ORDER BY create_time);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO projectId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        SET num = 100001;
        OPEN cursor2;
        inner_loop:
        LOOP
            FETCH cursor2 INTO scenarioId;
            IF done
            THEN
                LEAVE inner_loop;
            END IF;
            UPDATE api_scenario
            SET num = num
            WHERE id = scenarioId;
            SET num = num + 1;
        END LOOP;
        SET done = 0;
        CLOSE cursor2;
    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL test_cursor1();
DROP PROCEDURE IF EXISTS test_cursor1;