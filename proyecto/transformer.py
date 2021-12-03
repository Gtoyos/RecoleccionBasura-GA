import numpy as np

N = 50

#r1 = np.loadtxt(open("data\\ContenedoresGraphHopper\\tiempo.csv", "r"), delimiter=",")
#r2 = np.loadtxt(open("data\\ContenedoresGraphHopper\\distancia.csv", "r"), delimiter=",")

#r1 = r1[:N][:N]
#r2 = r2[:N][:N]
#np.savetxt("i50/tiempoContenedores.csv",r1,delimiter=",")
#np.savetxt("i50/distanciaContenedores.csv",r2,delimiter=",")

#r3 = np.loadtxt(open("data\\coordenadasContenedores.csv", "r"), delimiter=",")

r4 = np.loadtxt(open("data\\ContenedoresGraphHopper\\tiempoHaciaStartpoint.csv", "r"), delimiter=",")
r5 = np.loadtxt(open("data\\ContenedoresGraphHopper\\tiempoDesdeStartpoint.csv", "r"), delimiter=",")
r6 = np.loadtxt(open("data\\ContenedoresGraphHopper\\distanciaHaciaStartpoint.csv", "r"), delimiter=",")
r7 = np.loadtxt(open("data\\ContenedoresGraphHopper\\distanciaDesdeStartpoint.csv", "r"), delimiter=",")

r4 = r4[:N].transpose()
r5 = r5[:N].transpose()
r6 = r6[:N].transpose()
r7 = r7[:N].transpose()

#np.savetxt("i50/ubicacionContenedores.csv",r3,delimiter=",")
np.savetxt("i50/tiempoHaciaStartpoint.csv",r4,delimiter=",",newline=',')
np.savetxt("i50/tiempoDesdeStartpoint.csv",r5,delimiter=",",newline=',')
np.savetxt("i50/distanciaHaciaStartpoint.csv",r6,delimiter=",",newline=',')
np.savetxt("i50/distanciaDesdeStartpoint.csv",r7,delimiter=",",newline=',')
