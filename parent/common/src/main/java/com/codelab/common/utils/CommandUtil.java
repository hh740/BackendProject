package com.codelab.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CommandUtil {

    private static Logger logger = LoggerFactory.getLogger(CommandUtil.class);

    private static int EXIT_NORMAL = 0;

    private static int EXIT_UNNORMAL = 1;

    // 将paser变成可执行文件
    public static boolean chmod(String route, String fileName) {

        logger.debug("route:{}", route);
        logger.debug("fileName:{}", fileName);

        String cmdArray[] = {"chmod", "a+x", fileName};
        // 返回与当前 Java 应用程序相关的运行时对象
        Runtime runTime = Runtime.getRuntime();
        // 启动另一个进程来执行命令
        try {
            Process p = runTime.exec(cmdArray, null, new File(route));

            // waitFor方法会一直阻塞直到native进程完成
            p.waitFor();

            if (p.exitValue() == EXIT_NORMAL)
                return true;
            else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("excute chmod error !");
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("process hasn't exited !");
            return false;
        }
    }

    public static boolean excuteMRJob(String route, String shellName) {

        logger.debug("route:{}", route);
        logger.debug("shellName:{}", shellName);

        String[] cmdArray = {"./" + shellName};
        // 返回与当前 Java 应用程序相关的运行时对象
        Runtime runTime = Runtime.getRuntime();
        // 启动另一个进程来执行命令
        try {
            Process p = runTime.exec(cmdArray, null, new File(route));

            // waitFor方法会一直阻塞直到native进程完成
            p.waitFor();

            if (p.exitValue() == EXIT_NORMAL)
                return true;
            else
                return false;

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("excute error !");
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("process hasn't exited !");
            return false;
        }
    }

}