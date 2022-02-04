from multiprocessing import Pool
import os
from time import sleep

def afueraModuleII(number):
    sleep(5)
    os.system("/usr/lib/jvm/java-11-openjdk-amd64/bin/java -jar target/multi-moduleII.jar")

if __name__ == "__main__":
    print("running as main")

    pool = Pool(processes=3)

    pool.map(afueraModuleII,[1,2,3])