# TrustedPasSEnvironment
Computers and networks Security TP2 Trusted PaaS Environment for Containerized Services


## Instructions:

### Setup (and run server):

Run virtual box and install docker.

Then run redis server with ```docker run -p 6379:6379 redis```

It will download and run the redis server forwarding the VM port 6379 to container 6379 port where the redis server is running


##### Important
Make sure to setup port forwarding on virtual box as well, to map the port 6379 of the host to the VM (on 6379 as well)

### Run client (project):

Now, on the root directory of the project just build the image with:
``docker build -t srsc .``

and run the client specifying the host IP on the REDIS_SERVER variable:

``docker run -it -e REDIS_SERVER=192.168.118.32 srsc``

### Security Configurations

Per default, the client will use the blowfish encryption algorithm with a 448 byte key.
To personalize the security configurations follow the instructions bellow.

1. Create a folder anywhere on your computer with the following structure:
   
              .
              ├── ciphersuite.yml                   # Confguration file
              └── keystore.jceks                    # Keystore containing keys              

2. Run the client with the command:

````docker run -it -v /path/to/config/folder:/home/project/configs -e REDIS_SERVER=192.168.118.32 srsc````

##### Ciphersuite.yml structure:

``` yaml
# YAML
config:
  - ciphersuite: ciphersuite        # in algorithm/mode/padding format example: (blowfish/ECB/PKCS5Padding)
    provider: SunJCE                # Security Provider
    hmac: HMacSHA1                  # HMac Hashing algorithm
    keyStoreType: JCEKS             # Keystore type
    keyStoreName: keystore.jceks    # Keystore name
    keyName: mykey                  # Key name
    keyPassword: P4s5w0rd           # Key password
    keyStorePassword: P4s5w0rd      # Keystore password
```
