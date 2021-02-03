#! /usr/bin/python3
from google_play_scraper import app
from google_play_scraper.exceptions import NotFoundError
import csv

result = app(
    'com.nianticlabs.pokemongo',
    lang='en', # defaults to 'en'
    country='us' # defaults to 'us'
)
print(result["installs"])
filename = "latest.csv"
with open(filename) as f:
    content = f.readlines()
content = [x.strip() for x in content]
most_downloads = []
all_downloads = []
for row in reversed(content):
    if len(most_downloads) > 1000:
        break
    data = row.split(",")
    packageName = data[5].replace('"', '')
    contains = False
    for track in all_downloads:
        if track.split(',')[1] == packageName:
            contains = True
            break
    if contains:
        continue
    sha256 = data[0]
    store = data[len(data)-1]
    if store == "play.google.com":
        try:
            result = app(packageName)
            ins = result["installs"]
            if ins == '100,000,000+' or ins == '500,000,000+' or ins == '1,000,000,000+' or ins == '5,000,000,000+':
                line = sha256+','+packageName+','+ins
                most_downloads.append(line)
                print(packageName)
                print(ins)
                with open('temp_result.csv', 'a') as temp:
                    temp.write(line+'\n')
            else:
                line = sha256+','+packageName+','+ins
                all_downloads.append(line)
                print(packageName)
                print(ins)
        except:
            pass
with open('result.csv', 'w') as myfile:
    myfile.writelines('%s\n' % track for track in most_downloads)

