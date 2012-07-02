lifts500k <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lift_500k.csv",head=TRUE,sep=",")
lifts1m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lift_1m.csv",head=TRUE,sep=",")
lifts2m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lift_2m.csv",head=TRUE,sep=",")
lifts2_5m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lift_2_5m.csv",head=TRUE,sep=",")
lifts3m <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/probs/L2R_LR_lift_3m.csv",head=TRUE,sep=",")

plot(lifts500k$Predicted_prob_gt,lifts500k$lift,xlim=range(0,1),
     type='o',pch = 12, lty=1, col="blue", lwd=3, ylab="Lift", 
     xlab="Predicted probability greater than x ")

lines(lifts500k$Predicted_prob_gt,lifts1m$lift, type="o", pch=22, lty=2, lwd=3, col="red")
lines(lifts500k$Predicted_prob_gt,lifts2m$lift, type="o", pch=21, lty=3, lwd=3,col="green")
lines(lifts500k$Predicted_prob_gt,lifts2_5m$lift, type="o", pch=20, lty=4, lwd=3,col="orange")
lines(lifts500k$Predicted_prob_gt,lifts3m$lift, type="o", pch=18, lty=5, lwd=3, col="purple")

legend( "topleft", c("0.5M","1M", "2M", "2.5M","3M"), cex=1, 
       col=c("blue","red","green","orange","purple"), pch=c(12,22,21,20,18), lty=1:5)