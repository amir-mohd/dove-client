package com.tothenew.doveclient

import com.jcraft.jsch.*
import grails.transaction.Transactional

@Transactional
class FileUploadService {


    def grailsApplication
    String serverURL

    def notifyToServer(Long requestID, String msg) {
        serverURL = grailsApplication.config.dove.server.url
        log.info(new URL(serverURL + "?requestID=${requestID}&msg=${msg}").text)
    }

    def updateQueueStatus(Boolean isInProcess, Long requestID) {
        if (isInProcess) {
            notifyToServer(requestID, "success")
        } else {
            notifyToServer(requestID, "failed")
        }
    }

    Boolean uploadFileFromWindowsOSToSSHServer(String sourceFilePath, String destinationFilePath, Long requestID) {
        Boolean isInProcess = false
        File logFile = new File("C:\\UploadToElemental\\log.txt")
        serverURL = grailsApplication.config.dove.server.url
        String file = DoveClientConstants.WINDOWS_DIR_PATH + "\\" + sourceFilePath
        String elementalAddress = grailsApplication.config.dove.elemental.address
        String passwd = "elemental"
        String[] command = ["cmd.exe", "/C", "echo y | pscp.exe -pw " + passwd + " -p " + file + " " + elementalAddress + ":" + destinationFilePath];
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader perr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        BufferedReader pout = new BufferedReader(new InputStreamReader(p.getInputStream()));

        for (String s = pout.readLine(); s != null; s = pout.readLine()) {
            isInProcess = true
            log.info("" + s + "\n");
        }
        for (String s = perr.readLine(); s != null; s = perr.readLine()) {
            log.info(" " + s + "\n");
        }
        perr.close();
        pout.close();
        return isInProcess
    }


    boolean uploadFileFromLinuxOSToSSHServer(String sourceFile, String destinationFile) {
        FileInputStream fis = null;
        try {
            sourceFile = sourceFile.replace(" ", "_")
            String localFile = sourceFile;

            String user = "elemental";
            String host = "192.168.4.10";
            String remoteFile = destinationFile

            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);

            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            session.connect();

            boolean ptimestamp = true;

            // exec 'scp -t remoteFile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFile;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream inFile = channel.getInputStream();

            channel.connect();

            if (checkAck(inFile) != 0) {
                return false
            }

            File _lfile = new File(localFile);

            if (ptimestamp) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(inFile) != 0) {
                    return false
                }
            }

            // send "C0644 filesize filename", where filename should not include
            // '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (localFile.lastIndexOf('/') > 0) {
                command += localFile.substring(localFile.lastIndexOf('/') + 1);
            } else {
                command += localFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(inFile) != 0) {
                return false
            }

            // send a content of localFile
            fis = new FileInputStream(localFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0)
                    break;
                out.write(buf, 0, len); // out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(inFile) != 0) {
                return false
            }
            out.close();

            channel.disconnect();
            System.out.println("channel disconnected successfully")
            session.disconnect();
            System.out.println("session disconnected successfully")

        } catch (Exception e) {
            System.out.println("===========" + e.getMessage() + "==========");
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception ee) {
                return false
            }
            return false
        }
        return true
    }

    static int checkAck(InputStream inFile) throws IOException {
        int b = inFile.read();

        if (b == 0)
            return b;
        if (b == -1)
            return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = inFile.read()) != '\n') {
                sb.append((char) c);

            }
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
