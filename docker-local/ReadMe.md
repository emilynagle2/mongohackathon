# Make the init script executable
chmod +x init-aws.sh

# Start all services
docker-compose up --build
