alter table test_case add review_status varchar(25) null;

update test_case set review_status = 'Prepare' where review_status is null;