@echo off    
for /R %%f in (*.class) do del %%f
javac Runner.java
if  errorlevel 1 goto ERROR
java Runner
exit /b 0

:ERROR
echo No Java installation found
echo 1. Check if Java is installed
echo 2. Check Path Variables point to the folder containg java.exe