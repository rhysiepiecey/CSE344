SELECT COUNT(*) 
FROM (
   select C.name as name, SUM(F.departure_delay) as delay
from FLIGHTS as F, CARRIERS as C
where F.carrier_id = C.cid
group by C.name
);