Project Introduction
====================

This is a simple DHT implementation created for CS 420: Networks and Distributed Systems.  
This DHT was originally meant to lookup articles of the US Constitution. 

File descriptions
-----------------
  
There are two main files in this project. The first is DHTNode.java.  
This is a node for the DHT. It accepts some commands, and responds accordingly. If the  
file requested hashes to a value greater than this node's key value, it returns the IP  
address of the next server in the DHT. If it hashes to the correct value but the file  
does not exist, it says that file is not in the DHT or has been removed. If it does  
exist, it returns the text of the file.

The second is DHTClient.java. This is the client for the DHT. It connects to the specified  
node in the DHT. To use the client, start it with the command line parameter consisting of  
the hostname of a node in the DHT.

config.properties is a server-specific configuration property that contains the following  
information:  
* NEXT: The next host to connect to.  
* KEYVAL: The key that this host is responsible for.  

keylist.properties is a file every server must have. It contains the articles of the  
constitution and their appropriate key. For example: `1=234` says that article 1 hashes to  
a key of 234.  

DHT Commands
------------

Valid DHT commands are:  
* quit (Shuts down the client)
* shutdown (Shuts down the DHT and then the client)
* keyval (Get the key that the server is responsible for)
* successor (Get the successor node)
* artkey <article number> (Get the hash value of the passed article number)
* insert <article number> (Insert article)
* article <article number> (Perform a lookup on the article)