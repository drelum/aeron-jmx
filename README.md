# aeron-jmx
JMX Monitoring for [Aeron](https://github.com/real-logic/Aeron) project. It provides a simple implementation that reads [stats counters](https://github.com/real-logic/Aeron/wiki/Monitoring-and-Debugging#system-and-position-counters) and publish them as JMX Beans.





To run:

java -cp ./aeron-driver.jar:aeron-jmx.jar com.aeron.aeronjmx.AeronMBean <AeronCNCFile>
