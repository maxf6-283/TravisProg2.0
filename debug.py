import subprocess

output = subprocess.getoutput("jps")
number = ""

for i in iter(output.splitlines()):
    if "Runner" in i:
        for x in i:
            if x.isnumeric():
                number += x

output = subprocess.getoutput("jstack "+number)

f = open("debug.log", "w")
f.write("\n\n\n\n\n")
f.write(output)
f.close()