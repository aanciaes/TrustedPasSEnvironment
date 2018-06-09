# TrustedPasSEnvironment
Computers and networks Security TP2 Trusted PaaS Environment for Containerized Services


## Deployment Instructions (https://hub.docker.com/u/aanciaes/):

### Setup (and run server):

Run virtual box and install docker.

Then run redis server with TLS server TPM module with the following command ```docker run --network host -p 9999:9999 -p 6379:6379 aanciaes/redis-tpm```

It will download and run the redis server forwarding the VM port 6379 to container 6379 port where the redis server is running

* --network host: to remove isolation from docker container and host
* -p 9999:9999: maps the tls tpm module port. Can be changed if the change is reflected on te server config file
* -p 6379:6379: maps the redis port.

#### Server Configurations

By default the project has a config file. To change it, there is no need to change the compiled code.
Create a folder with the following structure:

              .
              ├── ciphersuite.yml         # Confguration file
              └── server.jks              # Keystore containing keys  
              
With the ciphersuite.yml with the following structure:

```yaml
#YAML
serverConfig:
  keyStoreType: JKS
  keyStoreName: server.jks
  keyStorePassword: P4s5w0rd
  keyPassword: P4s5w0rd
  ciphersuites:
    - TLS_RSA_WITH_AES_256_CBC_SHA256
  confProtocols:
   - TLSv1.2
  sslContext: TLS
```

and run the server with the argument ````-v /path/to/folder/created:/home/project/configs/server````

##### Important
Make sure to setup port forwarding on virtual box as well, to map the port 6379 of the host to the VM (on 6379 as well)

### Run client (project):

Run the client specifying the host IP on the REDIS_SERVER variable:

``docker run -it -e REDIS_SERVER=192.168.118.32 aanciaes/srsc``

### Security Configurations

Per default, the client will use the blowfish encryption algorithm with a 448 byte key.
To personalize the security configurations follow the instructions bellow.

1. Create a folder anywhere on your computer with the following structure:
   
              .
              ├── ciphersuite.yml                   # Confguration file
              ├── clientTrustStore.yml              # TrustStore with the server certificate in it
              └── keystore.jceks                    # Keystore containing keys              

2. Run the client with the command:

````docker run -it -v /path/to/config/folder:/home/project/configs/client -e REDIS_SERVER=192.168.118.32 aanciaes/srsc````

##### Ciphersuite.yml structure:

``` yaml
# YAML
securityConfig:
    ciphersuite: ciphersuite            # in algorithm/mode/padding format example: (blowfish/ECB/PKCS5Padding)
    provider: SunJCE                    # Security Provider
    hmac: HMacSHA1                      # HMac Hashing algorithm
    keyStoreType: JCEKS                 # Keystore type
    keyStoreName: keystore.jceks        # Keystore name
    keyName: symkey                     # Key name
    keyPassword: P4s5w0rd               # Key password
    keyStorePassword: P4s5w0rd          # Keystore password
    signatureAlgorithm: SHA512withRSA   # Algorithm used to sign and check authenticity
    signatureAlgProvider: SunRsaSign    # Provider of the digital signature algorithm
    signatureKeyName: asymkey           # Asymetric key pair alias
    signatureKeyPassword: P4s5w0rd      # Asymetric key pair password
    
tpmHosts:
  vmsHost: localhost                    # VMS module SSL server host
  vmsPort: 9999                         # VMS module SSL server port
  gosHost: locahost                     # GOS module SSL server host
  gosPort: 8888                         # GOS module SSL server port
  ciphersuite: aes/ECB/PKCS5Padding     # Ciphersuite for Attestation Status encryptiom
  keySize: 256                          # Algorithm key size
  provider: SunJCE                      # Algorithm Provider
```
