from socket import *
import RPi.GPIO as GPIO
import time
import picamera
import cv2
import boto3, requests
import os, sys, re
from Tkinter import*
import netifaces as ni
import shutil


# get current raspberry pi's ip address
ni.ifaddresses('wlan0')
Rasp_ip=ni.ifaddresses('wlan0')[ni.AF_INET][0]['addr']

# sensor set
counter=0
GPIO.setmode(GPIO.BCM)
GPIO.setup(17, GPIO.IN) # motion sensor
GPIO.setup(4, GPIO.OUT) # led

# camera preview set
camera = picamera.PiCamera()
camera.resolution=(360, 270)
camera.start_preview(fullscreen=False, window=(0,40,360,270))

# photo path
capture_image_file = "/var/www/html/Capture_face/"
path_dir = '/var/www/html/Received_image/'
unpay_file = "/var/www/html/Unpay_face/"


# similarity
confidence=0
IP_address=""

# python GUI setting
unpay_num=0

root = Tk()
root.title("Smart Bus System")
root.geometry("600x600+600+40")

Label(root, text="Payment", pady=30, font="Verdana 20 bold").place(x=230, y=10)
Label(root, text='- - - - ', pady=10, font="Verdana 15").place(x=270, y=100)

result_img1 = PhotoImage(file="x-icon.png")
result_img2 = PhotoImage(file="o-icon.png")
result_img3 = PhotoImage(file="main-icon.png")

Img = Label(root, image=result_img3).place(x=170, y=200)
Label(root, text="Unpay Num", pady=5, font="Verdana 10 bold").place(x=10, y=500)
Label(root, text=str(unpay_num), pady=5, font="Verdana 10").place(x=15, y=520)


def motionSensor(channel) :
	GPIO.output(4, GPIO.LOW)

	# PIR Sensor
	if GPIO.input(channel) == 1 :
		global counter
		counter += 1

		# camera capture
		camera.capture(capture_image_file+str(counter)+".jpg")

		print("Motion detected! [{0}]".format(counter))

		# amazon aws - rekognition
		# Picamera image
		capture_image = requests.get('http://'+Rasp_ip+'/Capture_face/'+str(counter)+'.jpg')
		source_response_content = capture_image.content

		# android image
		IP_address=""
		file_list = os.listdir(path_dir)
		for img in file_list:
			android_image = requests.get('http://'+Rasp_ip+'/Received_image/'+img)
			target_response_content = android_image.content

			# compare android image to captured image
			rekognition_response = rekognition.compare_faces(
	        		SourceImage={
                			'Bytes': source_response_content
	       			},
        			TargetImage={
                			'Bytes': target_response_content
       				},
        			SimilarityThreshold=70
			)

			for face_match in rekognition_response['FaceMatches']:
       				confidence = face_match['Similarity']
				IP_address = img.replace(".jpg", "")
				confidence=0

		# if face is same, TCP socket
                client_sock = socket(AF_INET, SOCK_STREAM)
                try:
                        client_sock.connect((IP_address, 8888))
                        client_sock.send('PayOK\n'.encode())
                        print("Send : PayOK")

                        data = client_sock.recv(1024)
                        print("Received : "+data.encode())

			# pay complete GUI show
			Label(root, text=data.encode(), pady=10, font="Verdana 15").place(x=270, y=100)
			Label(root, image=result_img2).place(x=170, y=200)
			time.sleep(1)

                except Exception as e:
                        print('Cant connected...')

			# copy capture face and store 'unpay' directory
                        shutil.copy(capture_image_file+str(counter)+'.jpg', unpay_file+'stop1_'+str(counter)+'.jpg')

			# didn't pay GUI show
			global unpay_num
			unpay_num += 1
			Label(root, text='- - - - ', pady=10, font="Verdana 15").place(x=270, y=100)
			Label(root, image=result_img1).place(x=170, y=200)
			Label(root, text=str(unpay_num), pady=5, font="Verdana 10").place(x=15, y=520)
			time.sleep(1)
	else:
		Label(root, text='- - - - ', pady=10, font="Verdana 15").place(x=270, y=100)
                Label(root, image=result_img3).place(x=170, y=200)


GPIO.add_event_detect(17, GPIO.BOTH, callback=motionSensor, bouncetime=100)

try :
	# set aws
        session = boto3.Session(profile_name='default')
        rekognition = session.client('rekognition')

	while True :
		time.sleep(0.5)
		root.mainloop()
except KeyboardInterrupt :
	print("\nInterrupted!")
finally :
	GPIO.cleanup()
