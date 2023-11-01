import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.*
import java.nio.file.Paths
import kotlin.system.exitProcess


var packageCodes = mutableMapOf<String, String>()
fun main(args: Array<String>) {
    val cwd = Paths.get("").toAbsolutePath().toString()
    val packageLookup = File(cwd.plus("\\input_package_lookup.csv"))
    val reader = packageLookup.bufferedReader()
    reader.readLine()
    var code = reader.readLine()
    while(code != null) {
        val key = code.substringBeforeLast(",").lowercase()
        val value = code.substringAfterLast(",")
        packageCodes[key] = value
        code = reader.readLine()
    }
    reader.close()
    RunConverter()
}

fun RunConverter(){
    var validInput = false
    var pdfText: List<String> = emptyList()
    var fileInput = ""
    println("Provide target file path or type exit to close the program")
    while(!validInput){
        fileInput = readLine()!!.replace("\"", "")
        if(fileInput.lowercase() == "exit")
            exitProcess(1)
        var pdfFile: File
        try{
            pdfFile = File(fileInput)
            if(!fileInput.endsWith(".pdf")){
                println("File must be a pdf, please check your input and then try again or type exit to close the program")
                continue
            }
        }
        catch (e: Exception){
            println("Invalid file, please check your input and then try again or type exit to close the program")
            continue
        }
        pdfText = GetPdfText(pdfFile)
        var isOrderForm = false
        for(line in pdfText){
            if(line.startsWith("Order #"))
                isOrderForm = true
        }
        if(!isOrderForm){
            println("File provided does not appear to be an order form, please check your input and then try again or type exit to close the program")
            continue
        }
        validInput = true
    }
    if(pdfText.isNotEmpty()){
        val orders = ProcessRawText(pdfText)
        WriteCsv(orders, fileInput)
    }
}

fun GetPdfText(pdfFile: File): List<String> {
    try {

        // Load the PDF document
        val document: PDDocument = Loader.loadPDF(pdfFile)

        val noPages = document.numberOfPages

        // Create a PDFTextStripper object
        val pdfStripper = PDFTextStripper()

        // Extract text from the PDF
        val pdfText = pdfStripper.getText(document)

        // Close the document
        document.close()
        val pdfList = pdfText.split("\n").toMutableList()
        var headerLine = pdfList[0]
        val headerRegex = Regex("\\\\d{1,2}/\\\\d{1,2}/\\\\d{2}, \\\\d{1,2}:\\\\d{2}")
        val matchResult = headerRegex.find(headerLine)
        if(matchResult == null){
            headerLine = "no header line"
        }
        var i = 0
        var end = false
        while(!end){
            var line = pdfList[i]
            if(line == " \r"){
                if(pdfList[i+1] == headerLine){
                    pdfList.removeAt(i)
                    pdfList.removeAt(i)
                }
                pdfList.removeAt(i)
            }
            if(line == headerLine){
                pdfList.removeAt(i)
                pdfList.removeAt(i)
            }
            i++
            if(i >= pdfList.size)
                end = true
        }

        /*for(line in pdfList){
            println(line)
        }

        exitProcess(0)*/

        return pdfList
    } catch (e: Exception) {
        e.printStackTrace()
        return listOf("")
    }
}

fun ProcessRawText(pdfList: List<String>): List<Order>{
    val addressRegex = Regex("[^,]+(,[^,]+)+(,[^,]+)+[\\d\\d\\d\\d\\d]")
    var sizeString = ""
    for(key in packageCodes.keys){
        val cleanKey = key.replace("(", "\\(").replace(")", "\\)")
        sizeString = sizeString.plus(cleanKey).plus("|")
    }
    sizeString = sizeString.substringBeforeLast("|")
    sizeString = "($sizeString)"
    val sizeRegex = Regex(sizeString, RegexOption.IGNORE_CASE)
    var orders = ArrayList<Order>()
    var i = 0
    var currentOrder: Order = Order(-1, packageCodes)
    while(i < (pdfList.size)){
        if(pdfList[i].startsWith("Order #")){
            val orderNum = Integer.valueOf(pdfList[i].substringAfter("#").replace("\r", ""))
            currentOrder = Order(orderNum, packageCodes)
        }
        if(pdfList[i].startsWith("Gallery Name")){
            val galleryName = pdfList[i].substringAfter(":").replace("\r", "")
            currentOrder.galleryName = galleryName
        }
        if(pdfList[i].startsWith("Billing Address")){
            val nameBillingAddress = pdfList[i+1].replace("\r", "")
            currentOrder.nameBillingAddress = nameBillingAddress
        }
        if(pdfList[i].startsWith("Buyer Shipping Address")){
            while(addressRegex.find(pdfList[i]) == null){
                i++
            }
            i++
            if(pdfList[i].startsWith("Buyer Comments")){
                var resolved = false
                println("Buyer comments encountered")
                while(!resolved){
                    println("Please type the line number of the last line on which there are buyer comments and then press enter. If the end of the buyer comments is not visible, type \"next\" and then press enter")
                    for(j in 0 until 5){
                        println("${j+1}: ".plus(pdfList[i+j]))
                    }
                    val userInput = readLine()!!.lowercase()
                    val inputCheck = Regex("(?:[1-5]|next)").find(userInput)
                    while(inputCheck == null){
                        println("Valid inputs are a digit between 1 and 5 or \"next\"")
                        for(j in 0 until 5){
                            println("${j+1}: ".plus(pdfList[i+j]))
                        }
                        continue
                    }
                    if(userInput == "next"){
                        i+=5
                        continue
                    }
                    i+=userInput.toInt()
                    resolved = true
                }
            }
            while(!pdfList[i].startsWith("Subtotal")){
                var curImage = pdfList[i].substringBefore(".jpg").replace("\r", "").replace(" ", "")
                while(!pdfList[i].contains(".jpg")) {
                    i++
                    curImage = curImage.plus(pdfList[i].substringBefore(".jpg").replace("\r", "").replace(" ", ""))
                }
                curImage = curImage.plus(".jpg")
                currentOrder.fileOrdered = curImage
                //find size ordered
                var matchResult: MatchResult? = sizeRegex.find(pdfList[i])
                while(matchResult == null) {
                    i++
                    matchResult = sizeRegex.find(pdfList[i])
                }
                val matchedText = matchResult.value
                currentOrder.sizeOrdered = matchedText
                //find quantity and total price
                while(!pdfList[i].contains("Qty Item Price Total Price")){
                    i++
                }
                i++
                currentOrder.quantity = Integer.valueOf(pdfList[i].substringBefore(" "))
                currentOrder.totalPrice = pdfList[i].substringAfterLast(" ").replace("\r", "")

                //add subsequent images
                i++
                orders.add(currentOrder)
                currentOrder = currentOrder.copyOrder()
            }
        }
        i++
    }
    return orders
}



fun WriteCsv(orders: List<Order>, inputFileName: String) {
    try{
        val cwd = Paths.get("").toAbsolutePath().toString()
        val newCsv = File(cwd + "\\output\\" + inputFileName.substringAfterLast("\\").replace(".pdf", "_") + "output.csv")
        val fw = FileWriter(newCsv)
        val writer = BufferedWriter(fw)

        writer.write("Order#,GalleryName,NameBillingAddress,FilesOrdered,SizeOrdered,PackageCode,Quantity,Total Price,Package,\n")
        for(i in 0 until (orders.size)){
            writer.write(orders[i].convertToCSV())
        }
        writer.close()
        println("pdf converted!")
    }
    catch(ioe: IOException){
        ioe.printStackTrace()
    }
}