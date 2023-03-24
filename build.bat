@echo off    
for /R %%f in (*.class) do del %%f
javac Runner.java