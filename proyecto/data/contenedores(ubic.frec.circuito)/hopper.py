#Script de generación de la matrices de tiempo y distancia.
#Se utiliza la API de Graphopper para generar los tiempos y distancias.
#Esta toma en cuenta el mapa de calles de la ciudad de montevideo para calcular la mejor ruta. 

import requests
import numpy as np
import csv
import json
import time
import threading as th
import random
from random import randrange

HOPPER_URL=[ "http://localhost:8989/route?","http://localhost:9998/route?","http://localhost:9001/route?","http://localhost:9004/route?","http://localhost:9006/route?","http://localhost:9008/route?","http://localhost:9010/route?",
"http://localhost:9012/route?","http://localhost:9015/route?","http://localhost:9017/route?"
]

SESSIONS = [requests.Session(),requests.Session(),requests.Session(),requests.Session(),
			requests.Session(),requests.Session(),requests.Session(),requests.Session(),
			requests.Session(),requests.Session(),requests.Session(),requests.Session(),
			requests.Session()]
INPUTFILE = "latlong.csv"
TIMEOUTPUTFILE = "tiempoV5800-6000.csv"
DISTNACEOUTPUTFILE = "distanciaV5800-6000.csv"
STARTROW = 5800
ENDROW = 6000
distancia = []
tiempo = []
counter = 0

def getTripDistanceTime(x,y):
	k = randrange(10)
	res = SESSIONS[k].get(HOPPER_URL[k]+"point="+str(x[1])+","+str(x[0])+"&point="+str(y[1])+","+str(y[0])+"&type=json&locale=en-US&key=&elevation=false&profile=car").json()
	return (res["paths"][0]["distance"],res["paths"][0]["time"])

def hopperLine(N,distI,timeI,latlong):
	global counter
	for x in range(0,len(distI)):
		while(1):
			try:
				td = getTripDistanceTime(latlong[x],latlong[N])
				break
			except Exception as e:
				print(str(e))
				pass
		counter+=1
		if(counter%1000==0):
			print((counter/(len(timeI)*(ENDROW-STARTROW))*100,"% |",td))
		distI[x] = td[0]
		timeI[x]= td[1]

def main():
	contenedores=0
	with open(INPUTFILE) as csvfile:
		reader = csv.DictReader(csvfile, delimiter=',', quotechar=',')
		for row in reader:
			contenedores+=1
	

	latlong = []
	with open(INPUTFILE) as csvfile:
		reader = csv.DictReader(csvfile, delimiter=',', quotechar=',')
		for row in reader:
			res = (row['X'], row['Y'])
			resf=(res[0].replace(',', '.'),res[1].replace(',', '.'))
			resf = (float(resf[0]),float(resf[1]))
			latlong.append(resf)

	hilos = []
	distRows = []
	timeRows = []
	for i in range(STARTROW,ENDROW):
		distI = np.zeros((contenedores))
		timeI = np.zeros((contenedores))
		distRows.append(distI)
		timeRows.append(timeI)
		h = th.Thread(target=hopperLine,args=(i,distI,timeI,latlong))
		h.start()
		hilos.append(h)
		time.sleep(random.uniform(0,0.1)) #Para que no arranquen todos los threads al mismo tiempo.
	for h in hilos:
		h.join()
	tiempo = np.zeros((0,contenedores))
	distancia = np.zeros((0,contenedores))

	for t in timeRows:
		tiempo = np.insert(tiempo,tiempo.shape[0],t,axis=0)
	for d in distRows:
		distancia = np.insert(distancia,distancia.shape[0],d,axis=0)

	np.savetxt(TIMEOUTPUTFILE,tiempo,delimiter=",")
	np.savetxt(DISTNACEOUTPUTFILE,distancia,delimiter=",")


if __name__ == "__main__":
	main()

