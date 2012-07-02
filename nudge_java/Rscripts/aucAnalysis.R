auc500k <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_500k.csv",head=TRUE,sep=",")
auc1m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_1m.csv",head=TRUE,sep=",")
auc2m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_2m.csv",head=TRUE,sep=",")
auc2_5m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_2_5m.csv",head=TRUE,sep=",")
auc3m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_aucPoints_3m.csv",head=TRUE,sep=",")




  plot(auc500k$X0.0,auc500k$X0.0.1, ylim=range(0,1),xlim=range(0,1),
     type='o',pch = 12, lty=1, col="blue", lwd=3, ylab="TP Rate (TP/P)", 
       xlab="FP Rate (FP/N)")



lines(auc1m$X0.0,auc1m$X0.0.1, type="o", pch=22, lty=2, lwd=3, col="red")

lines(auc2m$X0.0,auc2m$X0.0.1, type="o", pch=21, lty=3, lwd=3,col="green")
lines(auc2_5m$X0.0,auc2_5m$X0.0.1, type="o", pch=20, lty=4, lwd=3,col="orange")
lines(auc3m$X0.0,auc3m$X0.0.1, type="o", pch=18, lty=5, lwd=3, col="purple")
#lines(Pr.predicted_positive.threshold.,lifts2_5m$Pr.actual_positive., type="o", pch=10, lty=6, lwd=3, col="magenta")
lines(auc1m$X0.0,auc1m$X0.0, type="l",  lwd=2, col="black")

legend( "topleft", c("0.5M","1M", "2M", "2.5M","3M","Random"), cex=1, 
        col=c("blue","red","green","orange","purple","black"), pch=c(12,22,21,20,18,-1), lty=1:5)
