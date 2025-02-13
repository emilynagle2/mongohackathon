// mongo-init.js
db = db.getSiblingDB('mydatabase');

// Create collections
db.createCollection('customers');
db.createCollection('merchants');
db.createCollection('transactions');