# TrustedPasSEnvironment
Computers and networks Security TP2 Trusted PaaS Environment for Containerized Services


## Deployment Instructions (https://hub.docker.com/u/aanciaes/):

### Setup (and run server):

Run virtual box and install docker.

Copy the jar file TrustedPaaS-1.0-jar-with-dependencies.jar to the virtual box guest OS.
Copy the config folder as well with the following structure:

              .
              └── configs                   # Confguration folder
                └── server                  # Server configurations
                    ├── ciphersuite.yml     # Config file
                    └── server.jks          # Keystore containing keys  


Next step is to install stunnel4 software to allow TLS connection between the Redis server and the client.

``apt-get install stunnel4``

After the installation go to the stunnel config file at ``/etc/default/stunnel`` and change the ``ENABLE`` line to 1.

Copy the files ``private.pem`` and ``redis-server.conf`` in folder ````configs/stunnel/```` to ``/etc/stunnel/`` so the stunnel service recognizes and runs the configuration file.
(You can create your own config file and certificates)

Run stunnel with ````sudo service stunnel4 start```` 

And in the directory created above to the tpm, run the TPM module with ````java -cp .:TrustedPaaS-1.0-jar-with-dependencies.jar unl.fct.srsc.tpm.TpmTLSServer````

Then run redis with the following command ```docker run -p 6379:6379 redis```. You can provide a redis.conf file and set a password if you like.

It will download and run the redis server forwarding the VM port 6379 to container 6379 port where the redis server is running

#### Server Configurations

By default the project has a config file. To change it, there is no need to change the compiled code.
Just change the ciphersuite.yml in the previous folder:
              
The file ciphersuite.yml must have the following structure:

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

and run the server the TLS server again with the same command.

#### Redis Configuration file
Additionally you can specify your own redis configuration file with the argument

````-v /myredis/conf/redis.conf:/usr/local/etc/redis/redis.conf````

If you want to specify a password, make sure to indicate it in the client config file as well

##### Important
Make sure to setup port forwarding on virtual box as well, to map the port 6379 of the host to the VM (on 6379 as well) or setup the proper networking.

### Run client (project):

To run the client you need to specify the host and port where stunnel is accepting requests and run the command:

``docker run -it -e STUNNEL_HOST=192.168.118.32 -e STUNNEL_PORT=8888 aanciaes/srsc``

You can specify the variable NUMBER_OF_OPS to change the number of operations of the benchmark with -e NUMBER_OF_OPS=1000

### Security Configurations

By default, the client will use the blowfish encryption algorithm with a 448 byte key.
To personalize the security configurations follow the instructions bellow.

1. Create a folder anywhere on your computer with the following structure:
   
              .
              ├── ciphersuite.yml                   # Configuration file
              ├── clientTrustStore.yml              # TrustStore with the server certificate in it
              └── keystore.jceks                    # Keystore containing keys              

2. Run the client with the command:

````docker run -it -v /path/to/config/folder:/home/project/configs/client -e STUNNEL_HOST=192.168.118.32 -e STUNNEL_PORT=8888 aanciaes/srsc````

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
    redisServer: 192.168.56.101         # Redis Server
    redisPassword: foobared             # Redis Password (Leave blank if no authentication is required)

vmsTpm:
  host: 192.168.56.101                  # VMS module SSL server host
  port: 9999                            # VMS module SSL server port
  ciphersuite: aes/ECB/PKCS5Padding     # Ciphersuite for Attestation Status encryption
  keySize: 256                          # Algorithm key size
  provider: SunJCE                      # Algorithm Provider

gosTpm:     
  host: locahost                        # GOS module SSL server host
  port: 8888                            # GOS module SSL server port
  ciphersuite: aes/ECB/PKCS5Padding     # Ciphersuite for Attestation Status encryption
  keySize: 256                          # Algorithm key size
  provider: SunJCE                      # Algorithm Provider               
```
