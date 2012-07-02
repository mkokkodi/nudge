coeffs <- read.table(file="/Users/mkokkodi/Desktop/bigFiles/nudge/results/L2R_LR_coeffs.csv",head=FALSE,sep=",")

avgCoeff <- rep(0,15)
stdevCoeff <- rep(0,15)
for(i in 2:16) {
avgCoeff[i-1] <- colMeans(coeffs[i])
stdevCoeff[i-1] <- sapply(coeffs[i],sd)
print(avgCoeff[i-1],3)
print(stdevCoeff[i-1],2)
};



