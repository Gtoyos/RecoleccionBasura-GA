#!/bin/bash
#SBATCH --job-name=aeInstancia1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=39
#SBATCH --mem=70G
#SBATCH --time=48:00:00
#SBATCH --partition=besteffort
#SBATCH --qos=besteffort
#SBATCH --mail-type=ALL
#SBATCH --mail-user=guillermo.toyos@fing.edu.uy

source /etc/profile.d/modules.sh

export JAVA_HOME=~/lejava

cd ~/ae2021_practico/proyecto
~/lejava/bin/java -jar -Xms69G ae.jar 1000 16 exp 1
