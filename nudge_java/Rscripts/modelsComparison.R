aucC1 <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_1m.csv",head=TRUE,sep=",")
aucL1 <-read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L1R_LR_aucPoints_C1_3m.csv",head=TRUE,sep=",")
#aucL2dual <-read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_DUAL_aucPoints_C1_3m.csv",head=TRUE,sep=",")

plot(aucC1$X0.0,aucC1$X0.0.1, ylim=range(0,1),xlim=range(0,1),
     type='o',pch = 12, lty=1, col="blue", lwd=3, ylab="TP Rate (TP/P)", 
     xlab="FP Rate (FP/N)")


lines(aucL1$X0.0,aucL1$X0.0.1, type="o", pch=22, lty=2, lwd=3, col="red")
#lines(aucL2dual$X0.0,aucL2dual$X0.0.1, type="o", pch=21, lty=3, lwd=3,col="green")
lines(aucL2dual$X0.0,aucL2dual$X0.0, type="l",  lwd=2, col="black")

legend( "topleft",c("L2R_LR","L1R_LR"), border=c(NA,NA,NA,NA),  cex=1, 
       col=c("blue","red","black"), pch=c(12,22,21,-1), lty=c(1,2,3,1))

