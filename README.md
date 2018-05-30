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