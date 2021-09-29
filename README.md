# Zazuko CARML Service #

This project creates a web service around the [CARML RML Engine](https://github.com/carml/carml), this allows you to send mappings and sources with a POST and will get the resulting triples.

## Flavors ##

This project provides two flavors
* WAR to use in stock tomcat
* stand-alone service which uses [Apache Meecorwave](https://openwebbeans.apache.org/meecrowave/)

## Building ##

To build this project you need a standard maven setup

```mvn clean package```

Will generate both the Meecrowave bundle and the drop in WAR

Results are available in
```war/target/war-1.0.0-SNAPSHOT.war```
```service/target/meecrowave-meecrowave-distribution.zip```

The war should be copied in the Tomcat webapps directory, the zip distribution contains a Meecrowave instance that can be started through ```bin/meecrowave.sh run```

The war has test endpoint at ```service/test``` the meecrowave instance has the test endpoint at ```/test```

## Service ##

The service at ```/```(*meecrowave*),```/service/```(*war*) expect ```multipart/form-data``` with the following fields to be POSTed
* ```mapping``` a turtle based R2RML mapping file
* ```source``` the source file, the formats supported are XML, CSV and JSON, indicated by the content type

Headers
* The service supports content negotiation to determine the result format through the ```Accept``` header, if none is provided it will return ```text/turtle```

### Results ###
Either a RDF file in the requested format is returned with ```200 OK``` status code or a error report according to the [Problem Details for HTTP APIs
](https://datatracker.ietf.org/doc/html/rfc7807#section-3) with a ```400``` status code.
