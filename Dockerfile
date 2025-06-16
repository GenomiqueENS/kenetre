FROM ubuntu:24.04

ARG VERSION=0.34
ARG BUILD_PACKAGES="wget apt-transport-https"
ARG DEBIAN_FRONTEND=noninteractive


RUN apt update && \
    apt install --yes $BUILD_PACKAGES openjdk-11-jre-headless && \
    cd /usr/local/bin && \
    wget -q  https://github.com/jourdren/kenetre-bidon3/releases/download/v${VERSION}/kenetre-bin-${VERSION}-jar-with-dependencies.jar && \
    ln -s kenetre-bin-*.jar kenetre.jar && \
    echo "#!/bin/bash\njava -jar \"/usr/local/bin/kenetre.jar\" \"\$@\"" > kenetre.sh && \
    chmod +x kenetre.sh && \
    apt remove --purge --yes $BUILD_PACKAGES && \
    apt autoremove --purge --yes && \
    apt clean && \
    rm -rf /var/lib/apt/lists/*
