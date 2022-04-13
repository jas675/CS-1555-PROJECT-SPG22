--Helper functions--
DROP FUNCTION IF EXISTS single_route_search(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS multi_route_search(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS line_segment(depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS segments(depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS route_time(route_int integer, train_speed integer, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS route_cost(route_int integer, train_price integer, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS route_num_stations(route_int integer, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS route_num_stops(route_int integer, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS order_in_route(route_int integer, station_int integer);
DROP FUNCTION IF EXISTS order_in_line(rail_int integer, station_int integer);
DROP FUNCTION IF EXISTS first_station(route_int integer);
DROP FUNCTION IF EXISTS train_cost(train_int integer);
DROP FUNCTION IF EXISTS train_speed(train_int integer);

DROP FUNCTION IF EXISTS single_route_num_stations(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS single_route_num_stops(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS single_route_time(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS single_route_cost(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS multi_route_num_stations(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS multi_route_num_stops(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS multi_route_cost(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS multi_route_time(day_param varchar, depart_int integer, arrival_int integer);
DROP FUNCTION IF EXISTS trains_at_station(station_int integer, day_param varchar, start_time time, end_time time);
DROP VIEW IF EXISTS multi_line_routes;
DROP VIEW IF EXISTS ranked_trains;

DROP FUNCTION IF EXISTS is_multi_line_route(route_int integer);

DROP FUNCTION IF EXISTS add_passenger(first_name varchar, last_name varchar, street_name varchar, city_name varchar, zip_add varchar);
DROP FUNCTION IF EXISTS edit_passenger(id integer, first_name varchar, last_name varchar, street_name varchar, city_name varchar, zip_add varchar);
DROP FUNCTION IF EXISTS view_passenger(id integer, first_name varchar, last_name varchar, street_name varchar, city_name varchar, zip_add varchar);
DROP FUNCTION IF EXISTS update_clock(year text, month text, day text, hour text, minute text, second text);
DROP FUNCTION IF EXISTS get_day(times timestamp);
DROP FUNCTION IF EXISTS day_to_timestamp(day varchar, times time);
DROP FUNCTION IF EXISTS book_reservation(cust_id integer, tr_sch_id integer);
DROP FUNCTION IF EXISTS get_ticket(rsv_id integer);
DROP FUNCTION IF EXISTS display_sch_of_route(id integer);
DROP FUNCTION IF EXISTS get_table_with_number_of_stops();
DROP FUNCTION IF EXISTS get_routes_that_stop_xx_stations(percentage integer);
DROP FUNCTION IF EXISTS get_routes_that_does_not_stop_at_station(stop_st integer);
DROP FUNCTION IF EXISTS delete_expired_reservation() CASCADE;
DROP TRIGGER IF EXISTS reservation_cancel ON Clock CASCADE;
DROP FUNCTION IF EXISTS get_clock_timestamp() CASCADE;
DROP FUNCTION IF EXISTS change_schedule() CASCADE;
DROP TRIGGER IF EXISTS line_disruptions ON Trainschedule CASCADE;
DROP FUNCTION IF EXISTS last_station(route_int integer) CASCADE;
DROP FUNCTION IF EXISTS get_max_seats(trn_sch integer) CASCADE;
DROP FUNCTION IF EXISTS get_available_seats(trn_sch_id integer) CASCADE;
DROP FUNCTION IF EXISTS get_another_train_schedule(trn_sch_id integer) CASCADE;



--Compute the order of the given station in the given route
CREATE OR REPLACE FUNCTION order_in_route (route_int integer, station_int integer)
RETURNS TABLE(order_in_route int) AS $$
    SELECT order_in_route
    FROM station_route sr
        WHERE route_id = route_int AND station_id = station_int;
$$ LANGUAGE SQL;


--Compute the order of the given station in the given line
CREATE OR REPLACE FUNCTION order_in_line(rail_int integer, station_int integer)
RETURNS TABLE(order_in_line int) AS $$
    SELECT order_in_line
    FROM station_line sl
        WHERE sl.rail_id = rail_int AND station_id = station_int;
$$ LANGUAGE SQL;


--Compute the first station in the given route
CREATE OR REPLACE FUNCTION first_station (route_int integer)
RETURNS INTEGER AS $$
    SELECT station_id
    FROM station_route sr
        WHERE route_int = route_id AND order_in_route = 0 LIMIT 1

$$ LANGUAGE SQL;


--Get cost per km of a train
CREATE OR REPLACE FUNCTION train_cost(train_int integer)
RETURNS INTEGER AS $$
    SELECT price_per_km
    FROM train t
        WHERE t.train_id = train_int LIMIT 1;
$$ LANGUAGE SQL;


--Get speed of a train
CREATE OR REPLACE FUNCTION train_speed(train_int integer)
RETURNS INTEGER AS $$
    SELECT top_speed
    FROM train t
        WHERE t.train_id = train_int LIMIT 1;
$$ LANGUAGE SQL;


--Gets rail id and distance between two stations (which are next to each other)
CREATE OR REPLACE FUNCTION line_segment (depart_int integer, arrival_int integer)
RETURNS TABLE(rail_id int, distance int) AS $$
    SELECT destination.rail_id, arrival.distance_prev
    FROM station_line destination
        JOIN station_line arrival
            ON (destination.rail_id = arrival.rail_id)
             AND ((destination.station_id = LEAST(depart_int,arrival_int))
             AND (arrival.station_id = GREATEST(depart_int,arrival_int)))
        WHERE destination.order_in_line-1 = arrival.order_in_line OR destination.order_in_line+1 = arrival.order_in_line
$$ LANGUAGE SQL;
--SELECT * FROM line_segment(5,6);


--Returns a table of rail_ids and distances between two stations... must be on same or adjacent lines.
--Assumption: If stations aren't directly next to each other, they are at least on the same line, or directly connecting lines.
CREATE OR REPLACE FUNCTION segments (depart_int integer, arrival_int integer)
RETURNS TABLE(the_rail_id int, distance int) AS $$
     DECLARE
         line_depart RECORD;
         line_arrival RECORD;

         same_line BOOLEAN;
         line INTEGER;

         line1 INTEGER;
         line2 INTEGER;
         merge_station INTEGER;

         initial_attempt INTEGER;

         dir INTEGER = 1;
         dir2 INTEGER = 1;
    BEGIN
        initial_attempt := (SELECT rail_id FROM line_segment(depart_int,arrival_int) LIMIT 1);
       if( initial_attempt IS NOT NULL) THEN --Stations next to each other: one segment
           return query SELECT * FROM line_segment(depart_int,arrival_int);
       ELSE --Not next to each other, for some reason:

            --Check if on same line
            FOR line_depart IN SELECT rail_id FROM station_line WHERE station_id=depart_int
            LOOP
                FOR line_arrival IN SELECT rail_id FROM station_line WHERE station_id=arrival_int
                LOOP
                    IF line_depart.rail_id = line_arrival.rail_id THEN
                        same_line = True;
                        line = line_depart.rail_id;
                    END IF;
                end loop;
            END LOOP;

            IF(same_line) THEN --Same Line

                if((SELECT order_in_line FROM order_in_line(line, arrival_int) LIMIT 1) < (SELECT order_in_line FROM order_in_line(line, depart_int))) THEN
                    dir := -1;
                end if;

                return query
                    SELECT (SELECT rail_id FROM line_segment(sl.station_id,
                        (SELECT station_id
                        FROM station_line sl2
                        WHERE rail_id = line AND sl.order_in_line = sl2.order_in_line+dir))),
                    (SELECT distance_prev FROM line_segment(sl.station_id,
                        (SELECT station_id
                        FROM station_line sl2
                        WHERE rail_id = line AND sl.order_in_line = sl2.order_in_line+dir)))
                    FROM station_line sl
                    WHERE rail_id = line
                              AND (order_in_line BETWEEN
                                ((SELECT order_in_line FROM order_in_line(rail_id, depart_int) LIMIT 1)+1)
                                    AND
                                ((SELECT order_in_line FROM order_in_line(rail_id, arrival_int) LIMIT 1))
                                OR order_in_line BETWEEN
                                ((SELECT order_in_line FROM order_in_line(rail_id, arrival_int) LIMIT 1)+1)
                                    AND
                                ((SELECT order_in_line FROM order_in_line(rail_id, depart_int) LIMIT 1)))
                              ORDER BY order_in_line;

            ELSE --2 Different Lines (Assumption: the lines connect)

                 line1 := (SELECT a_rail_id
                                    FROM (SELECT rail_id a_rail_id, station_id a_station_id FROM station_line WHERE rail_id IN (SELECT rail_id FROM station_line WHERE station_id = depart_int)) sl3
                                    JOIN (SELECT rail_id b_rail_id, station_id b_station_id FROM station_line WHERE rail_id IN (SELECT rail_id FROM station_line WHERE station_id = arrival_int)) sl4
                                    ON sl3.a_station_id = sl4.b_station_id LIMIT 1);
                 line2 := (SELECT b_rail_id
                                    FROM (SELECT rail_id a_rail_id, station_id a_station_id FROM station_line WHERE rail_id IN (SELECT rail_id FROM station_line WHERE station_id = depart_int)) sl3
                                    JOIN (SELECT rail_id b_rail_id, station_id b_station_id FROM station_line WHERE rail_id IN (SELECT rail_id FROM station_line WHERE station_id = arrival_int)) sl4
                                    ON sl3.a_station_id = sl4.b_station_id LIMIT 1);
                 merge_station := (SELECT a_station_id
                                    FROM (SELECT rail_id a_rail_id, station_id a_station_id FROM station_line WHERE rail_id IN (SELECT rail_id FROM station_line WHERE station_id = depart_int)) sl3
                                    JOIN (SELECT rail_id b_rail_id, station_id b_station_id FROM station_line WHERE rail_id IN (SELECT rail_id FROM station_line WHERE station_id = arrival_int)) sl4
                                    ON sl3.a_station_id = sl4.b_station_id LIMIT 1);

                if((SELECT order_in_line FROM order_in_line(line1, depart_int) LIMIT 1) < (SELECT order_in_line FROM order_in_line(line1, merge_station))) THEN
                    dir := -1;
                end if;
                if((SELECT order_in_line FROM order_in_line(line2, arrival_int) LIMIT 1) < (SELECT order_in_line FROM order_in_line(line2, merge_station))) THEN
                    dir2 := -1;
                end if;
                 return query(
                    (SELECT (SELECT rail_id FROM line_segment(sl.station_id,
                        (SELECT station_id
                        FROM station_line sl5
                        WHERE rail_id = line1 AND sl.order_in_line = sl5.order_in_line+dir))),
                    (SELECT distance_prev FROM line_segment(sl.station_id,
                        (SELECT station_id
                        FROM station_line sl2
                        WHERE rail_id = line1 AND sl.order_in_line = sl2.order_in_line+dir)))
                    FROM station_line sl
                    WHERE rail_id = line1
                              AND (order_in_line BETWEEN
                                ((SELECT order_in_line FROM order_in_line(line1, depart_int) LIMIT 1)+1)
                                    AND
                                (SELECT order_in_line FROM order_in_line(line1, merge_station) LIMIT 1)
                                OR order_in_line BETWEEN
                                ((SELECT order_in_line FROM order_in_line(line1, merge_station) LIMIT 1)+1)
                                    AND
                                (SELECT order_in_line FROM order_in_line(line1, depart_int) LIMIT 1))
                              ORDER BY order_in_line)
                    UNION
                        (SELECT (SELECT rail_id FROM line_segment(sl.station_id,
                        (SELECT station_id
                        FROM station_line sl6
                        WHERE rail_id = line2 AND sl.order_in_line = sl6.order_in_line+dir2))),
                    (SELECT distance_prev FROM line_segment(sl.station_id,
                        (SELECT station_id
                        FROM station_line sl7
                        WHERE rail_id = line2 AND sl.order_in_line = sl7.order_in_line+dir2)))
                    FROM station_line sl
                    WHERE rail_id = line2
                              AND (order_in_line BETWEEN
                                ((SELECT order_in_line FROM order_in_line(line2, arrival_int) LIMIT 1)+1)
                                    AND
                                (SELECT order_in_line FROM order_in_line(line2, merge_station) LIMIT 1)
                                OR order_in_line BETWEEN
                                ((SELECT order_in_line FROM order_in_line(line2, merge_station) LIMIT 1)+1)
                                    AND
                                (SELECT order_in_line FROM order_in_line(line2, arrival_int) LIMIT 1))
                              ORDER BY order_in_line));

            end if;

       end if;
    END;
$$ LANGUAGE plpgsql;
--SELECT * FROM segments(3,25);


--Is the given line composed of multiple rail lines?
CREATE OR REPLACE FUNCTION is_multi_line_route (route_int integer)
RETURNS BOOLEAN AS $$
    DECLARE
        station_record RECORD;
        prev_station RECORD;

        my_rail_id INTEGER := 0;
        prev_rail_id INTEGER := 0;

        segment RECORD;
    BEGIN
        FOR station_record IN SELECT * FROM station_route WHERE route_id = route_int
                              ORDER BY route_id,order_in_route
        LOOP
            IF(prev_station IS NOT NULL) THEN
                FOR segment IN SELECT * FROM segments(prev_station.station_id, station_record.station_id)
                LOOP
                    my_rail_id := segment.the_rail_id;
                    IF(prev_rail_id=0) THEN
                        prev_rail_id := my_rail_id;
                    ELSIF(prev_rail_id != my_rail_id) THEN
                        return TRUE;
                    END IF;


                END LOOP;
            END IF;
            prev_station := station_record;

        END LOOP;
        return FALSE;
    END;
$$ LANGUAGE plpgsql;


--Get (single) routes going from depart to arrival on day
CREATE OR REPLACE FUNCTION single_route_search (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, train_id int) AS $$
    SELECT destination.route_id, t.train_id
    FROM station_route destination
        JOIN station_route arrival
            ON (destination.route_id = arrival.route_id)
                AND ((destination.station_id = depart_int AND destination.is_stop)
                AND (arrival.station_id = arrival_int AND arrival.is_stop))
        JOIN trainschedule t ON (destination.route_id = t.route_id AND t.day = day_param)
        LEFT OUTER JOIN reservation r ON t.schedule_id = r.train_sch_id
        JOIN train tr ON t.train_id = tr.train_id
        WHERE destination.order_in_route < arrival.order_in_route
        GROUP BY destination.route_id, t.train_id, num_seats HAVING count(r.reservation_id) < tr.num_seats;
$$ LANGUAGE SQL;
--SELECT * FROM single_route_search('Tuesday',35,14);


--Compute the time it takes to go down a (single) route from depart to arrival
CREATE OR REPLACE FUNCTION route_time (route_int integer, train_speed integer, depart_int integer, arrival_int integer)
RETURNS FLOAT AS $$
    DECLARE
        station_record RECORD;
        total_time FLOAT := 0;
        prev_station RECORD;

        my_rail_id INTEGER := 0;
        my_distance INTEGER := 0;

        rail_speed INTEGER := 0;

        segment RECORD;

    BEGIN
        FOR station_record IN SELECT * FROM station_route
                            WHERE route_id = route_int
                              AND order_in_route BETWEEN
                                  (SELECT order_in_route FROM order_in_route(route_int, depart_int) LIMIT 1)
                                    AND
                                  (SELECT order_in_route FROM order_in_route(route_int, arrival_int) LIMIT 1)
                              ORDER BY order_in_route
        LOOP
            IF(prev_station IS NOT NULL) THEN
                FOR segment IN SELECT * FROM segments(prev_station.station_id, station_record.station_id)
                LOOP
                    my_distance := segment.distance;
                    my_rail_id :=  segment.the_rail_id;
                    rail_speed := (SELECT speed_limit FROM railline WHERE rail_id = my_rail_id LIMIT 1);
                    IF(rail_speed > train_speed) THEN
                    total_time := total_time + (CAST(my_distance AS FLOAT)/train_speed);
                    ELSE
                        total_time := total_time + (CAST(my_distance AS FLOAT)/rail_speed);
                    end if;
                END LOOP;

            END IF;
            prev_station := station_record;

        END LOOP;
        return total_time;
    END;
$$ LANGUAGE plpgsql;
--SELECT route_time(208,150,3,7);


--Get (multi) routes going from depart to arrival on day
CREATE OR REPLACE FUNCTION multi_route_search (day_param varchar, depart_int integer, arrival_int integer)
    RETURNS TABLE(route_id int, route_id2 int, station_id int, train1 int, train2 int) AS $$
    SELECT DISTINCT sr1.route_id, sr2.route_id, sr1.station_id, tr.train_id, tr2.train_id
    FROM station_route sr1
    JOIN station_route sr2 ON sr1.station_id = sr2.station_id AND sr1.is_stop AND sr2.is_stop
    JOIN trainschedule t ON (sr1.route_id = t.route_id AND t.day = day_param)
         LEFT OUTER JOIN reservation r ON t.schedule_id = r.train_sch_id
         JOIN train tr ON t.train_id = tr.train_id
    JOIN trainschedule t2 ON (sr2.route_id = t2.route_id AND t2.day = day_param)
            LEFT OUTER JOIN reservation r2 ON t2.schedule_id = r2.train_sch_id
            JOIN train tr2 ON t2.train_id = tr2.train_id

    WHERE sr1.route_id != sr2.route_id AND (SELECT station_id
            FROM station_route sr3
            WHERE route_id = sr1.route_id AND sr3.is_stop
            AND station_id = depart_int AND sr3.order_in_route < sr1.order_in_route) IS NOT NULL
           AND  (SELECT station_id
            FROM station_route sr4
            WHERE route_id = sr2.route_id AND sr4.is_stop
            AND station_id = arrival_int AND sr4.order_in_route > sr2.order_in_route) IS NOT NULL

           AND  --Route 1 must arrive at station before route 2 departs
            t.time + (route_time(sr1.route_id, tr.top_speed, first_station(sr1.route_id), sr1.station_id)* interval '1 hour')
            <= t2.time + (route_time(sr2.route_id, tr2.top_speed, first_station(sr2.route_id), sr2.station_id)* interval '1 hour')

    GROUP BY sr1.route_id, sr2.route_id, sr1.station_id, tr.train_id, tr2.train_id, tr.num_seats, tr2.num_seats HAVING count(r.reservation_id) < tr.num_seats AND count(r2.reservation_id) < tr2.num_seats;

$$ LANGUAGE SQL;
--SELECT * FROM multi_route_search('Monday', 33, 9);


--Compute the cost it takes to go down a (single) route from depart to arrival
CREATE OR REPLACE FUNCTION route_cost (route_int integer, train_price integer, depart_int integer, arrival_int integer)
RETURNS FLOAT AS $$
    DECLARE
        station_record RECORD;
        total_cost FLOAT := 0;
        prev_station RECORD;

        my_distance INTEGER := 0;

        segment RECORD;
    BEGIN
        FOR station_record IN SELECT * FROM station_route
                            WHERE route_id = route_int
                              AND order_in_route BETWEEN
                                  (SELECT order_in_route FROM order_in_route(route_int, depart_int) LIMIT 1)
                                    AND
                                  (SELECT order_in_route FROM order_in_route(route_int, arrival_int) LIMIT 1)
                              ORDER BY order_in_route
        LOOP
            IF(prev_station IS NOT NULL) THEN
                FOR segment IN SELECT * FROM segments(prev_station.station_id, station_record.station_id)
                LOOP
                    my_distance := segment.distance;

                    total_cost := total_cost + my_distance * train_price;
                END LOOP;
            END IF;
            prev_station := station_record;

        END LOOP;
        return total_cost;
    END;
$$ LANGUAGE plpgsql;
--SELECT route_cost(208,150,3,7);


--Compute the number of stations train will pass through on this route from depart to arrival
CREATE OR REPLACE FUNCTION route_num_stations (route_int integer, depart_int integer, arrival_int integer)
RETURNS FLOAT AS $$
    DECLARE
        station_record RECORD;
        total_stations INTEGER := 0;
        prev_station RECORD;

        segment RECORD;
    BEGIN
        FOR station_record IN SELECT * FROM station_route
                            WHERE route_id = route_int
                              AND order_in_route BETWEEN
                                  (SELECT order_in_route FROM order_in_route(route_int, depart_int) LIMIT 1)
                                    AND
                                  (SELECT order_in_route FROM order_in_route(route_int, arrival_int) LIMIT 1)
                              ORDER BY order_in_route
        LOOP
            IF(prev_station IS NOT NULL) THEN
                FOR segment IN SELECT * FROM segments(prev_station.station_id, station_record.station_id)
                LOOP
                    total_stations := total_stations + 1;

                END LOOP;
            ELSE
                total_stations := total_stations +1;
            END IF;
            prev_station := station_record;

        END LOOP;
        return total_stations;
    END;
$$ LANGUAGE plpgsql;
--SELECT route_num_stations(208,3,7);


--Compute the number of stops train will pass through on this route from depart to arrival
CREATE OR REPLACE FUNCTION route_num_stops(route_int integer, depart_int integer, arrival_int integer)
RETURNS FLOAT AS $$
    DECLARE
        station_record RECORD;
        total_stops INTEGER := 0;

    BEGIN
        FOR station_record IN SELECT * FROM station_route
                            WHERE route_id = route_int
                              AND order_in_route BETWEEN
                                  (SELECT order_in_route FROM order_in_route(route_int, depart_int) LIMIT 1)
                                    AND
                                  (SELECT order_in_route FROM order_in_route(route_int, arrival_int) LIMIT 1)
                              ORDER BY order_in_route
        LOOP
            IF station_record.is_stop THEN
                total_stops := total_stops + 1;
            end if;
        END LOOP;
        return total_stops;
    END;
$$ LANGUAGE plpgsql;
--SELECT route_num_stops(208,3,7);


--Compute the order of the given station in the given route
CREATE OR REPLACE FUNCTION order_in_route (route_int integer, station_int integer)
RETURNS TABLE(order_in_route int) AS $$
    SELECT order_in_route
    FROM station_route sr
        WHERE route_id = route_int AND station_id = station_int;
$$ LANGUAGE SQL;


--Compute the order of the given station in the given line
CREATE OR REPLACE FUNCTION order_in_line(rail_int integer, station_int integer)
RETURNS TABLE(order_in_line int) AS $$
    SELECT order_in_line
    FROM station_line sl
        WHERE sl.rail_id = rail_int AND station_id = station_int;
$$ LANGUAGE SQL;


--Compute the first station in the given route
CREATE OR REPLACE FUNCTION first_station (route_int integer)
RETURNS INTEGER AS $$
    SELECT station_id
    FROM station_route sr
        WHERE route_int = route_id AND order_in_route = 0 LIMIT 1

$$ LANGUAGE SQL;


--Get cost per km of a train
CREATE OR REPLACE FUNCTION train_cost(train_int integer)
RETURNS INTEGER AS $$
    SELECT price_per_km
    FROM train t
        WHERE t.train_id = train_int LIMIT 1;
$$ LANGUAGE SQL;


--Get speed of a train
CREATE OR REPLACE FUNCTION train_speed(train_int integer)
RETURNS INTEGER AS $$
    SELECT top_speed
    FROM train t
        WHERE t.train_id = train_int LIMIT 1;
$$ LANGUAGE SQL;





--Functions for users (JDBC) to access--

--2A (1) Gets (single) routes ordered by number of stations
CREATE OR REPLACE FUNCTION single_route_num_stations (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, train_id int, num_stations int) AS $$
    SELECT s.route_id, s.train_id, route_num_stations(s.route_id, depart_int, arrival_int)
    FROM single_route_search(day_param,depart_int,arrival_int) s
    JOIN station_route sr on s.route_id=sr.route_id
    GROUP BY s.route_id, train_id
    ORDER BY route_num_stations(s.route_id, depart_int, arrival_int);
$$ LANGUAGE SQL;
--SELECT * FROM single_route_num_stations('Monday',32,13);


--2A (2) Gets (single) routes ordered by number of stops
--Assumption: we count the departure and arrival station as 'stops' (obviously easy to change, just add -1 or -2 if this isn't right)
CREATE OR REPLACE FUNCTION single_route_num_stops (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, train_id int, num_stops int) AS $$
    SELECT s.route_id,s.train_id, route_num_stops(s.route_id, depart_int, arrival_int)
    FROM single_route_search(day_param,depart_int,arrival_int) s
    JOIN station_route sr on s.route_id=sr.route_id AND sr.is_stop
    GROUP BY s.route_id,s.train_id
    ORDER BY route_num_stops(s.route_id, depart_int, arrival_int);
$$ LANGUAGE SQL;
--SELECT * FROM single_route_num_stops('Monday',32,13);


--2A (3) Gets (single) routes ordered by time
CREATE OR REPLACE FUNCTION single_route_time (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, train_id int, travel_time float) AS $$
    SELECT s.route_id,s.train_id, route_time(s.route_id, t.top_speed, depart_int, arrival_int)
    FROM single_route_search(day_param,depart_int,arrival_int) s
    JOIN train t ON s.train_id = t.train_id
    ORDER BY route_time(s.route_id, t.top_speed, depart_int, arrival_int);
$$ LANGUAGE SQL;
--SELECT * FROM single_route_time('Monday',8,3);


--2A (4) Gets (single) routes ordered by cost
CREATE OR REPLACE FUNCTION single_route_cost(day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, train_id int, cost float) AS $$
    SELECT s.route_id,s.train_id, route_cost(s.route_id, t.price_per_km, depart_int, arrival_int)
    FROM single_route_search(day_param,depart_int,arrival_int) s
    JOIN train t ON s.train_id = t.train_id
    ORDER BY route_cost(s.route_id, t.price_per_km, depart_int, arrival_int);
$$ LANGUAGE SQL;
--SELECT * FROM single_route_cost('Thursday',3,11);


--2B (1) Gets (multi) routes ordered by number of stations
CREATE OR REPLACE FUNCTION multi_route_num_stations (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, route_id2 int, transition_station int, train1 int, train2 int, num_stations int) AS $$
    SELECT s.route_id, s.route_id2, s.station_id transition_point, train1, train2, route_num_stations(s.route_id, depart_int, s.station_id)-1
                + route_num_stations(s.route_id2, s.station_id, arrival_int)
    FROM multi_route_search(day_param,depart_int,arrival_int) s

    GROUP BY s.route_id, s.route_id2, s.station_id, train1, train2
    ORDER BY (route_num_stations(s.route_id, depart_int, s.station_id)
                 + route_num_stations(s.route_id2, s.station_id, arrival_int));
$$ LANGUAGE SQL;
--SELECT * FROM multi_route_num_stations('Monday',33,5);


--2B (2) Gets (multi) routes ordered by number of stops
CREATE OR REPLACE FUNCTION multi_route_num_stops (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, route_id2 int, transition_station int, train1 int, train2 int, num_stops int) AS $$
    SELECT s.route_id, s.route_id2, s.station_id transition_point, train1, train2, route_num_stops(s.route_id, depart_int, s.station_id)-1
                + route_num_stops(s.route_id2, s.station_id, arrival_int)
    FROM multi_route_search(day_param,depart_int,arrival_int) s
    GROUP BY s.route_id, s.route_id2, s.station_id, train1, train2
    ORDER BY (route_num_stops(s.route_id, depart_int, s.station_id)
                 + route_num_stops(s.route_id2, s.station_id, arrival_int));
$$ LANGUAGE SQL;
--SELECT * FROM multi_route_num_stops('Monday',33,5);


--2B (3) Gets (multi) routes ordered by time
--Assumption: total 'time' here means total times on the routes(trains), not the time of the trip (so time spent waiting at transition station is not considered)
CREATE OR REPLACE FUNCTION multi_route_time (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, route_id2 int, transition_station int, train1 int, train2 int, travel_time float) AS $$
    SELECT s.route_id, s.route_id2, s.station_id transition_point, train1, train2, (route_time(s.route_id, train_speed(train1), depart_int, s.station_id)
                 + route_time(s.route_id2, train_speed(train2), s.station_id, arrival_int))
    FROM multi_route_search(day_param,depart_int,arrival_int) s
    GROUP BY s.route_id, s.route_id2, s.station_id, train1, train2
    ORDER BY (route_time(s.route_id, train_speed(train1), depart_int, s.station_id)
                 + route_time(s.route_id2, train_speed(train2),s.station_id, arrival_int));
$$ LANGUAGE SQL;
--SELECT * FROM multi_route_time('Monday',33,5);


--2B (4) Gets (multi) routes ordered by cost
CREATE OR REPLACE FUNCTION multi_route_cost (day_param varchar, depart_int integer, arrival_int integer)
RETURNS TABLE(route_id int, route_id2 int, transition_station int, train1 int, train2 int, cost int) AS $$
    SELECT s.route_id, s.route_id2, s.station_id transition_point, train1, train2, (route_cost(s.route_id, train_cost(train1), depart_int, s.station_id)
                 + route_cost(s.route_id2, train_cost(train2), s.station_id, arrival_int))
    FROM multi_route_search(day_param,depart_int,arrival_int) s
    GROUP BY s.route_id, s.route_id2, s.station_id, train1, train2
    ORDER BY (route_cost(s.route_id, train_cost(train1), depart_int, s.station_id)
                 + route_cost(s.route_id2, train_cost(train2),s.station_id, arrival_int));
$$ LANGUAGE SQL;
--SELECT * FROM multi_route_cost('Monday',33,5);



--5A  Gets trains that go through a station (on day and within time range)
CREATE OR REPLACE FUNCTION trains_at_station (station_int integer, day_param varchar, start_time time, end_time time)
RETURNS TABLE(train_id int, the_start_time time, route_time float, at_station_time time) AS $$
    SELECT ts.train_id, ts.time, route_time(sr.route_id,t.top_speed, first_station(sr.route_id),station_int), ts.time+(route_time(sr.route_id,t.top_speed, first_station(sr.route_id),station_int)* interval '1 hour')
    FROM trainschedule ts --single_route_search(day_param,depart_int,arrival_int) s
    JOIN station_route sr on ts.route_id = sr.route_id
    JOIN train t on ts.train_id = t.train_id
    WHERE ts.day = day_param
      AND sr.station_id = station_int
      AND ts.time+(route_time(sr.route_id,t.top_speed, first_station(sr.route_id),station_int)* interval '1 hour')
          BETWEEN start_time AND end_time
$$ LANGUAGE SQL;
--SELECT * FROM trains_at_station(23,'Monday','0:45','24:00');


--5B  Gets routes that travel more than one rail line
--Assumption: A route traversing multiple lines means that at least one 'segment' (not station) of the line must be on another line
CREATE VIEW multi_line_routes AS
    SELECT * FROM route WHERE is_multi_line_route(route_id);
--SELECT * FROM multi_line_routes;


--5C  Ranks
--Assumption: We desire to rank the trains based on the number of routes they participate in
CREATE OR REPLACE VIEW ranked_trains AS
    SELECT train.train_id, count(*) routes, RANK() OVER(
            ORDER BY count(*) DESC
        ) rank_no
    FROM train
    JOIN trainschedule t on train.train_id = t.train_id
    GROUP BY train.train_id HAVING COUNT(*) >= 2;
--SELECT * FROM ranked_trains;

--This adds a new customer by their credentials and returns their primary key
CREATE OR REPLACE FUNCTION add_passenger ( first_name VARCHAR(20), last_name VARCHAR(30), street_name VARCHAR(50), city_name VARCHAR(30), zip_add VARCHAR(13) )
RETURNS INTEGER AS
$$
    DECLARE
        id INTEGER;
    BEGIN
        SELECT MAX(customer_id) + 1 INTO id FROM Passenger;
        INSERT INTO Passenger VALUES( id,  first_name, last_name, street_name, city_name, zip_add);
        RETURN id;
    END
$$ LANGUAGE plpgsql;

--Edits the customer information given and returns their id
CREATE OR REPLACE FUNCTION edit_passenger ( id INTEGER, first_name VARCHAR(20), last_name VARCHAR(30), street_name VARCHAR(50), city_name VARCHAR(30), zip_add VARCHAR(13) )
RETURNS INTEGER AS
$$
    BEGIN
        IF first_name != '' THEN
            UPDATE Passenger
            SET f_name = first_name
            WHERE customer_id = id;
        END IF;

        IF last_name != '' THEN
            UPDATE Passenger
            SET l_name = last_name
            WHERE customer_id = id;
        END IF;

        IF street_name != '' THEN
            UPDATE Passenger
            SET street_address = street_name
            WHERE customer_id = id;
        END IF;

        IF city_name != '' THEN
            UPDATE Passenger
            SET city = city_name
            WHERE customer_id = id;
        END IF;

        IF zip_add != '' THEN
            UPDATE Passenger
            SET postal_code = zip_add
            WHERE customer_id = id;
        END IF;

        RETURN id;
    END
$$ LANGUAGE plpgsql;

--Gets the full passenger information based on their credentials / id. Returns table ...checks for similar
CREATE OR REPLACE FUNCTION view_passenger ( id INTEGER, first_name VARCHAR, last_name VARCHAR, street_name VARCHAR, city_name VARCHAR, zip_add VARCHAR )
RETURNS TABLE( cust_id INTEGER, fname TEXT, lname TEXT, streetaddress TEXT, cities TEXT, postalcode TEXT ) AS
$$
    DECLARE
        min INTEGER := 0;
        max INTEGER := 2147483646;
    BEGIN
        IF id != 0 THEN
            min := id;
            max := id;
        END IF;

        IF first_name = '' THEN
            first_name := '^.*$';
        END IF;

        IF last_name = '' THEN
            last_name := '^.*$';
        END IF;

        IF street_name = '' THEN
            street_name := '^.*$';
        END IF;

        IF city_name = '' THEN
            city_name := '^.*$';
        END IF;

        IF zip_add = '' THEN
            zip_add := '^.*$';
        END IF;

        RETURN QUERY(SELECT p.customer_id::INTEGER, p.f_name::TEXT, p.l_name::TEXT, p.street_address::TEXT, p.city::TEXT, p.postal_code::TEXT FROM Passenger p
        WHERE p.customer_id >= min AND p.customer_id <= max
        AND p.f_name ~ first_name
        AND p.l_name ~ last_name
        AND p.street_address ~ street_name
        AND p.city ~ city_name
        AND p.postal_code ~ zip_add);

    END
$$ LANGUAGE plpgsql;

--Gets the last station of a route
CREATE OR REPLACE FUNCTION last_station (route_int integer)
RETURNS INTEGER AS
$$
    SELECT station_id
    FROM station_route
    WHERE route_int = route_id
    ORDER BY order_in_route DESC
    LIMIT 1;

$$ LANGUAGE SQL;


--Gets the max number of seats in a train given the train schdule
CREATE OR REPLACE FUNCTION get_max_seats ( trn_sch INTEGER)
RETURNS INTEGER AS
$$
    DECLARE
        tr_id INTEGER;
        rtn_val INTEGER;
    BEGIN

        SELECT train_id INTO tr_id FROM Trainschedule WHERE schedule_id = trn_sch;

        SELECT num_seats INTO rtn_val FROM Train WHERE train_id = tr_id;

        RETURN rtn_val;

    END;

$$ LANGUAGE plpgsql;

--Calcualtes the avaibale seats of a train given the train schedule
CREATE OR REPLACE FUNCTION get_available_seats ( trn_sch_id INTEGER )
RETURNS INTEGER AS
$$
        SELECT get_max_seats(trn_sch_id) - COUNT(*)  FROM Reservation WHERE train_sch_id = trn_sch_id LIMIT 1;

$$ LANGUAGE SQL;


--Generates another train schdule given a train schdule id
CREATE OR REPLACE FUNCTION get_another_train_schedule( trn_sch_id INTEGER)
RETURNS INTEGER AS
$$
    DECLARE
        rt_id INTEGER;
        return_id INTEGER;

    BEGIN
        SELECT route_id INTO rt_id FROM trainschedule where schedule_id = trn_sch_id;
        SELECT schedule_id INTO return_id FROM trainschedule
        WHERE schedule_id != trn_sch_id AND route_id = rt_id LIMIT 1;

        IF return_id IS NULL THEN
            RETURN -1;
        ELSE
            RETURN return_id;
        END IF;



    END;

$$ LANGUAGE plpgsql;

--Set clock
CREATE OR REPLACE FUNCTION update_clock ( year TEXT, month TEXT, day TEXT, hour TEXT, minute TEXT, second TEXT )
RETURNS TIMESTAMP AS
$$
    DECLARE
        count INTEGER := 0;
    BEGIN

        SELECT COUNT(*) INTO count FROM CLOCK;

        IF count = 0 THEN
            INSERT INTO clock VALUES((year || month || day)::date, (hour || minute || second)::time);
        ELSE
            UPDATE clock
            SET clock_date = (year || month || day)::date, clock_time = (hour || minute || second)::time
            WHERE TRUE;
        END IF;

        RETURN TO_TIMESTAMP( year || '-' || month || '-'|| day || ' ' || hour || ':' || minute || ':' || second , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;

    END
$$ LANGUAGE plpgsql;

--Helper Method that gets the day based on the timestamp - Full week name
CREATE OR REPLACE FUNCTION get_day ( times TIMESTAMP)
RETURNS VARCHAR(10) AS
$$
    DECLARE
        day INTEGER;
    BEGIN

        SELECT EXTRACT(DOW FROM times) INTO day;

        IF day = 0 THEN
            RETURN 'Sunday'::VARCHAR(10);
        ELSEIF day = 1 THEN
            RETURN 'Monday'::VARCHAR(10);
        ELSEIF day = 2 THEN
            RETURN 'Tuesday'::VARCHAR(10);
        ELSEIF day = 3 THEN
            RETURN 'Wednesday'::VARCHAR(10);
        ELSEIF day = 4 THEN
            RETURN 'Thursday'::VARCHAR(10);
        ELSEIF day = 5 THEN
            RETURN 'Friday'::VARCHAR(10);
        ELSE
            RETURN 'Saturday'::VARCHAR(10);
        END IF;

    END;
$$ LANGUAGE plpgsql;

--Converts a given day to a fixed date and given time
CREATE OR REPLACE FUNCTION day_to_timestamp ( day VARCHAR(10), times TIME)
RETURNS TIMESTAMP AS
$$
    BEGIN

        IF day = 'Sunday' THEN
            RETURN TO_TIMESTAMP( '2022-04-24' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        ELSEIF day = 'Monday' THEN
            RETURN TO_TIMESTAMP( '2022-04-25' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        ELSEIF day = 'Tuesday' THEN
            RETURN TO_TIMESTAMP( '2022-04-26' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        ELSEIF day = 'Wednesday' THEN
            RETURN TO_TIMESTAMP( '2022-04-27' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        ELSEIF day = 'Thursday' THEN
            RETURN TO_TIMESTAMP( '2022-04-28' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        ELSEIF day = 'Friday' THEN
            RETURN TO_TIMESTAMP( '2022-04-29' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        ELSE
            RETURN TO_TIMESTAMP( '2022-04-20' || ' ' || times::TEXT , 'YYYY-MM-DD HH24:MI:SS')::TIMESTAMP;
        END IF;

    END;

$$ LANGUAGE plpgsql;

--Puts in a Reservation that is not yet tickted
CREATE OR REPLACE FUNCTION book_reservation ( cust_id INTEGER, tr_sch_id INTEGER)
RETURNS INTEGER AS
$$
    DECLARE
        sch_start_time TIMESTAMP;
        sch_end_time TIMESTAMP;
        rsv_id INTEGER;
        price FLOAT;
    BEGIN
        SELECT day_to_timestamp( day, time) INTO sch_start_time FROM TrainSchedule WHERE schedule_id = tr_sch_id;
        SELECT MAX(sch_start_time) - INTERVAL '2 hour' INTO sch_end_time;
        SELECT route_cost(route_id, train_cost(train_id), first_station(route_id), last_station(route_id)) INTO price
        FROM Trainschedule;

        IF get_available_seats( tr_sch_id ) > 0 THEN
            INSERT INTO Reservation VALUES( DEFAULT, cust_id, tr_sch_id, sch_start_time, sch_end_time, price ,FALSE );

            SELECT reservation_id INTO rsv_id FROM Reservation WHERE customer_id = cust_id AND train_sch_id = tr_sch_id
            AND r_start_time = sch_start_time AND rend_time = sch_end_time;

            RETURN rsv_id;
        ELSE
            RETURN -1;
        END IF;

    END;

$$  LANGUAGE plpgsql;

--Gets the ticket for the reservation given the reservation id
CREATE OR REPLACE FUNCTION get_ticket ( rsv_id INTEGER)
RETURNS VOID AS
$$
    BEGIN
        UPDATE Reservation
        SET ticketed = TRUE
        WHERE reservation_id = rsv_id;
    END;

$$ LANGUAGE plpgsql;

--Method that shows the schdule of a route given the route id
CREATE OR REPLACE FUNCTION display_sch_of_routes ( id INTEGER )
RETURNS TABLE( sch_id INTEGER, days VARCHAR(10), hours TIME, tr_id INTEGER, tr_name VARCHAR(30), dscrptn VARCHAR(100)) AS
$$
    BEGIN

        RETURN QUERY(SELECT routes.schedule_id, routes.day, routes.time, train.train_id, name, description
        FROM (SELECT schedule_id, day, time, train_id
        FROM TrainSchedule
        WHERE route_id = id) AS routes INNER JOIN Train ON routes.train_id = train.train_id);


    END;
$$ LANGUAGE plpgsql;

--Method that gets the routes that stop at a certain percent stations
CREATE OR REPLACE FUNCTION get_routes_that_stop_xx_stations( percentage INTEGER)
RETURNS TABLE ( rt_id INTEGER, trues INTEGER) AS
$$
    BEGIN
        RETURN QUERY (SELECT final.route_id, final.percent::INTEGER
        FROM (SELECT total.route_id AS route_id, (trues.trues / total.total) * 100 AS percent
        FROM (SELECT sr.route_id, COUNT(*) * 1.0 AS total FROM ROUTE
        RIGHT OUTER JOIN Station_route sr
        ON Route.route_id = sr.route_id
        GROUP BY sr.route_id) total INNER JOIN (SELECT sr.route_id, COUNT(*) * 1.0 AS trues FROM ROUTE
        RIGHT OUTER JOIN Station_route sr
        ON Route.route_id = sr.route_id
        WHERE is_stop = true
        GROUP BY sr.route_id) trues ON total.route_id = trues.route_id) final
        WHERE percent >= percentage);

    END;
$$ LANGUAGE plpgsql;

--Get s the train that do not stop at a certain station given staion id
CREATE OR REPLACE FUNCTION get_routes_that_does_not_stop_at_station( stop_st INTEGER)
RETURNS TABLE ( tr_id INTEGER, nme VARCHAR(30), dcrp VARCHAR(100)) AS
$$
    BEGIN

        RETURN QUERY (SELECT train_id, name::VARCHAR(30), description::VARCHAR(100)
        FROM ( SELECT train.train_id, train.name, train.description, t.route_id
        FROM Train LEFT OUTER JOIN trainschedule t ON train.train_id = t.train_id ) A
        LEFT OUTER JOIN station_route ON station_route.route_id = A.route_id
        WHERE station_id = stop_st AND is_stop = FALSE);
    END;
$$ LANGUAGE plpgsql;

--Trigger reservation_cancel and helper methods
CREATE OR REPLACE FUNCTION get_clock_timestamp()
RETURNS TIMESTAMP AS
$$
    DECLARE
        times TIMESTAMP;
    BEGIN
        SELECT (clock_date || ' ' || clock_time)::TIMESTAMP INTO times FROM clock;
        RETURN times;
    end;
$$ LANGUaGE plpgsql;

CREATE OR REPLACE FUNCTION delete_expired_reservation()
RETURNS TRIGGER AS
$$
BEGIN
  DELETE FROM Reservation WHERE get_clock_timestamp() > reservation.rend_time AND ticketed = false;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER reservation_cancel
    AFTER UPDATE ON clock
    EXECUTE PROCEDURE delete_expired_reservation();

--Trigger line disruptions and helper methods
CREATE OR REPLACE FUNCTION change_schedule()
RETURNS TRIGGER AS
$$
    DECLARE
        sch_start_time TIMESTAMP;
        sch_end_time TIMESTAMP;
        prices FLOAT;

    BEGIN
        IF get_another_train_schedule(NEW.schedule_id) != -1 THEN
            SELECT day_to_timestamp( day, time) INTO sch_start_time FROM TrainSchedule WHERE schedule_id = New.schedule_id;
            SELECT MAX(sch_start_time) - INTERVAL '2 hour' INTO sch_end_time;
            SELECT route_cost(NEW.route_id, train_cost(NEW.train_id), first_station(NEW.route_id), last_station(NEW.route_id))
            INTO prices FROM Trainschedule;

            UPDATE Reservation SET train_sch_id = get_another_train_schedule(NEW.schedule_id), r_start_time = sch_start_time, rend_time = sch_end_time, price = prices
            WHERE no_adjustments = FALSE AND train_sch_id = NEW.schedule_id;
        ELSE
            DELETE FROM Reservation WHERE no_adjustments = FALSE AND train_sch_id = NEW.schedule_id;
        END IF;

      RETURN NULL;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER line_disruptions
    AFTER UPDATE ON Trainschedule
    FOR EACH ROW
        WHEN (NEW.disruption = TRUE)
    EXECUTE FUNCTION change_schedule();

---




