#!/bin/bash

DB_IP=$(ctx target instance runtime_properties ip_address)
DB_NAME=$(ctx target node properties db_name)
DB_USER=$(ctx target node properties db_user)
DB_PASSWORD=$(ctx target node properties db_password)

ctx logger info "Write the wp-config.php file"

file=$(sudo find / -name 'wp-config-sample.php')
folder=$(dirname $file)
eval "cd $folder"

sudo cp wp-config-sample.php wp-config.php
sudo sed -i 's/database_name_here/'$DB_NAME'/' wp-config.php
sudo sed -i 's/username_here/'$DB_USER'/' wp-config.php
sudo sed -i 's/password_here/'$DB_PASSWORD'/' wp-config.php
sudo sed -i 's/localhost/'$DB_IP'/' wp-config.php