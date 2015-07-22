package com.tothenew.doveclient

import com.tothenew.dove.UploadToSSHServerJob

class FileController {
    def fileUploadService


    def upload(String sourceFile, String destinationFile) {
//        String osName = System.getProperty("os.name")
//        println(params)
//        if (osName.toLowerCase().indexOf("window") >= 0) {
//            fileUploadService.uploadFileFromWindowsOSToSSHServer(sourceFile, destinationFile)
//        } else if (osName.toLowerCase().indexOf("linux") >= 0) {
//            fileUploadService.uploadFileFromLinuxOSToSSHServer(sourceFile, destinationFile)
//        }
        UploadToSSHServerJob.triggerNow(params)
        render("success")
    }
}
