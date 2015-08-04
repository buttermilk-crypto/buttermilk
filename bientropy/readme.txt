Implementation of bientropy algorithm as described in http://arxiv.org/ftp/arxiv/papers/1305/1305.0954.pdf
Copyright David R. Smith, 2015. All Rights Reserved.
 
 
 Buttermilk BiEntropy Command Line Client
--------------------------------------------

Usage:

java -jar bientropy.jar BIENTROPY
                         -i|--item <string>
                        [-a|--armor hex|base16|base64|base64url|none]
                        [-f|--outputFormat json|csv|none]
                        [-s|--stdin]

-i|--item
        a command line value to process (possibly in armored form)
-a|--armor
        specifies the content encoding of the input items (for binary data) such as base64
        use "none" for ascii text input
[-f|--outputFormat json|csv|none]
        the output format
[-s|--stdin]
        accept piped input. e.g., 
        java -jar target\bientropyApp-jar-with-dependencies.jar bientropy -a base16 -f none -s < input.txt

For one byte, the BiEntropy algorithm variant will be used (Power Series).
For multiple bytes, the TresBiEntropy algorithm variant will be used (Logarithmic Series).

*************************************************************************************************

Utility functions:

To generate some cryptographically strong random binary data:

java -jar bientropy.jar RAND
                         -l|--length
                         -c| --count
                        [-a|--armor none|hex|base16|base64|base64url]
 
 
 
 
 