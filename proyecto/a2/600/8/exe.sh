#!/bin/bash
#SBATCH --job-name=aeInstancia1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=39
#SBATCH --mem=100G
#SBATCH --time=48:00:00
#SBATCH --partition=besteffort
#SBATCH --qos=besteffort
#SBATCH --mail-type=ALL
#SBATCH --mail-user=guillermo.toyos@fing.edu.uy

source /etc/profile.d/modules.sh

export JAVA_HOME=~/lejava

cd ~/ae2021_practico/proyecto/a/600/8

for i in `seq 1 20`;
do
	~/lejava/bin/java -jar -Xms69G -Xmx100G ae.jar 100000 8
done