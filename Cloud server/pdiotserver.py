from flask import Flask
from flask import request
import os.path
import time
import random

from flask import jsonify
import ast
import uuid
import string
import names
from threading import Lock
from time import gmtime, strftime
import os
import numpy as np
import csv
import tensorflow as tf

app = Flask(__name__)

########## Initialisation

# def initialise_model_buffer():
#     activity_data_Respeck = np.zeros([1,50, 6], dtype = np.float32)
#     activity_data_Thingy = np.zeros([1,50, 6], dtype = np.float32)
#     np.save("./Model/buffer_Respeck.npy", activity_data_Respeck)
#     np.save("./Model/buffer_Thingy.npy", activity_data_Thingy)

# def initialise_user_data_buffer():
#     activity_in_seconds = np.zeros([14], dtype = np.float32)
#     np.save("./Model/buffer_Respeck.npy", activity_data_Respeck)
#     np.save("./Model/buffer_Thingy.npy", activity_data_Thingy)


########## Model helper



# def push_a_piece_of_data_Thingy(accelX,accelY,accelZ,gyroX,gyroY,gyroZ): 
#     activity_data_Thingy = np.load("./Model/buffer_Thingy.npy")
#     for i in range(0,49):
#         for j in range(0,6):
#             activity_data_Thingy[0][i][j] = activity_data_Thingy[0][i+1][j]

#     activity_data_Thingy[0][49][0] = accelX
#     activity_data_Thingy[0][49][1] = accelY
#     activity_data_Thingy[0][49][2] = accelZ
#     activity_data_Thingy[0][49][3] = gyroX
#     activity_data_Thingy[0][49][4] = gyroY
#     activity_data_Thingy[0][49][5] = gyroZ

#     np.save("./Model/buffer_Thingy.npy", activity_data_Thingy)

# def push_a_piece_of_data_Respeck(accelX,accelY,accelZ,gyroX,gyroY,gyroZ): 
#     activity_data_Respeck = np.load("./Model/buffer_Respeck.npy")
#     for i in range(0,49):
#         for j in range(0,6):
#             activity_data_Respeck[0][i][j] = activity_data_Respeck[0][i+1][j]

#     activity_data_Respeck[0][49][0] = accelX
#     activity_data_Respeck[0][49][1] = accelY
#     activity_data_Respeck[0][49][2] = accelZ
#     activity_data_Respeck[0][49][3] = gyroX
#     activity_data_Respeck[0][49][4] = gyroY
#     activity_data_Respeck[0][49][5] = gyroZ

#     np.save("./Model/buffer_Respeck.npy", activity_data_Respeck)

def load_labels(path): # Read the labels from the text file as a Python list.
  with open(path, 'r') as f:
    return [line.strip() for i, line in enumerate(f.readlines())]
  
# def set_input_tensor(interpreter, activity):
#   tensor_index = interpreter.get_input_details()[0]['index']
#   input_tensor = interpreter.tensor(tensor_index)()[0]
#   input_tensor[:, :] = activity

# def classify_activity(interpreter, activity, top_k=1):
#     input_details = interpreter_Respeck.get_input_details()
#     output_details = interpreter_Respeck.get_output_details()
#     input_shape = input_details[0]['shape']
#     input_data = activity
#     interpreter_Respeck.set_tensor(input_details[0]['index'], input_data)
#     interpreter_Respeck.invoke()
#     output_data = interpreter_Respeck.get_tensor(output_details[0]['index'])
#     return (output_data.argmax(), output_data.max())



# def classify_activity0(interpreter, activity, top_k=1):
#   set_input_tensor(interpreter, activity)

#   interpreter.invoke()
#   output_details = interpreter.get_output_details()[0]
#   output = np.squeeze(interpreter.get_tensor(output_details['index']))

#   scale, zero_point = output_details['quantization']
#   output = scale * (output - zero_point)

#   ordered = np.argpartition(-output, top_k)
#   return [(i, output[i]) for i in ordered[:top_k]][0]

########## Model interpreter

model_Respeck_path = "./Model/Model-for-Respeck.tflite"
model_Thingy_path = "./Model/Model-for-Thingy.tflite"

label_Thingy_path = "./Model/labelsT.txt"
label_Respeck_path = "./Model/labelsR.txt"

labels_Respeck = load_labels(label_Respeck_path)
labels_Thingy = load_labels(label_Thingy_path)

# interpreter_Respeck = tf.lite.Interpreter(model_path=model_Respeck_path)
# interpreter_Respeck.allocate_tensors()

# interpreter_Thingy = tf.lite.Interpreter(model_path=model_Thingy_path)
# interpreter_Thingy.allocate_tensors()

########## globals

user_login_info_file = 'user_info.csv'
open(user_login_info_file, 'a+').close()
#--userID--userPassword--
#corresponding history file:
#   userID.csv

#user_history_info_file = 'orders.csv'

#for atomic updates
#lock = Lock()




########## API info

@app.route('/')
def hello_world():
    test=1
    return 'hello world!'

@app.route('/test')
def test():
    test+=1
    return test

########### helper functions below
def idVerify(id, password):
    with open(user_login_info_file) as f:
        content = f.readlines()
        content = [item.rstrip('\n') for item in content]
        for line in content:
            data = line.split(',')
            if data[0] == id and data[1] == password:
                return True
    return False

def signupVerify(id, password):
    with open(user_login_info_file) as f:
        content = f.readlines()
        content = [item.rstrip('\n') for item in content]
        if len(content)==0:
            return True
        for line in content:
            data = line.split(',')
            if data[0] == id:
                return False
    return True


########### endpoints below

@app.route('/signup')
def signup():
    if 'ID' in request.args and 'password' in request.args:
        if signupVerify(request.args.get('ID'),request.args.get('password')):
            with open(user_login_info_file,'a+') as f:
                f.write(str(request.args.get('ID'))+","+str(request.args.get('password'))+"\n")
                
                history_file = request.args.get('ID')+".csv"
                open(history_file, 'a+').close()

                return "Successfully signed up."
        else:
            return "ID is registered already"
    return "Not valid registration"

@app.route('/login')
def login():
    if 'ID' in request.args and 'password' in request.args:
        if idVerify(request.args.get('ID'),request.args.get('password')):
            #initialise_model_buffer()

            history_file = request.args.get('ID')+".csv"
            open(history_file, 'a+').close()

            return "Login successful."
        else:
            return "User doesn't exist or password is incorrect."

@app.route('/getStats')
def get_stats():
    if 'ID' in request.args and 'password' in request.args:
        if idVerify(request.args.get('ID'),request.args.get('password')):
            history_file = request.args.get('ID')+".csv"
            if os.path.isfile(history_file):

                time_until = int(request.args.get('timestamp'))

                with open(history_file,'r') as f:
                    reader = csv.reader(f)
                    rows = [row for row in reader]
                data=np.array(rows)
                for i in data:
                    if int(i[1])<time_until: #data too old
                        i[0] = "0"
                        i[1] = "0"
                stat = np.array([0 for i in range(14)])
                for i in range(len(labels_Respeck)):
                    for j in range(len(data)):
                        if str(data[j][0])==labels_Respeck[i]:
                            stat[i]+=1
                result = ""
                for i in stat:
                    result+=str(i)
                    result+=","
                print (result)
                return result[0:-1]
            else:
                return "No history found, please update"
        else:
            return "User doesn't exist or password is incorrect."


@app.route('/updateHistory')
def update_history():
    if 'ID' in request.args and 'password' in request.args:
        if idVerify(request.args.get('ID'),request.args.get('password')):
            history_file = request.args.get('ID')+".csv"
            if os.path.isfile(history_file):
                with open(history_file, 'a+') as f:
                    f.write(request.args.get('activity')+","+request.args.get('timestamp')+"\n")
                return "History updated."
            else:
                open(history_file, 'a+').close()
                with open(history_file, 'a+') as f:
                    f.write(request.args.get('activity')+","+request.args.get('timestamp')+"\n")
                return "History updated."
        else:
            return "User doesn't exist or password is incorrect."

@app.route('/clearHistory')
def clear_history():
    if 'ID' in request.args and 'password' in request.args:
        if idVerify(request.args.get('ID'),request.args.get('password')):
            history_file = request.args.get('ID')+".csv"
            if os.path.isfile(history_file):
                os.remove(history_file)
                return "History cleared."
            else:
                return "History cleared."
        else:
            return "User doesn't exist or password is incorrect."

# @app.route('/push_Respeck')
# def push_Respeck():
#     #print(activity_data_Respeck)
#     if 'ID' in request.args and 'password' in request.args:
#         if idVerify(request.args.get('ID'),request.args.get('password')):
#             accelX = np.float32(request.args.get('accelX'))
#             accelY = np.float32(request.args.get('accelY'))
#             accelZ = np.float32(request.args.get('accelZ'))
#             gyroX = np.float32(request.args.get('gyroX'))
#             gyroY = np.float32(request.args.get('gyroY'))
#             gyroZ = np.float32(request.args.get('gyroZ'))
#             push_a_piece_of_data_Respeck(accelX,accelY,accelZ,gyroX,gyroY,gyroZ)
#             #activity_data_Respeck = np.load("./Model/buffer_Respeck.npy")
#             #print(activity_data_Respeck)
#             return "Successfully pushed."
            
#         else:
#             return "User doesn't exist or password is incorrect."

# @app.route('/push_Thingy')
# def push_Thingy():
#     if 'ID' in request.args and 'password' in request.args:
#         if idVerify(request.args.get('ID'),request.args.get('password')):
#             accelX = np.float32(request.args.get('accelX'))
#             accelY = np.float32(request.args.get('accelY'))
#             accelZ = np.float32(request.args.get('accelZ'))
#             gyroX = np.float32(request.args.get('gyroX'))
#             gyroY = np.float32(request.args.get('gyroY'))
#             gyroZ = np.float32(request.args.get('gyroZ'))
#             push_a_piece_of_data_Thingy(accelX,accelY,accelZ,gyroX,gyroY,gyroZ)
#             return "Successfully pushed."
#         else:
#             return "User doesn't exist or password is incorrect."

# @app.route('/getClassification')
# def getClassification():

#     if 'ID' in request.args and 'password' in request.args:
#         if idVerify(request.args.get('ID'),request.args.get('password')):
#             activity_data_Respeck = np.load("./Model/buffer_Respeck.npy")
#             label_id, prob = classify_activity(interpreter_Respeck, activity_data_Respeck)
#             classification_label = labels_Respeck[label_id]
#             #if classification_label == "Sitting" or classification_label == "Desk work" or classification_label == "Standing":
#                 #label_id, prob = classify_activity(interpreter_Thingy, activity_data_Thingy)
#                 #classification_label = labels_Thingy[label_id]
#             return classification_label

#         else:
#             return "User doesn't exist or password is incorrect."

if __name__ == '__main__':
    app.run(host= '0.0.0.0', port=8001, threaded=False, processes=10)



