Global-Snapshot
==============

Introduction
-------

Distributed Snapshot uses [Chandy-Lamport snapshot algorithm](https://en.wikipedia.org/wiki/Snapshot_algorithm) and applied into a simple banking application. Application is build on top of [Java RMI](http://en.wikipedia.org/wiki/Java_remote_method_invocation), which is an object-oriented equivalent of remote procedure calls ([RPC](http://en.wikipedia.org/wiki/Remote_procedure_call)).

Bank branches are treated as nodes, which form a strongly connected graph (single connected component). Each node has an initial balances and start exchanging random amount of money (within predefined boundaries) at fixed time rate as soon as they are connected. Any node can initiate snapshot and log it into *.csv file.

Distributed Snapshot effectively selects a consistent cut (no messages jump from future into the past and no message receipt is recorded without send) and is a non-blocking algorithm. Snapshot reflects the global state in which the distributed system might have been.

####Features
    - nodes can be run on separate hosts
    - nodes can join but cannot leave (for simplicity of the cut)
    - nodes can initiate and log the distributed snapshot
    - multiple snapshots can be taken at a same time (distinguished by snapshot ID)

####Assumptions
    - system is peer-to-peer: any bank can connect to any other
    - bank makes new transfer immediately after the previous one
    - nodes knows one existing node (id and host) in the graph in order to join

Installation
-------
Requirements: *JDK 8*, *Maven*

Configure service parameters in **service.properties** file.

####Run inside of IDE
    - mvn clean install
    - run main ServerLauncher.java
    
####Run as executable JAR
    - mvn clean install
    - execute following line in new window to start the node (bank):
        - java -jar GlobalSnapshot-${version}-jar-with-dependencies.jar

Server Nodes State Machine Diagram
-------
![Diagram](/diagrams/Server_Nodes_State_Machine_Diagram.png)

Architecture Diagram
-------
![Diagram](/diagrams/Architecture_Diagram.png)

Documentation
-------
[Project Description (PDF)](/docs/Project_Description.pdf)

Authors
-------
[Victor Ekimov](https://github.com/NorthernDemon)