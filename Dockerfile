FROM openjdk:8-jdk

ENV MAVEN_VERSION=3.5.3
ENV USER_HOME_DIR="/root"
ENV SHA=b52956373fab1dd4277926507ab189fb797b3bc51a2a267a193c931fffad8408
ENV BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

# Download maven and install maven
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha256sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Setup maven environment variables
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

ADD . /home/project
WORKDIR /home/project

CMD mvn clean install \
   && java -Djavax.net.ssl.trustStore=configs/client/clientTrustStore \
        -cp .:target/TrustedPasS-1.0-jar-with-dependencies.jar \
        unl.fct.srsc.client.RedisTrustedClient