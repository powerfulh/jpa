select * from agent_task t
;
select
c.op, c.entity_n, ifnull(c.via_commit, c.prev_string) via, ct.leftword, ct.rightword,
(select w.word from llm_word w where w.n = ct.leftword) lw, (select w.word from llm_word w where w.n = ct.rightword) rw, ct.cnt, ct.space
from agent_change c
left join plm_context ct on c.via_commit is not null and c.entity_n = ct.n
where c.task = (select min(t.n) from agent_task t)
order by c.via_commit is null, c.entity_n
;
select * from llm_word w
order by w.updated_date desc