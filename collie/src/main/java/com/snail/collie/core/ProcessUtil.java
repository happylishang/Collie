package com.snail.collie.core;

import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProcessUtil {

    static String sProcName;

    public static String getProcessName() {
        if (!TextUtils.isEmpty(sProcName)) {
            return sProcName;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return sProcName = processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
