CREATE TABLE Station
(
    station_id  INTEGER,
    address     VARCHAR(100),
    opening_time_mon_sun TIME[],
    closing_time_mon_sun TIME[],

    CONSTRAINT station_pk PRIMARY KEY(station_id)
);

CREATE TABLE RailLine
(
	rail_id     INTEGER,
	s_station   INTEGER,
	e_station   INTEGER,
	r_line      INTEGER,
	distance    INTEGER,
	speed_limit INTEGER,
	CONSTRAINT rail_line_pk
        PRIMARY KEY(rail_id),
    CONSTRAINT start_station_fk
        FOREIGN KEY(s_station)
        REFERENCES Station(station_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT end_station_fk
        FOREIGN KEY(e_station)
        REFERENCES Station(station_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ref_line_fk
        FOREIGN KEY(r_line)
        REFERENCES RailLine(rail_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Route
(
    route_id   INTEGER,
    rail_lines INTEGER,
    stop       BOOL[],
    CONSTRAINT route_pk
        PRIMARY KEY (route_id),
    CONSTRAINT rail_lines_fk
        FOREIGN KEY(rail_lines)
        REFERENCES RailLine(rail_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Train
(
    train_id        INTEGER,
    top_speed       INTEGER,
    num_seats       INTEGER,
    price_per_mile  NUMERIC(10,2),
    CONSTRAINT train_pk
        PRIMARY KEY (train_id)
);

CREATE TABLE TrainSchedule
(
    train_schedule_id INTEGER,
    train_id          INTEGER,
    route_id          INTEGER,
    curr_avail_seats  INTEGER,
    CONSTRAINT train_schedule_pk
        PRIMARY KEY (train_schedule_id),
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
    email          VARCHAR(50),
    phone_number   VARCHAR(12),
    block_num      VARCHAR(10),
    street_address VARCHAR(50),
    city           VARCHAR(30),
    state          VARCHAR(2),
    zip            INTEGER,
    CONSTRAINT customer_pk
        PRIMARY KEY (customer_id)
);

CREATE TABLE Reservation
(
    reservation_id INTEGER,
    customer_id    INTEGER,
    train_sch_id   INTEGER,
    r_start_time   TIMESTAMP,
    rend_time      INTEGER,
    ticketed       BOOL,

    CONSTRAINT reservation_pk
        PRIMARY KEY (reservation_id),
    CONSTRAINT customer_fk
        FOREIGN KEY (customer_id)
            REFERENCES Passenger (customer_id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT train_sch_fk
        FOREIGN KEY (train_sch_id)
            REFERENCES TrainSchedule (train_schedule_id)
            ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE CLOCK(
    clock_date      DATE,
    clock_time      TIMESTAMP
);

