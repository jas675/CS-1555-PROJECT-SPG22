drop table if exists station_line;
drop table if exists railline;
drop table if exists station_route;
drop table if exists station;
drop table if exists reservation;
drop table if exists trainschedule;
drop table if exists route;
drop table if exists train;
drop table if exists passenger;
drop table if exists clock;

CREATE TABLE Station
(
    station_id  INTEGER,
    name        VARCHAR(30),

    opening_time TIME,
    closing_time TIME,

    delay       INTEGER,

    street      VARCHAR(100),
    town        VARCHAR(100),
    postal_code CHAR(13),

    CONSTRAINT station_pk
        PRIMARY KEY(station_id)
);

CREATE TABLE RailLine
(
	rail_id     INTEGER,
	speed_limit INTEGER,
	CONSTRAINT rail_line_pk
        PRIMARY KEY(rail_id)
);

CREATE TABLE Station_Line
(
    rail_id     INTEGER,
    station_id  INTEGER,
    distance_prev INTEGER,
    order_in_line       INTEGER,

    CONSTRAINT station_line_pk
        PRIMARY KEY(rail_id,station_id,order_in_line),

    CONSTRAINT rail_id_fk
        FOREIGN KEY(rail_id)
        REFERENCES RailLine(rail_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT station_line_id_fk
        FOREIGN KEY(station_id)
        REFERENCES station(station_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Route
(
    route_id   INTEGER,
    CONSTRAINT route_pk
        PRIMARY KEY (route_id)
);

CREATE TABLE Station_Route(
    route_id       INTEGER,
    station_id     INTEGER,
    is_stop        BOOLEAN,
    order_in_route INTEGER,

    CONSTRAINT station_route_pk
        PRIMARY KEY(route_id,station_id,order_in_route),

    CONSTRAINT route_id_fk
        FOREIGN KEY(route_id)
        REFERENCES route(route_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT station_route_id_fk
        FOREIGN KEY(station_id)
        REFERENCES station(station_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Train
(
    train_id        INTEGER,
    name            VARCHAR(30),

    description     VARCHAR(100),
    num_seats       INTEGER,
    top_speed       INTEGER,

    price_per_km    INTEGER,
    CONSTRAINT train_pk
        PRIMARY KEY (train_id)
);

CREATE TABLE TrainSchedule
(
    schedule_id       INTEGER,
    route_id          INTEGER,
    day               VARCHAR(10),
    time              TIME,
    train_id          INTEGER,

    CONSTRAINT train_schedule_pk
        PRIMARY KEY (schedule_id),
    CONSTRAINT train_fk
        FOREIGN KEY (train_id)
        REFERENCES Train(train_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT route_fk
        FOREIGN KEY (route_id)
        REFERENCES Route(route_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE Passenger
(
    customer_id    INTEGER,
    f_name         VARCHAR(20),
    l_name         VARCHAR(30),


    street_address VARCHAR(50),
    city           VARCHAR(30),

    postal_code    CHAR(13),

    CONSTRAINT customer_pk
        PRIMARY KEY (customer_id)
);

CREATE TABLE Reservation
(
    reservation_id SERIAL,
    customer_id    INTEGER,
    train_sch_id   INTEGER,
    r_start_time   TIMESTAMP,
    rend_time      TIMESTAMP,
    ticketed       BOOL,

    CONSTRAINT reservation_pk
        PRIMARY KEY (reservation_id),
    CONSTRAINT customer_fk
        FOREIGN KEY (customer_id)
            REFERENCES Passenger (customer_id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT train_sch_fk
        FOREIGN KEY (train_sch_id)
            REFERENCES TrainSchedule (schedule_id)
            ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE CLOCK(
    clock_date      DATE,
    clock_time      TIME
);


