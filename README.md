# Buttermilk Crypto

Buttermilk crypto is a project serving the overall ambition to replace PKIX with something more contemporary and viable. We have plenty of interesting cryptographic algorithms but few ways to make use of them. 

The specific objectives explored by the project in version 1.0.0 release:

  - techniques to measure entropy of binary data (and thus the strength of keys and passwords)
  - work on key vaulting
  - work on contemporary key materials formats
  
Specific to the Java(tm) programming language, work was done to extract Java-based cryptography from the JCE provider framework, which is a superstructure built on top of the PKIX and ASN.1 encodings. 
  
I think version 1.0 was able to demonstrate it is feasible to do practical work without PKIX, ASN.1, and in the Java(tm) context, without JCE. 
  


 