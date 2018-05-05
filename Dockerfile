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
    python-skimage \
    python2.7 \
    python2.7-numpy \
    python-matplotlib \
    python2.7-scipy \
    python2.7-lxml \
    wget \
    python3 \
    python3-lxml \
    python3-pil \
    python-setuptools \
    python-pip \
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

# Create ocr4all directories and grant tomcat permissions
RUN mkdir -p /var/ocr4all/data && \
    mkdir -p /var/ocr4all/models/default && \
    mkdir -p /var/ocr4all/models/custom && \
    chmod -R g+w /var/ocr4all && \
    chgrp -R tomcat8 /var/ocr4all

# Install ocropy
RUN cd /opt/OCR4all_Web/src/main/resources/ocropy && \
    wget -nd http://www.tmbdev.net/en-default.pyrnn.gz && \
    mkdir models && \
    mv en-default.pyrnn.gz models/ && \
    python2.7 setup.py install

# Make ocropus standard model available to OS environment
RUN mkdir /usr/local/share/ocropus
RUN ln -s /opt/OCR4all_Web/src/main/resources/ocropy/models/en-default.pyrnn.gz /usr/local/share/ocropus/en-default.pyrnn.gz
# Make pretrained ocropus models available to project environment
RUN for OCR_MODEL in `cd /opt/OCR4all_Web/src/main/resources/ocropy/pretraining/models && ls`; \
        do ln -s /opt/OCR4all_Web/src/main/resources/ocropy/pretraining/models/$OCR_MODEL /var/ocr4all/models/default/$OCR_MODEL; \
    done

# Make all ocropus scripts available to JAVA environment
RUN for OCR_SCRIPT in `cd /usr/local/bin && ls ocropus-*`; do ln -s /usr/local/bin/$OCR_SCRIPT /bin/$OCR_SCRIPT; done

# Install tensorflow
RUN pip install --upgrade tensorflow

# Install calamari
RUN cd /opt/OCR4all_Web/src/main/resources/calamari && \
    python setup.py install

# Make all calamari scripts available to JAVA environment
RUN for CALAMARI_SCRIPT in `cd /usr/local/bin && ls calamari-*`; do ln -s /usr/local/bin/$CALAMARI_SCRIPT /bin/$CALAMARI_SCRIPT; done

# Make pagedir2pagexml.py available to JAVA environment
RUN ln -s /opt/OCR4all_Web/src/main/resources/pagedir2pagexml.py /bin/pagedir2pagexml.py

# Start server when container is started
# Enviroment variable
ENV CATALINA_HOME /usr/share/tomcat8
RUN ln -s /var/lib/tomcat8/common $CATALINA_HOME/common && \
    ln -s /var/lib/tomcat8/server $CATALINA_HOME/server && \
    ln -s /var/lib/tomcat8/shared $CATALINA_HOME/shared && \
    ln -s /etc/tomcat8 $CATALINA_HOME/conf && \
    mkdir $CATALINA_HOME/temp && \
    mkdir $CATALINA_HOME/webapps && \
    mkdir $CATALINA_HOME/logs && \
    ln -s /var/lib/tomcat8/webapps/OCR4all_Web.war $CATALINA_HOME/webapps && \
    ln -s /var/lib/tomcat8/webapps/GTC_Web.war $CATALINA_HOME/webapps && \
    ln -s /var/lib/tomcat8/webapps/Larex.war $CATALINA_HOME/webapps
ENTRYPOINT [ "/usr/share/tomcat8/bin/catalina.sh", "run" ]
