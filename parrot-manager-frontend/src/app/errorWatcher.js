errorWatcherTitle = null;
errorWatcherReport = null;

window.onerror = function(message, url, lineNumber, column, error)
{
    // Build friendly string
    title = "JavaScript Exception - [" + lineNumber + "," + column + "] " + message;

    body  = "Message:\n";
    body += message + "\n\n";
    body += "URL:\n";
    body += url + "\n\n";
    body += "Line number: " + lineNumber + ", column: " + column + "\n\n";

    console.log(errorWatcherReport);

    // Show dialogue to user
    errorWatcherShow(title, body);
}

function errorWatcherShow(title, body)
{
    // Append generic body template
    body =  "Exception Details\n"
         +  "==================>\n"
         + title + "\n\n"
         + body;

    body += "\n\n";
    body += "Platform Details\n";
    body += "==================>\n";
    body += this.buildInfoService.getBuildInfo();
    body += "\n";
    body += "Other Useful Details\n";
    body += "==================>\n";
    body += "(please insert any steps to reproduce etc)";

    // Store for later
    errorWatcherTitle = title;
    errorWatcherReport = body;

    // Inform user
    bootbox.dialog({
        message :   "<h3>Unexpected exception</h3>" +
                    "<p>Something has gone wrong, please send us the below information on our Github. Be sure to remove any sensitive information.</p>" +
                    "<div class='form-group'><textarea class='form-control' rows='12'>" + body + "</textarea></div>" +
                    "<div class='form-group clearfix'>" +
                        "<button class='btn btn-primary pull-left' onclick='errorWatcherGithub()'>Copy Github Issue Link</button>" +
                        "<button class='btn btn-primary pull-right' onclick='errorWatcherCopy()'>Copy</button>" +
                        "<span class='pull-right'>&nbsp;</span>" +
                        "<button class='btn btn-default pull-right' onclick='window.location.reload()'>Reload UI</button>" +
                    "</div>"
    });
}

function errorWatcherCopy()
{
    this.clipboardService.setText(errorWatcherReport);
    toastr.info("Info copied to clipboard");
}

function errorWatcherGithub()
{

    var url = "https://github.com/limpygnome/parrot-manager/issues/new?";
    url += "title=" + encodeURIComponent(errorWatcherTitle);
    url += "&body=" + encodeURIComponent(errorWatcherReport);

    this.runtimeService.setClipboard(url);
    toastr.info("Github link copied to clipboard");
}

console.log("error watcher setup");
