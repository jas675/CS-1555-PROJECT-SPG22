#Revisions for CS1555 Project Phase 2

We decided to redo signifigant parts of our schema for the second phase of the project.  In using intermediate tables rather than arrays, we sought to make our database more in line with the normal forms; our relation should be in 3NF now (or at close to it).  In order to generate data for the intermediate tables, we created a python script that transformed the seemingly array-based data into a large sequence of insert statements, which can now be found in new_schema.sql.  Additionally, we changed some attributes of our schema to be more in line with the data given to us.

Some assumptions about the data and the results desired are outlined in comments relative to their respective functions in dml.sql
* Disruptions happen to a train schedule. In that case, a the original reservation is changed to the new train schedule. If paid, it will stay paid and there is no change in reservation ID. 
* Since no data was given about the exact dates on the days of the week, hard coded date values were used to aling with teh pseudo-clock
* A flat-rate is calculated for each route not matter where the passengers' destination is. The price is calculated using the per kilometer rate attribute in train table. 
