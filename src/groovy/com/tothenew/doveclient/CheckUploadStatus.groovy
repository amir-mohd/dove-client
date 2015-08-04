package com.tothenew.doveclient

class CheckUploadStatus implements Runnable {

    Process process
    Long requestID
    String serverURL
//    File logFile=new File("C:\\UploadToElemental\\log.txt")
    public void uploadStatus(Process process, Long requestID, String serverURL) {
        this.serverURL = serverURL
        this.process = process
        this.requestID = requestID
        Thread thread = new Thread(this)
        thread.start()
    }

    public void run() {
        while (true) {
            if (!isRunning(process)) {
//                logFile.append("success")
                notifyToServer(requestID, "success")
                break
            }
            try {
                Thread.sleep(10000)
            } catch (Exception e) {
                notifyToServer(requestID, "failed")
                break
            }

        }
    }

    protected boolean isRunning(Process process) {

        try {
            int exitVal=process.exitValue()
//            logFile.append(exitVal+"\n")
            return false
        } catch (Exception e) {
//            logFile.append(" \nException "+e.getMessage()+"\n")
            return true
        }
    }

    def notifyToServer(Long requestID, String msg) {
        try{
            new URL(serverURL + "?requestID=${requestID}&msg=${msg}").text
        }catch (Exception e){
            println e.getMessage()
        }
    }
}
