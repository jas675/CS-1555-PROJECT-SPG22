
def main():

    print("?")

    _input = open("input.txt",'r')
    _output = open("output.txt",'w')
    
    routes = []
    output_str = ""

    for line in _input:
        splits = line.split(":")
        x = int(splits[1].strip().split(" ")[0])
        routes.append(x)
        
        stops = splits[3].split(",")
        stops = [int(x.strip()) for x in stops]
       
        
        stations = splits[2].split(",")

        stations[len(stations)-1] = stations[len(stations)-1].strip().split(" ")[0]
        stations = [int(x.strip()) for x in stations]
        
        #Routes, Stations, Stops
        #print(x)
        #print(stations)
        #print(stops)
        
        #output
        
        query = "INSERT INTO route VALUES("+str(x)+");\n"
        output_str+=query
        
        
        for i in range(0,len(stations)):
            is_stop = stations[i] in stops
            query = "INSERT INTO station_route VALUES("+str(x)+","+str(stations[i])+","+str(is_stop)+","+str(i)+");\n"
            output_str += (query)
        
    _output.write(output_str)
    _input.close()
    _output.close()
    #print(output_str)    
        
main()