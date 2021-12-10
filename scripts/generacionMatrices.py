# Generacion de las matrices tiempo/distancia con los datos obtenidos

import csv
import numpy as np
import matplotlib.pyplot as plt

reader = csv.reader(open("t0.csv", "r"), delimiter=",")
x = list(reader)
rd = np.array(x).astype("float")
cols = rd.shape[1] #11556

bigt = np.zeros((0,cols))
bigd = np.zeros((0,cols))

for i in range(0,18):
    print("Reading file",i)
    rt = np.loadtxt(open("t"+str(i)+".csv", "r",encoding='cp850'), delimiter=",")
    rd = np.loadtxt(open("d"+str(i)+".csv", "r",encoding='cp850'), delimiter=",")
    #reader = csv.reader(open("t"+str(i)+".csv", "r"), delimiter=",")
    #x = list(reader)
    #rt = np.array(x).astype("float")
    #reader = csv.reader(open("d"+str(i)+".csv", "r"), delimiter=",")
    #x = list(reader)
    #rd = np.array(x).astype("float")

    bigt = np.insert(bigt,bigt.shape[0],rt,axis=0)
    bigd = np.insert(bigd,bigd.shape[0],rd,axis=0)


np.savetxt("tiempo.csv",bigt,delimiter=",")
np.savetxt("distancia.csv",bigd,delimiter=",")

plt.imshow(bigt, cmap='viridis')
plt.colorbar()
plt.show()
plt.savefig("heatT.pdf")


plt.imshow(bigd, cmap='viridis')
plt.colorbar()
plt.show()
plt.savefig("heatD.pdf")