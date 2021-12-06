
#población | p.cruzamiento | p.mutacion | ejecucion | fitness | distancia | tiempo | desborde | t.computo

i2 = []

with open("results/inst2.txt", 'r') as file:
    for line in file:
        s = line.rstrip().split(" ")
        if(len(s)==9):
            i2.append(s)

pp = [50,100,150]
mm = [0.0020833334, 0.001, 0.01]
cc = [0.6, 0.75, 0.95]

for c in cc:
    for p in pp:
        for m in mm:
            count = 0
            for e in i2:
                if(e[0]==str(p) and e[1]==str(c) and e[2]==str(m)):
                    count+=1
            print(c,p,m,count)