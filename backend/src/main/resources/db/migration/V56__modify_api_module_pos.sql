set @rownum=0;
update api_module set pos=(select @rownum := @rownum +1) where pos is null;
update api_scenario_module set pos=(select @rownum := @rownum +1) where pos is null;