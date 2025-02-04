# mqtt-binarystreamclient

MQTT Binary Stream Client project, exercise 6 from the website (https://perso.telecom-paristech.fr/diacones/mqtt/mqtt-tp.html) for SLR203 course in Télécom Paris.

## Compilation and execution

Compilation: with Maven `mvn compile`

Execution:
- For the 6.1 (connect and connack), run `mvn exec:java@connectconnack`
- For the 6.2 (publish), run `mvn exec:java@publish`
- For the 6.3 (subscribe), run `mvn exec:java@subscribe`
- For the 6.4 (asynchronous), run `mvn exec:java@asynchronous`