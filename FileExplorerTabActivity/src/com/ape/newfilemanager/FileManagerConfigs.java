package com.ape.newfilemanager;

import android.util.Xml;

import com.ape.newfilemanager.helper.Util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileManagerConfigs
{
    private static final String CONFIG_XML_FILE_NAME = "/system/etc/FileManagerConfigs.xml";
    private static final String TAG_IS_HAVE_CLOUD = "is_have_cloud";
    private static final String TAG_FTP_ROOT_PATH = "ftp_root_path";

    private static boolean mIsHaveCloud = false;
    private static String mFtpRootPath = null;

    static {
        loadConfigsFile();
    }

    public static void generateConfigs(InputStream xml)
            throws XmlPullParserException, IOException
    {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(xml, "UTF-8");
        int event = pullParser.getEventType();

        while (event != XmlPullParser.END_DOCUMENT)
        {
            switch (event)
            {
                case XmlPullParser.START_DOCUMENT:
                    break;

                case XmlPullParser.START_TAG:
                    if (TAG_IS_HAVE_CLOUD.equals(pullParser.getName()))
                    {
                        String value = pullParser.nextText();
                        mIsHaveCloud = ("true".equals(value)) ? true : false;
                    } else if (TAG_FTP_ROOT_PATH.equals(pullParser.getName()))
                    {
                        mFtpRootPath = pullParser.nextText();
                    }
                    break;

                case XmlPullParser.END_TAG:
                    break;
            }
            event = pullParser.next();
        }
    }
    
    public static void loadConfigsFile()
    {
        File xmlFile = new File(CONFIG_XML_FILE_NAME);
        FileInputStream fileIn = null;
        try
        {
            if (xmlFile.exists())
            {
                fileIn = new FileInputStream(xmlFile);
                generateConfigs(fileIn);
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try
            {
                if (fileIn != null)
                    fileIn.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean isHaveCloudModule()
    {
        return mIsHaveCloud;
    }

    public static String getFtpRootPath()
    {
        return mFtpRootPath != null ? mFtpRootPath : Util.getSdDirectory();
    }
}
