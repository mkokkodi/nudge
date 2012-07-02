aucC1 <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_1m.csv",head=TRUE,sep=",")
aucC05 <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_C0.5_3m.csv",head=TRUE,sep=",")
aucC025 <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_C0.25_3m.csv",head=TRUE,sep=",")
aucC006 <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_C0.062_3m.csv",head=TRUE,sep=",")
aucC2 <-read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_C2_3m.csv",head=TRUE,sep=",")
aucC4 <-read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_C4_3m.csv",head=TRUE,sep=",")
aucC16 <-read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_C16_3m.csv",head=TRUE,sep=",")

plot(aucC1$X0.0,aucC1$X0.0.1, ylim=range(0,1),xlim=range(0,1),
     type='o',pch = 12, lty=1, col="blue", lwd=3, ylab="TP Rate (TP/P)", 
     xlab="FP Rate (FP/N)")

lines(aucC05$X0.0,aucC05$X0.0.1, type="o", pch=22, lty=2, lwd=3, col="red")
lines(aucC025$X0.0,aucC025$X0.0.1, type="o", pch=21, lty=3, lwd=3,col="green")
lines(aucC006$X0.0,aucC006$X0.0.1, type="o", pch=20, lty=4, lwd=3,col="orange")
lines(aucC2$X0.0,aucC2$X0.0.1, type="o", pch=18, lty=5, lwd=3, col="purple")
lines(aucC4$X0.0,aucC4$X0.0.1, type="o", pch=10, lty=6, lwd=3, col="magenta")
lines(aucC16$X0.0,aucC16$X0.0.1, type="o", pch=1, lty=7, lwd=3, col="yellow")

legend( "topleft",c("C=1","C=0.5", "C=0.25","C=0.06", "C=2", "C=4","C=16"), border=c(NA,NA,NA,NA),  cex=1, 
        col=c("blue","red","green","orange","purple", "magenta","yellow"), pch=c(12,22,21,20,18,10,1), lty=c(1,2,3,4,5,6,7))
