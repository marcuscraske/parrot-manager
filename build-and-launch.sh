#!/bin/bash

mvn clean package &&
java -jar parrot-manager/target/parrot.jar

