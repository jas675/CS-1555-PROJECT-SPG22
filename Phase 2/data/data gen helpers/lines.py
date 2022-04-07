
def main():

    print("?")

    _input = open("input2.txt",'r')
    _output = open("output2.txt",'w')
    
    lines = []
    output_str = ""

    for line in _input:
        splits = line.split(":")
        x = int(splits[1].strip().split(" ")[0])
        #lines.append(x)
        
        distances = splits[4].split(",")
        distances = [int(x.strip()) for x in distances]
       
        speed = int(splits[2].strip().split(" ")[0].strip())
        
        stations = splits[3].split(",")

        stations[len(stations)-1] = stations[len(stations)-1].strip().split(" ")[0]
        stations = [int(x.strip()) for x in stations]
        
        #Routes, Stations, Stops
        #print(x)
        #print(stations)
        #print(stops)
        
        #output
        
        query = "INSERT INTO RailLine VALUES("+str(x)+","+str(speed)+");\n"
        output_str+=query
        
        
        for i in range(0,len(stations)):
            #is_stop = stations[i] in stops
            query = "INSERT INTO Station_Line VALUES("+str(x)+","+str(stations[i])+","+str(distances[i])+","+str(i)+");\n"
            output_str += (query)
        
    print(output_str)    
    _output.write(output_str)
    _input.close()
    _output.close()
    #print(output_str)    
        
main()