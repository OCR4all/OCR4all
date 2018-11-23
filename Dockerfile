# Base Image
FROM ubuntu:18.04
ENV DEBIAN_FRONTEND=noninteractive

# Enable Networking on port 8080 (Tomcat)
EXPOSE 8080
# Enable Networking on port 5000 (Flask)
EXPOSE 5000

# Installing dependencies and deleting cache
RUN apt-get update && apt-get install -y \
    locales \
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
    python3-setuptools \
    python3-pip \
    supervisor \
&& rm -rf /var/lib/apt/lists/*

# Set the locale
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Put supervisor process manager configuration to container
RUN wget -P /etc/supervisor/conf.d https://gitlab2.informatik.uni-wuerzburg.de/chr58bk/OCR4all_Web/raw/development/supervisord.conf

# Repository
RUN cd /opt && git clone -b development --recurse-submodules https://gitlab2.informatik.uni-wuerzburg.de/chr58bk/OCR4all_Web.git

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
    python2.7 setup.py install

# Make all ocropy scripts available to JAVA environment
RUN for OCR_SCRIPT in `cd /usr/local/bin && ls ocropus-*`; \
        do ln -s /usr/local/bin/$OCR_SCRIPT /bin/$OCR_SCRIPT; \
    done

# Install tensorflow
RUN pip3 install --upgrade tensorflow

# Install calamari
RUN cd /opt/OCR4all_Web/src/main/resources/calamari && \
    python3 setup.py install

# Make all calamari scripts available to JAVA environment
RUN for CALAMARI_SCRIPT in `cd /usr/local/bin && ls calamari-*`; \
        do ln -s /usr/local/bin/$CALAMARI_SCRIPT /bin/$CALAMARI_SCRIPT; \
    done

# Make pagedir2pagexml.py available to JAVA environment
RUN ln -s /opt/OCR4all_Web/src/main/resources/pagedir2pagexml.py /bin/pagedir2pagexml.py

# Make pretrained CALAMARI models available to the project environment
RUN ln -s /opt/OCR4all_Web/src/main/resources/ocr4all_models/default /var/ocr4all/models/default/default; 

# Install nashi
RUN cd /opt/OCR4all_Web/src/main/resources/nashi/server && \
    python3 setup.py install && \
    python3 -c "from nashi.database import db_session,init_db; init_db(); db_session.commit()" && \
    echo 'BOOKS_DIR="/var/ocr4all/data/"\nIMAGE_SUBDIR="/PreProc/Gray/"' > nashi-config.py
ENV FLASK_APP nashi
ENV NASHI_SETTINGS /opt/OCR4all_Web/src/main/resources/nashi/server/nashi-config.py
ENV DATABASE_URL sqlite:////opt/OCR4all_Web/src/main/resources/nashi/server/test.db

# Force tomcat to use java 8
RUN rm /usr/lib/jvm/default-java && \
    ln -s /usr/lib/jvm/java-1.8.0-openjdk-amd64 /usr/lib/jvm/default-java && \
    update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

# Solve Tomcat issues with Ubuntu
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

# Start processes when container is started
ENTRYPOINT [ "/usr/bin/supervisord" ]
