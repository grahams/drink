@echo off
javac -bootclasspath c:\develop\tini1.02\bin\tiniclasses.jar -d bin *.java
java TINIConvertor -f bin -o bin\DrinkServer.tini -d c:\develop\tini1.02\bin\tini.db
