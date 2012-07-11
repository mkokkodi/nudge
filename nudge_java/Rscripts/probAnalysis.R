#lifts100k <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lifts_100k.csv",head=TRUE,sep=",")
#lifts200k <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lifts_200k.csv",head=TRUE,sep=",")
probs500k <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_probsComparison_500k.csv",head=TRUE,sep=",")
probs1m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_probsComparison_1m.csv",head=TRUE,sep=",")
probs2m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_probsComparison_2m.csv",head=TRUE,sep=",")
probs2.5m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_probsComparison_2_5m.csv",head=TRUE,sep=",")
probs3m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_probsComparison_3m.csv",head=TRUE,sep=",")

attach(probs500k)
plot(Pr.predicted_positive.threshold.,probs500k$Pr.actual_positive., ylim=range(0,1),xlim=range(0,1),
     type='o',pch = 12, lty=1, col="blue", lwd=3, ylab="Actual probability of being positive", 
     xlab="Predicted probability of being positive")

lines(Pr.predicted_positive.threshold.,probs1m$Pr.actual_positive., type="o", pch=22, lty=2, lwd=3, col="red")
lines(Pr.predicted_positive.threshold.,probs2m$Pr.actual_positive., type="o", pch=21, lty=3, lwd=3,col="green")
lines(Pr.predicted_positive.threshold.,probs2_5m$Pr.actual_positive., type="o", pch=20, lty=4, lwd=3,col="orange")
lines(Pr.predicted_positive.threshold.,probs3m$Pr.actual_positive., type="o", pch=18, lty=5, lwd=3, col="purple")
#lines(Pr.predicted_positive.threshold.,lifts2_5m$Pr.actual_positive., type="o", pch=10, lty=6, lwd=3, col="magenta")
lines(Pr.predicted_positive.threshold.,Pr.predicted_positive.threshold., type="l",  lwd=2, col="black")

legend( "topleft", c("0.5M","1M", "2M", "2.5M","3M","Random"), cex=1, 
        col=c("blue","red","green","orange","purple","black"), pch=c(12,22,21,20,18,-1), lty=1:5)
