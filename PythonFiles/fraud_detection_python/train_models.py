import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
from sklearn.preprocessing import LabelEncoder
from sklearn.enseemble import IsolationForest
import joblib
# Just boilerplate code - TODO


# load dataset
df = pd.read_csv('data.csv')

#feature selection and procession

# split data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
# train model
model = xgb.XGBClassifier()
model.fit(X_train, y_train)
# evaluate model
y_pred = model.predict(X_test)
accuracy = accuracy_score(y_test, y_pred)
print("Accuracy: %.2f%%" % (accuracy * 100.0))
# save model
joblib.dump(model, 'model.pkl')
