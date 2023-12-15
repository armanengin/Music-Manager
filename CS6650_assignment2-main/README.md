# assignment 2

## Installation:
* Clone the repository
* mvn clean install
* for load balanced servers: run the command mvn exec:java -Dexec.mainClass="io.swagger.client.api.TestClient" -Dexec.args="10 30 2 'http://ip_address_of_load_balancer/javaServer_war'"
* for one server and db: run the command mvn exec:java -Dexec.mainClass="io.swagger.client.api.TestClient" -Dexec.args="10 30 2 'http://ip_address_of_server:port/javaServer_war'"

