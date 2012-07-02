accAnalysis <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_acc.csv",head=TRUE,sep=",")

attach(accAnalysis)
# Define 2 vectors


plot(data_size,AUC)

plot(data_size, ACC_major_class)
lines(data_size,ACC_model, type="o", pch=22, lty=2, col="red")

