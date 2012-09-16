Amici
=====

p2p social network

####Introduction
Amici is essentially a protocol that facilitates social communication in a distributed manner. 
In many respects it aims to provide the same functionality as you may expect from Twitter or App.net but without any centralized control.

####Background
I personally believe that Twitter was a revolution. It provided a brand new way of communicating on the internet in a way that changed things on a scale not seen since email.
But I always saw Twitter as protocol. The problem as I see it is that unlike email, twitter etc is a service controlled by one organisation rather than a standard that is free to be used.
Although competitors are coming along to address deficiencies in Twitter, I can't find anyone attempting to solve this problem. 

####Technicals
Amici is built on [openkad](http://code.google.com/p/openkad/) which is a Java implementation of the [Kademlia DHT](http://en.wikipedia.org/wiki/Kademlia). Openkad basically provides a massive Hash Map ontop of all nodes on the Amici network. 

All posts are stored by keys (author, hash tag, mention, etc) on the associated node in the network.

Of course we need to be able to verify that a post was indeed written by the person we think it was. Amici uses digital signatures from [Email Certificates](http://en.wikipedia.org/wiki/S/MIME). When a user first starts using Amici they must upload their CA trusted certificate. All subsequent posts are signed with their private key and we can verify that against their certificate. Luckily Email Certificates are free from [StartSSL](http://www.startssl.com/?app=1), [Comodo](http://www.comodo.com/home/email-security/free-email-certificate.php) etc.
