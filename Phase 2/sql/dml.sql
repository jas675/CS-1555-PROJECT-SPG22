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
