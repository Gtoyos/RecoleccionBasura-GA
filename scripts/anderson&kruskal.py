
import scipy.stats as ss
import scipy.misc as smp
from PIL import Image
import numpy as np
import sys

#población | p.cruzamiento | p.mutacion | ejecucion | fitness | distancia | tiempo | desborde | t.computo 
#población | p.cruzamiento | p.mutacion | ejecucion | fitness | distancia | tiempo | desborde | t.computo | avgFitness | bestFitness | avgComputingTime | Anderson | Kolmogorov
# 0             1               2           3           4           5       6           7           8           9           10              11              12          13

i2 = []

with open("results/t1/inst3.txt", 'r') as file:
    for line in file:
        s = line.rstrip().split(" ")
        if(len(s)==9):
            i2.append(s)

pp = [50,100,150]
mm = [0.004166667, 0.001, 0.01]
cc = [0.6, 0.75, 0.95]

#key: pp-cc-mm
datasets = {}
datafit = {}
dataval = {}
for c in cc:
    for p in pp:
        for m in mm:
            datasets[str(p)+"-"+str(c)+"-"+str(m)] = []
            datafit[str(p)+"-"+str(c)+"-"+str(m)] = []
            dataval[str(p)+"-"+str(c)+"-"+str(m)] = []

for c in cc:
    for p in pp:
        for m in mm:
            for e in i2:
                if(e[0]==str(p) and e[1]==str(c) and e[2]==str(m)):
                    #datasets[str(p)+"-"+str(c)+"-"+str(m)].append([e[4],e[5],e[6],e[7],e[8]])
                    datafit[str(p)+"-"+str(c)+"-"+str(m)].append(float(e[4]))

for c in cc:
    for p in pp:
        for m in mm:
            print(str(p)+"-"+str(c)+"-"+str(m)+" | ",len(datafit[str(p)+"-"+str(c)+"-"+str(m)]))

for c in cc:
    for p in pp:
        for m in mm:
            s = 0
            b = -1e299
            n = 0
            for v in datafit[str(p)+"-"+str(c)+"-"+str(m)]:
                s=s+v
                n=n+1
                if(v>b):
                    b = v
            #print(datafit[str(p)+"-"+str(c)+"-"+str(m)])
            print(ss.anderson(datafit[str(p)+"-"+str(c)+"-"+str(m)], dist='norm'))
            dataval[str(p)+"-"+str(c)+"-"+str(m)].append(s/n)
            dataval[str(p)+"-"+str(c)+"-"+str(m)].append(b)
#print(dataval)
algoritmos = []
for c in cc:
    for p in pp:
        for m in mm:
            algoritmos.append(str(p)+"-"+str(c)+"-"+str(m))

l=len(algoritmos)
resultados = np.zeros((l,l))
for i in range(0,l):
    for j in range(0,l):
        resultados[i][j] = ss.kruskal(datafit[algoritmos[i]],datafit[algoritmos[j]]).pvalue
#print(resultados)
data = np.zeros( (l,l,3), dtype=np.uint8 )


for i in range(0,l):
    for j in range(0,l):
        if(resultados[i][j]<0.05):
            data[i,j] = [230,0,0] #Red
        else:
            data[i,j] = [0,0,230] #blue



img = Image.fromarray( data )       # Create a PIL image
img.show()                      # View in default viewer

Q = {}
for i in range(0,l):
    s = 0
    for j in range(0,l):
        if(resultados[i][j]<0.05):
            s+=1
    Q[algoritmos[i]] = [s,dataval[algoritmos[i]][0]]

Q = {k: v for k, v in sorted(Q.items(), key=lambda item: item[1][1])}
#print(Q)