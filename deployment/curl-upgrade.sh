#!/bin/bash

set -euxo pipefail

curl --version
sudo apt remove curl
sudo apt purge curl
sudo apt-get update
sudo apt-get install -y libssl-dev autoconf libtool make
cd /usr/local/src
rm -rf curl*
sudo wget -v https://curl.haxx.se/download/curl-$VERSION.zip
sudo unzip curl-$VERSION.zip
cd curl-$VERSION
sudo ./buildconf
sudo ./configure --with-ssl
sudo make
sudo make install
sudo cp /usr/local/bin/curl /usr/bin/curl
sudo ldconfig
curl --version