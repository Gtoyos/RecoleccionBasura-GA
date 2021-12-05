#!/bin/bash
#SBATCH --job-name=aeInstancia3
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=75
#SBATCH --mem=100G
#SBATCH --time=48:00:00
#SBATCH --partition=normal
#SBATCH --qos=normal
#SBATCH --mail-type=ALL
#SBATCH --mail-user=guillermo.toyos@fing.edu.uy

source /etc/profile.d/modules.sh

export JAVA_HOME=~/lejava

cd ~/ae2021_practico/proyecto
~/lejava/bin/java -jar -Xms100G ae.jar 1000 74 exp 3