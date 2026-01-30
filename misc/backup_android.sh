export SERIAL=19231JEC208361
adb -s $SERIAL exec-out run-as "$PKG" sh -c 'cd files && tar -cf - .' > "backup$(date '+%Y%m%d%H%M%S').tar"