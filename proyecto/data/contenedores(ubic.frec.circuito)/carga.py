#Script de conversión de la ubicación de los contenedores del sistema de coordenadas ITRF2000-UTM21S a latitud/longitud clásica
#La ubicacion de los contenedores utiliza IRGAS2000 ITRF2000, proyección UTM 21S. Para transformar estas coordenadas a latitud,longitud se utliza un servicio de conversión
#provisto por el host epsg.io

import requests
import numpy as np
import csv
import json
import time

HOPPER_URL= "http://localhost:8989/route?"
EPSG_URL = "https://epsg.io/trans"
INPUTFILE = "Contenedores_domiciliarios.csv"
TIMEOUTPUTFILE = "tiempo.csv"
DISTNACEOUTPUTFILE = "distancia.csv"
LATOUTPUT = "output.csv"

def getTripDistanceTime(x,y):
	res = requests.get(HOPPER_URL+"point="+str(x[1])+","+str(x[0])+"&point="+str(y[1])+","+str(y[0])+"&type=json&locale=en-US&key=&elevation=false&profile=car").json()
	print("GRAPHHOPPER RESULT")
	print((res["paths"][0]["distance"],res["paths"][0]["time"]))
	return (res["paths"][0]["distance"],res["paths"][0]["time"])

def convert(a, b):
	res = requests.get(EPSG_URL+"?x="+str(a)+"&y="+str(b)+"&s_srs=31981&t_srs=4326&callback=_callbacks_._2kw2x8top")
	ans = json.loads(res.text[23:-1])
	print((ans["x"],ans["y"]))
	return (ans["x"],ans["y"])

def main():
	contenedores=0
	with open(INPUTFILE) as csvfile:
		reader = csv.DictReader(csvfile, delimiter=';', quotechar=';')
		for row in reader:
			contenedores+=1
	
	tiempo = np.zeros((contenedores,contenedores))
	distancia = np.zeros((contenedores,contenedores))
	latlong = []
	i=0
	with open(INPUTFILE) as csvfile:
		reader = csv.DictReader(csvfile, delimiter=';', quotechar=';')
		for row in reader:
			i+=1
			print("----"+str(i)+"----")
			res = convert(row['X'], row['Y'])
			latlong.append(res)
			if(i%100==99):
				print("Zzz")
				time.sleep(10)
	intermedio = np.zeros((contenedores,2))
	for x in range(0,contenedores):
		intermedio[x][0] = latlong[x][0]
		intermedio[x][1] = latlong[x][1]
	np.savetxt(LATOUTPUT,intermedio,delimiter=",")

	for x in range(0,contenedores):
		for y in range(0,contenedores):
				td = getTripDistanceTime(latlong[x],latlong[y])
				distancia[x][y] = td[0]
				tiempo[x][y] = td[1]
	np.savetxt(TIMEOUTPUTFILE,tiempo,delimiter=",")
	np.savetxt(DISTNACEOUTPUTFILE,distancia,delimiter=",")


if __name__ == "__main__":
	main()

