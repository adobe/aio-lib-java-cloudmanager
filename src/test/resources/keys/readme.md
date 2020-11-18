This folder contains the private keys for testing purposes.

They have been created with `openssl genpkey -out private-key-pkcs8-rsa.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048`.

Both PEM and DER format are provided while the latter has been converted from PEM format to the required PKCS-8 DER format with `openssl pkcs8 -topk8 -inform PEM -outform DER -in private-key-pkcs8-rsa.pem -out private-key-pkcs8-rsa.der -nocrypt`