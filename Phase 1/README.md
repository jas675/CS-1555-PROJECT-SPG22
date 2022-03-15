# CS-1555-PROJECT-SPG22

Phase 1 Part of the CS 1555 Project is implemented here. Phase 1 contains the ER Diagram, SQL DDL and a ZIP folder containing all the previous. 
Assumtions:
*All non pseudo tables has a primary id
*A station has a station ID
*A stations hours of operation is in 24 hours format eg“23:59-23:59”
*Rail Line is defined as a line segment with two stations. One as start and one as end station
*If a Rail Line contains more than two stations, the other subset of rail line can be referenced to itself
*A Route is made up of a rail line
*A reservation can only be held for a certain amount of time
*A string data type has appropriate length limitations
*The Rail Line follows a recursive relation 
