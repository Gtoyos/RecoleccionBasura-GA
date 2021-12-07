import scipy.stats as ss
import sys
import time

from matplotlib import pyplot

#población | p.cruzamiento | p.mutacion | ejecucion | fitness | distancia | tiempo | desborde | t.computo 
#población | p.cruzamiento | p.mutacion | ejecucion | fitness | distancia | tiempo | desborde | t.computo | avgFitness | bestFitness | avgComputingTime | Anderson | Kolmogorov


i2 = []

with open("results/inst1.txt", 'r', encoding='latin-1') as file:
    for line in file:
        s = line.rstrip().split(" ")
        if(len(s)==9):
            i2.append(s)

pp = [50,100,150]
mm = [0.0020833334, 0.001, 0.01]
cc = [0.6, 0.75, 0.95]

#key: pp-cc-mm
datasets = {}
datafit = {}
for c in cc:
    for p in pp:
        for m in mm:
            datasets[str(p)+"-"+str(c)+"-"+str(m)] = []
            datafit[str(p)+"-"+str(c)+"-"+str(m)] = []


for c in cc:
    for p in pp:
        for m in mm:
            for e in i2:
                if(e[0]==str(p) and e[1]==str(c) and e[2]==str(m) and float(e[4])>500):
                    datasets[str(p)+"-"+str(c)+"-"+str(m)].append([e[4],e[5],e[6],e[7],e[8]])
                    datafit[str(p)+"-"+str(c)+"-"+str(m)].append(float(e[4]))

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
            if len(datafit[str(p)+"-"+str(c)+"-"+str(m)]) > 0 :
            	print(ss.anderson(datafit[str(p)+"-"+str(c)+"-"+str(m)], dist='norm'))
            	pyplot.hist(datafit[str(p)+"-"+str(c)+"-"+str(m)])
            	pyplot.show()
            #print(b)
            #print(s/n)
            #exit(1)
