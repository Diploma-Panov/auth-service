#!/bin/bash

aws ecr get-login-password --region eu-central-1 --profile default | docker login --username AWS --password-stdin 533267200006.dkr.ecr.eu-central-1.amazonaws.com

docker compose pull