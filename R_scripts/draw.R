#! /usr/bin/env Rscript
args <- commandArgs(trailingOnly=TRUE)
file_path <- paste(args[1],'apiPerexception.csv',sep="")
counts <- read.csv(file=file_path,nrows=10)
pdf(file="r_fig/api-exception.pdf")
## Find a range of y's that'll leave sufficient space above the tallest bar
ylim <- c(0, 1.25*max(counts$count))
par(mar=c(20, 0.0, 0.0, 0.0))
xx <- barplot(counts$count,axes=FALSE,width=(0.5)*9,ylim=ylim)
text(x = xx, y = counts$count, label = counts$count, pos = 3,offset=0.4, srt=0,cex = 1.8, col = "black")
text(x = xx, y = counts$count, label = counts$per, pos = 3,offset=1.9, srt=0,cex = 1.8, col = "red")
#text(x = xx, y = counts$count, label = counts$exception, pos = 2,xpd=TRUE, cex = 1.3, col = "black")
## cex is for font size, 
text(counts$type,x = xx,offset = -0.2,y = -15,cex = 2.2,srt = 90,xpd = TRUE,pos = 2 )
dev.off()

file_path <- paste(args[1],'signalerPerexception.csv',sep="")
counts <- read.csv(file=file_path,nrows=10)
pdf(file="r_fig/sig-exception.pdf")
## Find a range of y's that'll leave sufficient space above the tallest bar
ylim <- c(0, 1.25*max(counts$count))
par(mar=c(20, 0.0, 0.0, 0.0))
xx <- barplot(counts$count,axes=FALSE,width=(0.5)*9,ylim=ylim)
text(x = xx, y = counts$count, label = counts$count, pos = 3,offset=0.4, srt=0,cex = 1.8, col = "black")
text(x = xx, y = counts$count, label = counts$per, pos = 3,offset=1.9, srt=0,cex = 1.8, col = "red")
#text(x = xx, y = counts$count, label = counts$exception, pos = 2,xpd=TRUE, cex = 1.3, col = "black")
## cex is for font size, 
text(counts$type,x = xx,offset = -0.2,y = -15,cex = 2.2,srt = 90,xpd = TRUE,pos = 2 )
dev.off()

file_path <- paste(args[1],'apiPerpackage.csv',sep="")
counts <- read.csv(file=file_path,nrows=10)
pdf(file="r_fig/api-package.pdf")
## Find a range of y's that'll leave sufficient space above the tallest bar
ylim <- c(0, 1.25*max(counts$count))
par(mar=c(14,0.0, 0.0, 0.0))
xx <- barplot(counts$count,axes=FALSE,width=(0.5)*9,ylim=ylim)
text(x = xx, y = counts$count, label = counts$count, pos = 3,offset=0.4, srt=0,cex = 1.8, col = "black")
text(x = xx, y = counts$count, label = counts$per, pos = 3,offset=1.9, srt=0,cex = 1.8, col = "red")
#text(x = xx, y = counts$count, label = counts$exception, pos = 2,xpd=TRUE, cex = 1.3, col = "black")
## cex is for font size, 
text(counts$type,x = xx,offset = -0.2,y = -15,cex = 2.2,srt = 90,xpd = TRUE,pos = 2 )
dev.off()

file_path <- paste(args[1],'signalerPerpackage.csv',sep="")
counts <- read.csv(file=file_path,nrows=10)
pdf(file="r_fig/sig-package.pdf")
## Find a range of y's that'll leave sufficient space above the tallest bar
ylim <- c(0, 1.25*max(counts$count))
par(mar=c(14, 0.0, 0.0, 0.0))
xx <- barplot(counts$count,axes=FALSE,width=(0.5)*9,ylim=ylim)
text(x = xx, y = counts$count, label = counts$count, pos = 3,offset=0.4, srt=0,cex = 1.8, col = "black")
text(x = xx, y = counts$count, label = counts$per, pos = 3,offset=1.9, srt=0,cex = 1.8, col = "red")
#text(x = xx, y = counts$count, label = counts$exception, pos = 2,xpd=TRUE, cex = 1.3, col = "black")
## cex is for font size, 
text(counts$type,x = xx,offset = -0.2,y = -15,cex = 2.2,srt = 90,xpd = TRUE,pos = 2 )
dev.off()

# Boxplots
counts <- read.csv(file=paste(args[1],'exceptionboxplot.csv',sep=""))  
pdf(file="r_fig/boxplot-exception.pdf")
boxplot(counts, las = 2, ylim=c(0,0.8),par(cex.axis=1.8,cex.lab=0.5,mar = c(20,3,0,0)+0.1))

counts <- read.csv(file=paste(args[1],'packageboxplot.csv',sep=""))  
pdf(file="r_fig/boxplot-package.pdf")
boxplot(counts, las = 2, ylim=c(0,0.8),par(cex.axis=1.8,cex.lab=0.5,mar = c(20,3,0,0)+0.1))

counts <- read.csv(file=paste(args[1],'yearboxplot29-19.csv',sep=""),check.names=FALSE)  
pdf(file="r_fig/boxplot-year-29-19.pdf",width=4,height=2)
boxplot(counts, las=1, ylim=c(0,0.6),par(cex.axis=0.8,cex.lab=0.5,mar = c(3,3,0,0)+0.1), boxwex=0.5)

counts <- read.csv(file=paste(args[1],'handleexceptionboxplot.csv',sep=""))  
pdf(file="r_fig/boxplot-handle-exception.pdf")
boxplot(counts, las = 2, ylim=c(0,0.8),par(cex.axis=1.8,cex.lab=0.5,mar = c(20,3,0,0)+0.1))

counts <- read.csv(file=paste(args[1],'handlepackageboxplot.csv',sep=""))  
pdf(file="r_fig/boxplot-handle-package.pdf")
boxplot(counts, las = 2, ylim=c(0,0.8),par(cex.axis=1.8,cex.lab=0.5,mar = c(20,3,0,0)+0.1))


# # Barplots
# counts <- read.csv(file='failurecase.csv')  
# pdf(file="barplot-failure-case.pdf")
# par(mar=c(16,3,2,0))
# barplot(counts$Case, las=2,ylim=c(0,35),names.arg=counts$Exception, cex.names=1.5)

# counts <- read.csv(file=paste(args[1],'handlepackageboxplot.csv',sep=""))  
# pdf(file="r_fig/boxplot-handle-package.pdf")
# boxplot(counts, las = 2, ylim=c(0,0.8),par(cex.axis=1.8,cex.lab=0.5,mar = c(20,3,0,0)+0.1))

file_path <- paste(args[1],'failurecase.csv',sep="")
counts <- read.csv(file=file_path,nrows=10)
pdf(file="r_fig/barplot-failure-case.pdf")
## Find a range of y's that'll leave sufficient space above the tallest bar
ylim <- c(0, 1.25*max(counts$Case+counts$Unique))
par(mar=c(21,0.0, 0.0, 0.0))
xx <- barplot(rbind(counts$Case,counts$Unique),axes=FALSE,width=(0.5)*9,ylim=ylim)
#text(x = xx, y = counts$Case, label = counts$Case, pos = 3,offset=0.4, srt=0,cex = 1.8, col = "black")
#text(x = xx, y = counts$count, label = counts$per, pos = 3,offset=1.9, srt=0,cex = 1.8, col = "red")
#text(x = xx, y = counts$count, label = counts$exception, pos = 2,xpd=TRUE, cex = 1.3, col = "black")
## cex is for font size, 
text(counts$Exception,x = xx,offset = -0.2,y = -1,cex = 2.2,srt = 90,xpd = TRUE,pos = 2 )
dev.off()