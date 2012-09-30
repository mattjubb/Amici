Amici
=====

p2p social network

####Introduction
Amici is a protocol that facilitates social communication in a distributed manner. 
In many respects it aims to provide much of the same functionality as you may expect from Twitter or App.net but without any centralized control/ownership.

####Background
I personally believe that Twitter was a revolution. It provided a brand new way of communicating on the internet in a way that changed things on a scale not seen since email.
But I always saw Twitter as protocol. The problem as I see it is that unlike email, twitter has been treated as a service controlled by one organisation rather than a standard that is free to be used.
Although many competitors are coming along to address deficiencies in Twitter, most don't seem to be specifically fixing this. 

####Technicals
Amici is built on [openkad](http://code.google.com/p/openkad/) which is a Java implementation of the [Kademlia DHT](http://en.wikipedia.org/wiki/Kademlia). Openkad basically provides a massive Hash Map ontop of all nodes on the Amici network. 

All posts are stored by keys (author, hash tag, mention, etc) on the associated node in the network.

Of course we need to be able to verify that a post was indeed written by the person we think it was. Amici uses digital signatures from [Email Certificates](http://en.wikipedia.org/wiki/S/MIME). When a user first starts using Amici they must upload their CA trusted certificate. All subsequent posts are signed with their private key and we can verify that signature against their certificate. Luckily Email Certificates are free from [StartSSL](http://www.startssl.com/?app=1), [Comodo](http://www.comodo.com/home/email-security/free-email-certificate.php) etc.

**There are two entities in Amici; Server or Node and Client.**

An *Amici server* will be running the code in this repository and is reponsible for storing/retrieving posts/certificates. It should be relatively long lived ie not starting/stopping as this creates more work for the network. It exposes a HTTP REST API (see [HttpRestServer.java](https://github.com/mattjubb/Amici/blob/master/src/org/amici/server/HttpRestServer.java))as a way clients can interact with it.

An *Amici client* will be per user and does not store any data apart from the user's details and private key. When the client is first used it should upload the user's certificate to the network. It may then submit posts or query for existing posts.


####Getting Started
You'll need Java 7 and [Apache Ivy](http://ant.apache.org/ivy/) to manage all the dependencies. Once you've installed that then run the `ant` command.
This will build Amici.jar which you can then run with `java -jar Amici.jar 5555` 

Amici server now be running on port 5555 with the URI openkad.udp://localhost:5555/. This instance is not connected to any other node - to do that youll also need to provide the URI of another Amici node ie `java -jar Amici.jar 5556 openkad.udp://localhost:5555/`

####Todos
* Currently the DataStore implementation (BasicDataStoreImpl) is just a bunch of in memory HashMaps. So there's no persistance. I'd like to replace this with a MongoDB backed storage.
* No tests - I know pretty bad.
* Expose all OpenKAD settings somehow - probably launch with a .properties file.
* REST server is not complete - still missing registration

####Help?!
Obviously this could end up being a fairly large project, I've come this far just to demonstrate that it somewhat works. 

But I'd much prefer if it was community driven - so please submit pull requests, suggestions, critiques, issues. I'll try my hardest to be open to them.