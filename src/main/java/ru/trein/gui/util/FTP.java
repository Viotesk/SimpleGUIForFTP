package ru.trein.gui.util;

import com.trein.FTPClient.FTPClient;

/**
 * Created by Shpien on 08.06.2017.
 */
public class FTP {
    private static FTPClient instance;

    private FTP(){}

    public static FTPClient getInstance() {
        if (instance == null)
            instance = new FTPClient();

        return instance;
    }

}
