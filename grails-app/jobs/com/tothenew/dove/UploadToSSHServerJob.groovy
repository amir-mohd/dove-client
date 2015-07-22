package com.tothenew.dove


class UploadToSSHServerJob {
    def fileUploadService
    static triggers = {
    }

    def execute(executionContext) {
        String sourceFile = executionContext.jobDataMap.sourceFile
        String destinationFile = executionContext.jobDataMap.destinationFile
        String osName = System.getProperty("os.name")
        println sourceFile + " " + destinationFile
        if (osName.toLowerCase().indexOf("window") >= 0) {
            fileUploadService.uploadFileFromWindowsOSToSSHServer(sourceFile, destinationFile)
        } else if (osName.toLowerCase().indexOf("linux") >= 0) {
            fileUploadService.uploadFileFromLinuxOSToSSHServer(sourceFile, destinationFile)
            println("Hello-------------------------")
        }
    }
}
