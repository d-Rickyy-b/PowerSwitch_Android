/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.shared.log;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.power_switch.shared.R;
import eu.power_switch.shared.application.ApplicationHelper;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

/**
 * This class handles all Log4JLog file/folder related stuff
 * <p/>
 * Created by Markus on 25.08.2015.
 */
public class LogHelper {

    /**
     * Default E-Mail recipients
     */
    private static final String[] DEFAULT_EMAILS = new String[]{"contact@power-switch.eu"};

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private LogHelper() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Get all zip file containing all current log files
     *
     * @return Zip file containing log files
     */
    @Nullable
    public static File getLogsAsZip(Context context) throws MissingPermissionException {
        if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(context)) {
            throw new MissingPermissionException(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String tempZipFileName = "logs.zip";
        String tempZipFilePath = Environment.getExternalStorageDirectory() + File.separator + TimberHelper.FileLoggingTree.LOG_FOLDER_NAME_EXTERNAL + File.separator + tempZipFileName;
        int    bufferSize      = 1024;

        // delete previous temp zip file
        File zipFile = new File(tempZipFilePath);
        if (zipFile.exists()) {
            zipFile.delete();
        }

        BufferedInputStream origin = null;
        FileOutputStream    dest   = null;
        try {
            dest = new FileOutputStream(tempZipFilePath);
            ZipOutputStream out    = new ZipOutputStream(new BufferedOutputStream(dest));
            byte            data[] = new byte[bufferSize];

            for (File logFile : TimberHelper.FileLoggingTree.getInternalLogFiles(context)) {
                FileInputStream fi = new FileInputStream(logFile);
                origin = new BufferedInputStream(fi, bufferSize);

                ZipEntry entry = new ZipEntry(logFile.getName());
                out.putNextEntry(entry);

                int count;
                while ((count = origin.read(data, 0, bufferSize)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();

            return zipFile;
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (origin != null) {
                try {
                    origin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dest != null) {
                try {
                    dest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Send Logs to an Email App via Intent
     *
     * @param destinationAddresses destination addresses (or null)
     * @param throwable            an exception that should be used for subject and content text
     * @param timeRaised           time the exception was raised
     */
    public static void sendLogsAsMail(Context context, String[] destinationAddresses, Throwable throwable, Date timeRaised) throws Exception {
        if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(context)) {
            throw new MissingPermissionException(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (destinationAddresses == null) {
            destinationAddresses = DEFAULT_EMAILS;
        }

        String subject;
        if (throwable == null) {
            subject = "PowerSwitch Logs";
        } else {
            subject = "Unknown Error - " + throwable.getClass()
                    .getSimpleName() + ": " + throwable.getMessage();
        }

        String content;
        if (throwable == null) {
            content = context.getString(R.string.send_logs_template);
        } else {
            content = context.getString(R.string.send_unknown_error_log_template);
            content += "\n\n\n";
            content += "<<<<<<<<<< DEVELOPER INFOS >>>>>>>>>>\n";
            content += "Exception was raised at: " + SimpleDateFormat.getDateTimeInstance()
                    .format(timeRaised) + "\n";
            content += "\n";
            content += "PowerSwitch Application Version: " + ApplicationHelper.getAppVersionDescription(context) + "\n";
            content += "Device API Level: " + android.os.Build.VERSION.SDK_INT + "\n";
            content += "Device OS Version name: " + Build.VERSION.RELEASE + "\n";
            content += "Device brand/model: " + LogHelper.getDeviceName() + "\n";
            content += "\n";
            content += "Exception stacktrace:\n";
            content += "\n";
            content += Log4JLog.getStackTraceText(throwable) + "\n";
        }

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SENDTO);
        emailIntent.setType("*/*");
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, destinationAddresses);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(LogHelper.getLogsAsZip(context)));

        Intent intent = Intent.createChooser(emailIntent, context.getString(R.string.send_to));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    /**
     * Send Logs to an E-Mail App via Intent
     * This includes an Exception that has been raised just before
     *
     * @param throwable  exception
     * @param timeRaised time the exception was raised
     */
    public static void sendLogsAsMail(Context context, Throwable throwable, Date timeRaised) throws Exception {
        sendLogsAsMail(context, null, throwable, timeRaised);
    }

    /**
     * Send Logs to an E-Mail App via Intent
     */
    public static void sendLogsAsMail(Context context) throws Exception {
        sendLogsAsMail(context, null, null, null);
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model        = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[]  arr            = str.toCharArray();
        boolean capitalizeNext = true;
        String  phrase         = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    /**
     * Add indentation to a String with multiple lines
     *
     * @param string any text
     *
     * @return indented string
     */
    public static String addIndentation(String string) {
        StringBuilder stringBuilder = new StringBuilder();

        Scanner scanner = new Scanner(string);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // process the line
            stringBuilder.append("\t")
                    .append(line);

            if (scanner.hasNextLine()) {
                stringBuilder.append("\n");
            }
        }
        scanner.close();

        return stringBuilder.toString();
    }


}
