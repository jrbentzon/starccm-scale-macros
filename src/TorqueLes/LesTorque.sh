# wget https://raw.githubusercontent.com/jrbentzon/starccm-scale-thermodynamics/main/src/TorqueLes/LesTorque.sh
RPM=__iRPM__
MeshSize=5
FlowRateMlMin=16.6
RotorDiameter=80e-3

rm *.java
mkdir -p Results
wget https://raw.githubusercontent.com/jrbentzon/starccm-scale-thermodynamics/main/src/TorqueLes/CouetteCell.java
wget https://raw.githubusercontent.com/jrbentzon/starccm-scale-thermodynamics/main/src/TorqueLes/RecordReynoldsStresses.java

cp RecordReynoldsStresses.java Results/

sed "s/__RPM__/$RPM/" -i CouetteCell.java
sed "s/__MeshSize__/$MeshSize/" -i CouetteCell.java
sed "s/__FlowRateMlMin__/$FlowRateMlMin/" -i CouetteCell.java
sed "s/__RotorDiameter__/$RotorDiameter/" -i CouetteCell.java