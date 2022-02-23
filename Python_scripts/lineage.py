#! /usr/bin/python3
import os.path
# Please contact https://androzoo.uni.lu to ask for your personal key.
key = 'b29928591d5e637f996c04a045ed839017471f92179052981e97cbd62461c2e6'
topApp = "res/RQ2/result.csv"
with open(topApp) as f:
    top = f.readlines()
tops = []
for row in top:
    row = row.split(',')[1]
    tops.append(row)
top100 = tops[:300]
print(top100)
filename = "res/RQ2/latest.csv"
with open(filename) as f:
    content = f.readlines()
# you may also want to remove whitespace characters like `\n` at the end of each line
content = [x.strip() for x in content]
startsmall = 0
for row in content:
    #if startsmall > 10:
    #    break
    data = row.split(',')
    packageName = data[5].replace('"', '')
    if packageName in top100:
        print(packageName)
        year = data[8].split('-')[0]
        print(year)
        if year in ['2014','2015','2016','2017','2018','2019','2020']:
            sha256 = data[0]
            filename = packageName+'-'+year
            print(filename)
            apkname = 'res/RQ2/appTemp/'+filename+".apk"
            ue_name = 'res/RQ2/ue/'+filename+'.apk.txt'
            all_name = 'res/RQ2/all/'+filename+'.apk.txt'
            if not os.path.isfile(ue_name):
                os.system("curl -o " + apkname + " -G -d apikey=" + key + " -d sha256=" + sha256 +
                  " http://serval04.uni.lux/api/download")
                os.system("java -jar target/Afuera-tool.jar "+apkname)
                os.system("rm "+apkname)
                startsmall += 1