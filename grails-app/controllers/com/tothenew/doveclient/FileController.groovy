package com.tothenew.doveclient

class FileController {
    def fileUploadService


    def upload(String sourceFile, String destinationFile,Long requestID) {

        UploadToSSHServerJob.triggerNow(params)
        render("processing")
    }
}
