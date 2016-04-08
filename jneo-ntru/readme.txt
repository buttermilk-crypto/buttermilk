This is a repackaged version of https://github.com/NTRUOpenSourceProject/ntru-crypto. It has been mavenized,
formatted for java, and some of the lint removed. Otherwise the essentials are unchanged.

Buttermilk generally uses the Bouncy Castle lightweight encryption classes, and BC includes an NTRU implementation
(apparently originally derived from https://tbuktu.github.io/ntru/). So you might ask why not use those?

Because a) it would require the BC "ext" jar, which I wanted to avoid, and b) more importantly, both the BC and 
original Tim Buktu implementations leave something to be desired from the standpoint of code re-use.
 
Both Security Innovation and the BC/Tim Buktu code base provides the idea of binary serialized private keys, I 
guess along the lines of PKCS#8, which is the rubbish we are trying to do something about. So we have a different 
idea on how to format keys. But to implement that idea requires exposing the key data for packaging using some 
other approach. I could not achieve this in the BC/Tim Buktu code due to the lack of accessor methods and the 
way the classes are designed. It would require a significant re-write.
    
At any rate, the Security Innovation code (i.e., this code) lacks the Fast FP support found in BC/Tim Buktu 
implementations but EES1087EP1, which is supposed to provide a 256 bit security level, still seems pretty good
to me for general use. That is, it still seems viable. I will work on supporting APR2011_439_FAST or APR2011_743_FAST
as time permits. 