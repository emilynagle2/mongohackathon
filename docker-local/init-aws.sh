#!/bin/bash
echo "Initialising AWS services in LocalStack..."

# Wait for LocalStack to be ready
until aws --endpoint-url=http://localhost:4566 s3 ls; do
  echo "Waiting for LocalStack..."
  sleep 3
done

# Create S3 bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://my-bucket

# Create SQS queue
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name my-queue

# Create SNS topic
aws --endpoint-url=http://localhost:4566 sns create-topic --name my-topic

echo "AWS services initialised!"
