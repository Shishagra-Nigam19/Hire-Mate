#!/bin/bash
# ===================================================================
# HireMate Automated Certbot TLS Certificate Initialization Script
# ===================================================================

if ! [ -x "$(command -v docker-compose)" ]; then
  echo 'Error: docker-compose is not installed.' >&2
  exit 1
fi

domains=(hiremate-api.com www.hiremate-api.com)
rsa_key_size=4096
data_path="./nginx/ssl"
email="admin@hiremate.com"
staging=0 # Set to 1 for testing certbot rate limits

if [ -d "$data_path" ]; then
  read -p "Existing SSL data found for $domains. Continue and replace? (y/N) " decision
  if [ "$decision" != "Y" ] && [ "$decision" != "y" ]; then
    exit
  fi
fi

mkdir -p "$data_path/live/$domains"

echo "--> Requesting Let's Encrypt SSL certificate for $domains..."
docker run --rm -v "$data_path:/etc/letsencrypt" -v "./certbot/www:/var/www/certbot" \
  certbot/certbot certonly --webroot -w /var/www/certbot \
    --email $email \
    -d ${domains[0]} -d ${domains[1]} \
    --rsa-key-size $rsa_key_size \
    --agree-tos \
    --force-renewal

echo "--> SSL Certificates issued successfully!"
