#!/bin/bash
# ref: http://www.theregister.co.uk/Print/2013/11/29/feature_diy_apple_ibeacons/
set -x
# inquiry local bluetooth device
#hcitool dev
export BLUETOOTH_DEVICE=hci0

export OFG="0x08"
export OCF="0x0008"
export IBEACONPROFIX="1E 02 01 1A 1A FF 4C 00 02 15"
export UUID="06 CC 44 0C 00 C0 00 A8 00 00 00 00 00 00 00 07"
export MAJOR="19 E1"
export MINOR="00 03"
export POWER="C5 00"

sudo hciconfig $BLUETOOTH_DEVICE up
sudo hciconfig $BLUETOOTH_DEVICE noleadv
sudo hciconfig $BLUETOOTH_DEVICE noscan
sudo hciconfig $BLUETOOTH_DEVICE pscan
sudo hciconfig $BLUETOOTH_DEVICE leadv

sudo hcitool -i $BLUETOOTH_DEVICE cmd 0x08 0x0008 $IBEACONPROFIX $UUID $MAJOR $MINOR $POWER
sudo hcitool -i $BLUETOOTH_DEVICE cmd 0x08 0x0006 A0 00 A0 00 00 00 00 00 00 00 00 00 00 07 00
sudo hcitool -i $BLUETOOTH_DEVICE cmd 0x08 0x000a 01

echo "complete"
