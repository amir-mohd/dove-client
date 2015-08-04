package com.tothenew.doveclient


class UploadToSSHServerJob {
    def fileUploadService
    static triggers = {
    }

    def execute(executionContext) {

        String sourceFile = executionContext.jobDataMap.sourceFile
        String destinationFile = executionContext.jobDataMap.destinationFile
        Long requestID = executionContext.jobDataMap.requestID as Long
        println(requestID)
        String osName = System.getProperty("os.name")
        Boolean isInProcess
        if (osName.toLowerCase().indexOf("win") >= 0) {
            isInProcess = fileUploadService.uploadFileFromWindowsOSToSSHServer(sourceFile, destinationFile, requestID)
            fileUploadService.updateQueueStatus(isInProcess, requestID)
        } else if (osName.toLowerCase().indexOf("linux") >= 0||osName.toLowerCase().indexOf("mac    ") >= 0) {
            isInProcess = fileUploadService.uploadFileFromLinuxOSToSSHServer(sourceFile, destinationFile)
            fileUploadService.updateQueueStatus(isInProcess, requestID)
        }
    }
}
