
# topAPKs.py
## To find top N apps in GooglePlay.The temp_downloads.csv documents partially the apps with at least 100 million+ downloads, the result.csv documents apps with at least 100 million+ downloads. This is because GooglePlay only gives a range of total downloads, instead of accurate number of downloads.

# download.py
## To download apk given sha from AndroZoo. 

# lineage.py
## 

# implication.py
## To find inner usage of UE-APIs in apks



# Procedure RQ2
## latest.csv --(topAPKs.py)--> temp_result.csv + result.csv (1)--(download.py)--> apps/*.apk 
##                                                           (2)--(lineage.py)--> ue/*.txt + all/*.txt 

# Implication
## Implication is for [usage to remember] --(implication.py)--> 