### Docker container
DOCKER_DIR="/home/bpoehland/Schreibtisch/BA/OCR4all/ocr4all_docker/"

mvn clean install -f pom.xml && \
sudo cp -f target/ocr4all.war ${DOCKER_DIR}/.
