select * from agent_task t
;
select c.op, c.entity_n, ifnull(c.via_commit, c.prev_string), ct.leftword, ct.rightword, ct.cnt, ct.space from agent_change c
left join plm_context ct on c.via_commit is not null and c.entity_n = ct.n
-- where c.task = 83
order by c.via_commit is null, c.entity_n
;
select * from llm_word w
order by w.updated_date desc