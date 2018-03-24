# Base Image
FROM ubuntu

# Enable Networking on port 8080
EXPOSE 8080

# Installing dependencies and deleting cache
RUN apt-get update&& apt-get install -y \
    git \
    maven \
    tomcat8 \
    openjdk-8-jdk\
    python2.7 \
    python2.7-numpy \
    python-matplotlib \
    python2.7-scipy \
    python2.7-lxml \
    wget \
&& rm -rf /var/lib/apt/lists/*

# Repository
RUN cd /opt && git clone --recurse-submodules https://gitlab2.informatik.uni-wuerzburg.de/chr58bk/OCR4all_Web.git

# Enabling direct request in Larex submodule
RUN sed -i 's/#directrequest:<value>/directrequest:enable/' /opt/OCR4all_Web/src/main/resources/LAREX/Larex/src/main/webapp/WEB-INF/larex.config

# Initial builds of maven project
RUN cd /opt/OCR4all_Web && mvn package
RUN cp /opt/OCR4all_Web/target/OCR4all_Web.war /var/lib/tomcat8/webapps/
RUN cd /opt/OCR4all_Web/src/main/resources/GTC_Web && mvn package
RUN cp /opt/OCR4all_Web/src/main/resources/GTC_Web/target/GTC_Web.war /var/lib/tomcat8/webapps/
RUN cd /opt/OCR4all_Web/src/main/resources/LAREX/Larex && mvn package 
RUN cp /opt/OCR4all_Web/src/main/resources/LAREX/Larex/target/Larex.war /var/lib/tomcat8/webapps/

# Grant tomcat permissions to ocr4all volume
RUN mkdir /var/ocr4all && chmod g+w /var/ocr4all && chgrp tomcat8 /var/ocr4all

# Install ocropy
RUN cd /opt/OCR4all_Web/src/main/resources/ocropy && \
    wget -nd http://www.tmbdev.net/en-default.pyrnn.gz && \
    mkdir models && \
    mv en-default.pyrnn.gz models/ && \
    python2.7 setup.py install

RUN ln -s /usr/local/bin/ocropus-nlbin /bin/ocropus-nlbin
RUN ln -s /usr/local/bin/ocropus-gpageseg /bin/ocropus-gpageseg
RUN ln -s /usr/local/bin/ocropus-rpred /bin/ocropus-rpred

# Start server when container is started
# Enviroment variable
ENV CATALINA_HOME /usr/share/tomcat8
RUN ln -s /var/lib/tomcat8/common $CATALINA_HOME/common && \
    ln -s /var/lib/tomcat8/server $CATALINA_HOME/server && \
    ln -s /var/lib/tomcat8/shared $CATALINA_HOME/shared && \
    ln -s /etc/tomcat8 $CATALINA_HOME/conf && \
    mkdir $CATALINA_HOME/temp && \
    mkdir $CATALINA_HOME/webapps && \
    ln -s /var/lib/tomcat8/webapps/OCR4all_Web.war $CATALINA_HOME/webapps && \
    ln -s /var/lib/tomcat8/webapps/GTC_Web.war $CATALINA_HOME/webapps && \
    ln -s /var/lib/tomcat8/webapps/Larex.war $CATALINA_HOME/webapps
ENTRYPOINT [ "/usr/share/tomcat8/bin/catalina.sh", "run" ]
