import subprocess

output = subprocess.getoutput("jps")
number = ""

#while True:
for i in iter(output.splitlines()):
    if "Runner" in i:
        for x in i:
            if x.isnumeric():
                number += x
output = subprocess.getoutput("jstack "+number)
f = open("debug.log", "a")
f.write("\n\n\n\n\n")
f.write(output)
f.close()