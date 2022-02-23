#! /usr/bin/python3
import os.path
# Please contact https://androzoo.uni.lu to ask for your personal key.
key = ''
sha256 = 'FFD9F67DA6B83FAFA881F3758B24AA4AEB9945280AC4C26455AB676989FAF795'
filename = 'res/RQ2/test.apk'
os.system("curl -o " + filename + " -G -d apikey=" + key + " -d sha256=" + sha256 +
          " http://serval04.uni.lux/api/download")
with open('temp_result.csv') as myfile:
    content = myfile.readlines()
content = [x.strip() for x in content]
for row in content:
    sha256 = row.split(',')[0]
    filename = 'res/RQ2/apps/'+row.split(',')[1]+".apk"
    if not os.path.isfile(filename):
        os.system("curl -o " + filename + " -G -d apikey=" + key + " -d sha256=" + sha256 +
              " http://serval04.uni.lux/api/download")



