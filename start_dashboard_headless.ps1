$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$mvn = "C:\Program Files\apache-maven-3.9.12\bin\mvn.cmd"
$inputContent = Get-Content inputs.txt -Raw

# Pipe input to the maven command
$inputContent | & $mvn clean compile exec:java -Dexec.mainClass="com.nexis.app.NexisNode"
