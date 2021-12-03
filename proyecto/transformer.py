import numpy as np

S= 6000
N = 7000
FOLDER = "i1000"

r1 = np.loadtxt(open("data\\ContenedoresGraphHopper\\tiempo.csv", "r"), delimiter=",")
r2 = np.loadtxt(open("data\\ContenedoresGraphHopper\\distancia.csv", "r"), delimiter=",")

r1 = r1[S:N][:,S:N]
r2 = r2[S:N][:,S:N]
np.savetxt(FOLDER +"/tiempoContenedores.csv",r1,delimiter=",")
np.savetxt(FOLDER +"/distanciaContenedores.csv",r2,delimiter=",")

r3 = np.loadtxt(open("data\\coordenadasContenedores.csv", "r"), delimiter=",")

r4 = np.loadtxt(open("data\\ContenedoresGraphHopper\\tiempoHaciaStartpoint.csv", "r"), delimiter=",")
r5 = np.loadtxt(open("data\\ContenedoresGraphHopper\\tiempoDesdeStartpoint.csv", "r"), delimiter=",")
r6 = np.loadtxt(open("data\\ContenedoresGraphHopper\\distanciaHaciaStartpoint.csv", "r"), delimiter=",")
r7 = np.loadtxt(open("data\\ContenedoresGraphHopper\\distanciaDesdeStartpoint.csv", "r"), delimiter=",")

r4 = r4[S:N].transpose()
r5 = r5[S:N].transpose()
r6 = r6[S:N].transpose()
r7 = r7[S:N].transpose()

np.savetxt(FOLDER +"/ubicacionContenedores.csv",r3,delimiter=",")
np.savetxt(FOLDER +"/tiempoHaciaStartpoint.csv",r4,delimiter=",",newline=',')
np.savetxt(FOLDER +"/tiempoDesdeStartpoint.csv",r5,delimiter=",",newline=',')
np.savetxt(FOLDER +"/distanciaHaciaStartpoint.csv",r6,delimiter=",",newline=',')
np.savetxt(FOLDER +"/distanciaDesdeStartpoint.csv",r7,delimiter=",",newline=',')
