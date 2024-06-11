.header on 
.mode csv
PRAGMA foreign_keys=ON;


-- 1.Create table CARRIERS
CREATE TABLE CARRIERS (cid varchar(7) PRIMARY KEY, 
		       name varchar(83));

-- 2.Create table MONTHS
CREATE TABLE MONTHS (mid int PRIMARY KEY, 
                     month varchar(9));

-- 3,Create table WEEKDAYS
CREATE TABLE WEEKDAYS (did int PRIMARY KEY, 
                       day_of_week varchar(9));

-- 4.Create table FLIGHTS
CREATE TABLE FLIGHTS (fid int PRIMARY KEY, 
                      month_id int,        -- 1-12
                      day_of_month int,    -- 1-31 
                      day_of_week_id int,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
                      carrier_id varchar(7), 
                      flight_num int,
                      origin_city varchar(34), 
                      origin_state varchar(47), 
                      dest_city varchar(34), 
                      dest_state varchar(46), 
                      departure_delay int, -- in mins
                      taxi_out int,        -- in mins
                      arrival_delay int,   -- in mins
                      canceled int,        -- 1 means canceled
                      actual_time int,     -- in mins
                      distance int,        -- in miles
                      capacity int, 
                      price int,            -- in $     
		      FOREIGN KEY(carrier_id) REFERENCES CARRIERS(cid)
                      FOREIGN KEY(month_id) REFERENCES MONTHS(mid)
		      FOREIGN KEY(day_of_week_id) REFERENCES WEEKDAYS(did)      
                      );
