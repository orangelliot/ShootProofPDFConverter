import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.*
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    var validInput = false
    var pdfText: List<String> = emptyList()
    var fileInput: String = ""
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

        // Create a PDFTextStripper object
        val pdfStripper = PDFTextStripper()

        // Extract text from the PDF
        val pdfText: String = pdfStripper.getText(document)

        // Print the extracted text
        //println(pdfText)

        // Close the document
        document.close()

        val pdfList = pdfText.split("\n").toMutableList()
        val headerLine = pdfList[0]
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

        exitProcess(0)
*/
        return pdfList
    } catch (e: Exception) {
        e.printStackTrace()
        return listOf("")
    }
}

fun ProcessRawText(pdfList: List<String>): List<Order>{
    val zipcodeRegex = Regex("[^,]+(,[^,]+)+(,[^,]+)+[\\d\\d\\d\\d\\d]")
    val sizeRegex = Regex("(2\\.5 x 3\\.5 \\(8 Wallets\\)|4 x 6|5 x 7|8 x 10|11 x 14|All Photos from this album|Single Photo)")
    var orders = ArrayList<Order>()
    var i = 0
    var currentOrder: Order = Order(-1)
    while(i < (pdfList.size)){
        if(pdfList[i].startsWith("Order #")){
            val orderNum = Integer.valueOf(pdfList[i].substringAfter("#").replace("\r", ""))
            currentOrder = Order(orderNum)
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
            while(zipcodeRegex.find(pdfList[i]) == null){
                i++
            }
            i++
            while(!pdfList[i].startsWith("Subtotal")){
                var curImage = pdfList[i].substringBefore(".jpg").replace("\r", "")
                if(!pdfList[i].contains(".jpg")) {
                    i++
                    curImage = curImage.plus(pdfList[i].substringBefore(".jpg").replace("\r", ""))
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
                currentOrder = currentOrder.incrementOrder()
            }
        }
        i++
    }
    return orders
}

fun WriteCsv(orders: List<Order>, inputFileName: String) {
    try{
        val cwd = Paths.get("").toAbsolutePath().toString()
        val newCsv = File(cwd + "\\" + inputFileName.substringAfterLast("\\").replace(".pdf", "_") + "output.csv")
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