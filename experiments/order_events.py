

#input_file = "/path/to/linear_output.txt"
#output_file = "/path/to/linear_ordered.txt"
input_file = "/path/to/eventual_cheating.txt"
output_file = "/path/to/eventual_ordered.txt"

input = open(input_file, 'r')
output = open(output_file, 'w')

events = []
ordered = []

for line in input:
    tokens = line.split(" ")
    timestamp = tokens[-1]
    events.append((int(timestamp), line))

events.sort()

for e in events:
    output.write(e[1])
    #print(e)

input.close()
output.close()
